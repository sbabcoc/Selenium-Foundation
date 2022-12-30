package com.nordstrom.automation.selenium.model;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.Callable;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nordstrom.automation.selenium.AbstractSeleniumConfig.WaitType;
import com.nordstrom.automation.selenium.exceptions.ContainerVacatedException;
import com.nordstrom.automation.selenium.exceptions.PageLoadRendererTimeoutException;
import com.nordstrom.automation.selenium.exceptions.PageNotLoadedException;
import com.nordstrom.automation.selenium.exceptions.TransitionErrorException;
import com.nordstrom.automation.selenium.exceptions.VacationStackTrace;
import com.nordstrom.automation.selenium.interfaces.DetectsLoadCompletion;
import com.nordstrom.automation.selenium.interfaces.TransitionErrorDetector;
import com.nordstrom.automation.selenium.model.Page.WindowState;
import com.nordstrom.automation.selenium.support.Coordinator;
import com.nordstrom.automation.selenium.support.Coordinators;
import com.nordstrom.common.base.ExceptionUnwrapper;

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
            return 0;
        }
    };
    
    private static final ThreadLocal<ComponentContainer> TARGET = new InheritableThreadLocal<>();
    private static final ServiceLoader<TransitionErrorDetector> errorDetectorLoader =
                    ServiceLoader.load(TransitionErrorDetector.class);
    
    private static final String RENDERER_TIMEOUT_MESSAGE = "receiving message from renderer";

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
    public Object intercept(@This final Object obj, @Origin final Method method, @AllArguments final Object[] args,
                    @SuperCall final Callable<?> proxy) throws Throwable {
        
        if (!(obj instanceof ComponentContainer)) {
            return proxy.call();
        }
        
        increaseDepth();
        long initialTime = System.currentTimeMillis();
        ComponentContainer container = (ComponentContainer) obj;
        
        try {
            if (container.isVacated()) {
                throw new ContainerVacatedException(container.getVacated());
            }
            
            WebDriver driver = container.getWrappedDriver();
    
            if (TARGET.get() != container) { 
                container.switchTo();
                TARGET.set(container);
            }
            
            WebElement reference = null;
            
            Class<?> returnType = method.getReturnType();
            Page parentPage = container.getParentPage();
            Set<String> initialHandles = null;
            try {
                // get initial set of window handles
                initialHandles = driver.getWindowHandles();
            } catch (WebDriverException eaten) {
                // framework errors are forbidden
            }
            
            boolean returnsContainer = ComponentContainer.class.isAssignableFrom(returnType);
            boolean returnsPage = Page.class.isAssignableFrom(returnType) && !Frame.class.isAssignableFrom(returnType);
            boolean detectsCompletion = returnsContainer && DetectsLoadCompletion.class.isAssignableFrom(returnType);
            
            // if expecting page without load logic
            if (returnsPage && !detectsCompletion) {
                try {
                    // get stale wait reference element by CSS selector
                    reference = driver.findElement(By.cssSelector("*"));
                } catch (WebDriverException e) {
                    // get stale wait reference element by XPath
                    reference = driver.findElement(By.xpath("/*"));
                }
            }
            
            // invoke intercepted method
            Object result = proxy.call();
            
            // if result is container, we're done
            if (result == container) return result;
            
            // if expecting parent page window to close
            if (parentPage.getWindowState() == WindowState.WILL_CLOSE) {
                // wait until parent page window closes
                WaitType.WAIT.getWait(driver).until(Coordinators.windowIsClosed(parentPage.getWindowHandle()));
                // get grandparent page
                parentPage = parentPage.getSpawningPage();
                // if grandparent found
                if (parentPage != null) {
                    // switch page context
                    parentPage.switchTo();
                    // record page context
                    TARGET.set(parentPage);
                // otherwise (parent is a root page)
                } else {
                    // get first available window handle
                    String windowHandle = driver.getWindowHandles().iterator().next();
                    // switch driver context to this window
                    driver.switchTo().window(windowHandle);
                    // clear context
                    TARGET.set(null);
                }
                // vacate container, recording associated method, stack trace, and reason
                container.setVacated(new VacationStackTrace(method, "Target window for this container has closed."));
                // no stale wait
                reference = null;
            }
            
            // if container spec'd
            if (returnsContainer) {
                // ensure that a non-null result was returned
                Objects.requireNonNull(result, "A method that returns container objects cannot produce a null result");
                
                String newHandle = null;
                ComponentContainer newChild = (ComponentContainer) result;
                
                // if page spec'd
                if (returnsPage) {
                    Page newPage = (Page) result;
                    // if expecting window to open for new page 
                    if (newPage.getWindowState() == WindowState.WILL_OPEN) {
                        // wait until new window opens
                        newHandle = WaitType.WAIT.getWait(driver).until(Coordinators.newWindowIsOpened(initialHandles));
                        // record parent of new page
                        newPage.setSpawningPage(parentPage);
                        // no stale wait
                        reference = null;
                    // otherwise (new page for existing window)
                    } else {
                        // vacate container, recording associated method, stack trace, and reason
                        container.setVacated(
                                new VacationStackTrace(method, "This page object no longer owns its target window."));
                        try {
                            // get current driver context
                            newHandle = driver.getWindowHandle();
                        } catch (WebDriverException eaten) {
                            // framework errors are forbidden
                        }
                    }
                }
                
                // enhance new container
                result = newChild.enhanceContainer(newChild);
                
                // if context acquired
                if (newHandle != null) {
                    // associate page with target window
                    ((Page) result).setWindowHandle(newHandle);
                    // wait for expected landing page to appear
                    ComponentContainer.waitForLandingPage((Page) result);
                }
                
                // if load logic spec'd
                if (detectsCompletion) {
                    // wait until load logic indicates completion
                    ((ComponentContainer) result).getWait(WaitType.PAGE_LOAD)
                                    .ignoring(PageNotLoadedException.class)
                                    .until(loadIsComplete());
                // otherwise, if stale wait
                } else if (reference != null) {
                    // wait until reference element goes stale
                    WaitType.PAGE_LOAD.getWait((ComponentContainer) result).until(loadIsComplete(reference));
                }
            }
            
            return result;
        } catch (Throwable t) {
            Throwable thrown = ExceptionUnwrapper.unwrap(t);
            if (thrown instanceof TimeoutException) {
                thrown = differentiateTimeout((TimeoutException) thrown);
            }
            throw thrown;
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
        int i = DEPTH.get() + delta;
        DEPTH.set(i);
        return i;
    }
    
    /**
     * Differentiate browser renderer timeouts
     * 
     * @param e undifferentiated timeout exception
     * @return differentiated timeout exception
     */
    private static TimeoutException differentiateTimeout(TimeoutException e) {
        if (e.getClass().equals(TimeoutException.class)) {
            String m = e.getMessage();
            if ((m != null) && m.contains(RENDERER_TIMEOUT_MESSAGE)) {
                TimeoutException d = new PageLoadRendererTimeoutException(m, e.getCause());
                d.setStackTrace(e.getStackTrace());
                return d;
            }
        }
        return e;
    }
    
    /**
     * Returns a 'wait' proxy that determines if the container has finished loading.
     * 
     * @return 'true' if the container has finished loading; otherwise 'false'
     */
    public static Coordinator<Boolean> loadIsComplete() {
        return new Coordinator<Boolean>() {

            @Override
            public Boolean apply(final SearchContext context) {
                scanForErrors(context);
                if (context instanceof DetectsLoadCompletion) {
                    return ((DetectsLoadCompletion) context).isLoadComplete();
                } else {
                    Logger logger = LoggerFactory.getLogger(Enhanceable.getContainerClass(context));
                    logger.warn("This context doesn't provide load completion status");
                    return true;
                }
            }
            
            @Override
            public String toString() {
                return "container to finish loading";
            }
        };
    }
    
    /**
     * Returns a 'wait' proxy that determines if the container has finished loading.
     * 
     * @param element the element to wait for
     * @return 'true' if the container has finished loading; otherwise 'false'
     */
    public static Coordinator<Boolean> loadIsComplete(final WebElement element) {
        return new Coordinator<Boolean>() {
            
            private final Coordinator<Boolean> stalenessOfElement = Coordinators.stalenessOf(element);

            @Override
            public Boolean apply(final SearchContext context) {
                scanForErrors(context);
                return stalenessOfElement.apply(null);
            }
            
            @Override
            public String toString() {
                return "container to finish loading";
            }
        };
    }
    
    /**
     * Notify registered {@link TransitionErrorDetector} service providers to perform a scan for errors.
     * <p>
     * <b>NOTE</b>: The error scan is only performed if the specified search context is a {@link ComponentContainer}.
     * 
     * @param context search context to scan for errors
     */
    static void scanForErrors(SearchContext context) {
        if (context instanceof ComponentContainer) {
            synchronized(errorDetectorLoader) {
                for (TransitionErrorDetector detector : errorDetectorLoader) {
                    String message = detector.scanForErrors((ComponentContainer) context);
                    if (message != null) {
                        throw new TransitionErrorException((ComponentContainer) context, message);
                    }
                }
            }
        }
        
    }
    
}
