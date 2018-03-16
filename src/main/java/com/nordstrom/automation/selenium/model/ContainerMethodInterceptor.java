package com.nordstrom.automation.selenium.model;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.nordstrom.automation.selenium.SeleniumConfig.WaitType;
import com.nordstrom.automation.selenium.exceptions.ContainerVacatedException;
import com.nordstrom.automation.selenium.interfaces.DetectsLoadCompletion;
import com.nordstrom.automation.selenium.model.Page.WindowState;
import com.nordstrom.automation.selenium.support.Coordinators;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;

/**
 * This enumeration implements the method interceptor for <b>Selenium Foundation</b> component container objects.
 * This interceptor is implemented as a standard Java enumeration singleton and performs the following tasks:
 * <ul>
 *     <li>Block calls to objects that have been superseded (vacated) by prior actions.</li>
 *     <li>Switch driver focus to the window/frame associated with the target object.</li>
 *     <li>If informed that actions of the invoked method will cause the associated window to close: <ul>
 *         <li>Wait for the window to close.</li>
 *         <li>If the target object was spawned by another object, switch focus to this object...</li>
 *         <li>... otherwise, switch focus to the first window in the driver's collection.</li>
 *         <li>Mark the target object as vacated to block further method calls.</li>
 *     </ul></li>
 *     <li>If the invoked method returns a new container object: <ul>
 *         <li>If the new object is a page: <ul>
 *             <li>If the page object is associated with a new window, wait for the window to appear...</li>
 *             <li>... otherwise, mark the target object as vacated to block further method calls.</li>
 *         </ul></li>
 *         <li>Wait for browser to rebuild its DOM.</li>
 *         <li>Create an "enhanced" version of the new container object, which installs the interceptor.</li>
 *         <li>If the new object is a page, verify that the browser has landed on the expected URL.</li>
 *     </ul></li>
 *     <li>Return the result of the invoked method.</li>
 * </ul>
 */
public enum ContainerMethodInterceptor {
    INSTANCE;
    
    private static final ThreadLocal<Integer> DEPTH = new InheritableThreadLocal<Integer>() {
        
        /**
         * {@inheritDoc}
         */
        @Override
        protected Integer initialValue() {
            return Integer.valueOf(0);
        }
    };
    
    private static final ThreadLocal<ComponentContainer> TARGET = new InheritableThreadLocal<>();

    /**
     * This is the method that intercepts component container methods in "enhanced" model objects.
     * 
     * @param obj "enhanced" object upon which the method was invoked
     * @param method {@link Method} object for the invoked method
     * @param args method invocation arguments
     * @param proxy call-able proxy for the intercepted method
     * @return {@code anything} (the result of invoking the intercepted method)
     * @throws Exception {@code anything} (exception thrown by the intercepted method)
     */
    @RuntimeType
    @SuppressWarnings({"squid:S3776", "squid:MethodCyclomaticComplexity", "squid:S1698", "squid:S134"})
    public Object intercept(@This final Object obj, @Origin final Method method, @AllArguments final Object[] args,
                    @SuperCall final Callable<?> proxy) throws Exception {
        
        if (!(obj instanceof ComponentContainer)) {
            return proxy.call();
        }
        
        increaseDepth();
        long initialTime = System.currentTimeMillis();
        ComponentContainer container = (ComponentContainer) obj;
        
        try {
            if (container.isVacated()) {
                throw new ContainerVacatedException(container.getVacater());
            }
            
            WebDriver driver = container.getDriver();
    
            if (TARGET.get() != container) { 
                container.switchTo();
                TARGET.set(container);
            }
            
            WebElement reference = null;
            Class<?> returnType = method.getReturnType();
            Page parentPage = container.getParentPage();
            Set<String> initialHandles = driver.getWindowHandles();
            
            boolean returnsContainer = ComponentContainer.class.isAssignableFrom(returnType);
            boolean returnsPage = Page.class.isAssignableFrom(returnType) && !Frame.class.isAssignableFrom(returnType);
            boolean detectsCompletion = returnsContainer && DetectsLoadCompletion.class.isAssignableFrom(returnType);
            
            if (returnsPage && !detectsCompletion) {
                reference = driver.findElement(By.tagName("*"));
            }
            
            Object result = proxy.call();
            
            // if result is container, we're done
            if (result == container) {
                return result;
            }
            
            if (parentPage.getWindowState() == WindowState.WILL_CLOSE) {
                WaitType.WAIT.getWait(driver).until(Coordinators.windowIsClosed(parentPage.getWindowHandle()));
                parentPage = parentPage.getSpawningPage();
                if (parentPage != null) {
                    parentPage.switchTo();
                    TARGET.set(parentPage);
                } else {
                    String windowHandle = driver.getWindowHandles().iterator().next();
                    driver.switchTo().window(windowHandle);
                    TARGET.set(null);
                }
                container.setVacater(method);
                reference = null;
            }
            
            if (returnsContainer) {
                Objects.requireNonNull(result, "A method that returns container objects cannot produce a null result");
                
                String newHandle = null;
                ComponentContainer newChild = (ComponentContainer) result;
                
                if (returnsPage) {
                    Page newPage = (Page) result;
                    if (newPage.getWindowState() == WindowState.WILL_OPEN) {
                        newHandle = WaitType.WAIT.getWait(driver).until(Coordinators.newWindowIsOpened(initialHandles));
                        newPage.setSpawningPage(parentPage);
                        reference = null;
                    } else {
                        newHandle = driver.getWindowHandle();
                        container.setVacater(method);
                    }
                }
                
                result = newChild.enhanceContainer(newChild);
                if (newHandle != null) {
                    ((Page) result).setWindowHandle(newHandle);
                    ComponentContainer.waitForLandingPage((Page) result);
                }
                
                if (detectsCompletion) {
                    ((ComponentContainer) result).getWait(WaitType.PAGE_LOAD)
                            .until(DetectsLoadCompletion.pageLoadIsComplete());
                } else if (reference != null) {
                    WaitType.PAGE_LOAD.getWait(driver).until(Coordinators.stalenessOf(reference));
                }
            }
            
            return result;
        } finally {
            int level = decreaseDepth();
            long interval = System.currentTimeMillis() - initialTime;
            
            if (level == 0) {
                container.getLogger().info("[{}] {} ({}ms)", level, method.getName(), interval);
            } else {
                container.getLogger().debug("[{}] {} ({}ms)", level, method.getName(), interval);
            }
        }
    }
    
    /**
     * Increment intercept depth counter
     * 
     * @return updated depth count
     */
    private static int increaseDepth() {
        return adjustDepth(1);
    }
    
    /**
     * Decrement intercept depth counter
     * 
     * @return updated depth count
     */
    private static int decreaseDepth() {
        return adjustDepth(-1);
    }
    
    /**
     * Apply the specified delta to intercept depth counter
     * 
     * @param delta depth counter delta
     * @return updated depth count
     */
    private static int adjustDepth(final int delta) {
        int i = DEPTH.get().intValue() + delta;
        DEPTH.set(Integer.valueOf(i));
        return i;
    }
}
