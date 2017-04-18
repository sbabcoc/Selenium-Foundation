package com.nordstrom.automation.selenium.model;

import java.lang.reflect.Method;
import java.util.Set;

import org.openqa.selenium.WebDriver;

import com.nordstrom.automation.selenium.exceptions.ContainerVacatedException;
import com.nordstrom.automation.selenium.model.Page.WindowState;
import com.nordstrom.automation.selenium.support.Coordinators;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

enum ContainerMethodInterceptor implements MethodInterceptor {
	INSTANCE;
	
	private static final ThreadLocal<ComponentContainer> target = new ThreadLocal<>();

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		
		ComponentContainer container = (ComponentContainer) obj;
		
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
		
		Object result = proxy.invokeSuper(obj, args);
		
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
	}
	
}
