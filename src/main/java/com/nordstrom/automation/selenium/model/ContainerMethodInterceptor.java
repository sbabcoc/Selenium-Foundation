package com.nordstrom.automation.selenium.model;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.Callable;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.nordstrom.automation.selenium.exceptions.ContainerVacatedException;
import com.nordstrom.automation.selenium.model.Page.WindowState;
import com.nordstrom.automation.selenium.support.Coordinators;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;

public enum ContainerMethodInterceptor {
	INSTANCE;
	
	private int depth = 0;
	private static final ThreadLocal<ComponentContainer> target = new ThreadLocal<>();

	/**
	 * This is the method that intercepts component container methods in "enhanced" model objects.
	 * 
	 * @param obj "enhanced" object upon which the method was invoked
	 * @param method {@link Method} object for the invoked method
	 * @param args method invocation arguments
	 * @param proxy call-able proxy for the intercepted method
	 * @return {@code anything} (the result of invoking the intercepted method)
	 * @throws Throwable {@code anything} (exception thrown by the intercepted method)
	 */
	@RuntimeType
	public Object intercept(@This Object obj, @Origin Method method, @AllArguments Object[] args, @SuperCall Callable<?> proxy) throws Throwable {
		
		if ( ! (obj instanceof ComponentContainer)) return proxy.call();
		
		depth++;
		long initialTime = System.currentTimeMillis();
		ComponentContainer container = (ComponentContainer) obj;
		
		try {
			if (container.isVacated()) {
				throw new ContainerVacatedException(container.getVacater());
			}
			
			WebDriver driver = container.getDriver();
	
			if (target.get() != container) {
				container.switchTo();
				target.set(container);
			}
			
			Page parentPage = container.getParentPage();
			Set<String> initialHandles = driver.getWindowHandles();
			WebElement reference = driver.findElement(By.tagName("html"));
			
			Object result = proxy.call();
			
			// if result is container, we're done
			if (result == container) return result;
			
			if (parentPage.getWindowState() == WindowState.WILL_CLOSE) {
				parentPage.getWait().until(Coordinators.windowIsClosed(parentPage.getWindowHandle()));
				container.setVacater(method);
			}
			
			if (ComponentContainer.class.isAssignableFrom(method.getReturnType())) {
				if (result == null) throw new NullPointerException("A method that returns container objects cannot produce a null result");
				
				String newHandle = null;
				ComponentContainer newChild = (ComponentContainer) result;
				
				// if new child is a page object
				if (newChild.getParent() == null) {
					Page newPage = (Page) result;
					if (newPage.getWindowState() == WindowState.WILL_OPEN) {
						newHandle = newPage.getWait().until(Coordinators.newWindowIsOpened(initialHandles));
					} else {
						if (newPage.getWindowState() != WindowState.VIA_AJAX) {
							newPage.getWait().until(Coordinators.stalenessOf(reference));
						}
						newHandle = parentPage.getWindowHandle();
						container.setVacater(method);
					}
				}
				
				result = newChild.enhanceContainer(newChild);
				if (newHandle != null) {
					((Page) result).setWindowHandle(newHandle);
				}
			}
			
			return result;
		} finally {
			depth--;
			long interval = System.currentTimeMillis() - initialTime;
			
			if (depth == 0) {
				container.getLogger().info("[{}] {} ({}ms)", depth, method.getName(), interval);
			} else {
				container.getLogger().debug("[{}] {} ({}ms)", depth, method.getName(), interval);
			}
		}
	}

}
