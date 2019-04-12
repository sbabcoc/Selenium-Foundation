package com.nordstrom.automation.selenium.exceptions;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NotFoundException;

import com.nordstrom.automation.selenium.interfaces.DetectsLoadCompletion;
import com.nordstrom.automation.selenium.model.ComponentContainer;
import com.nordstrom.automation.selenium.model.ContainerMethodInterceptor;
import com.nordstrom.automation.selenium.model.Page;
import com.nordstrom.automation.selenium.support.Coordinator;

/**
 * This exception is thrown by implementations of {@link DetectsLoadCompletion#isLoadComplete()} to indicate that 
 * loading of the page is not yet complete. The determination of page-load conditions is scenario-specific and may
 * include the use of {@link ComponentContainer#checkPageLoadCondition(Coordinator, String)}, which facilitates
 * the use of predefined condition evaluation functions for page-load checking.<br>
 * <br>
 * The condition-polling mechanism employed by
 * {@link ContainerMethodInterceptor#intercept(Object, Method, Object[], Callable)} records instances of this
 * exception, but will continue to poll until {@link DetectsLoadCompletion#isLoadComplete()} completes without
 * exception or time runs out. Note that it's not necessary to wrap instances of {@link NotFoundException}
 * (e.g. - {@link NoSuchElementException}) with this exception, as these are automatically handled by
 * {@link ComponentContainer#waitForLandingPage(Page)}.
 */
public class PageNotLoadedException extends RuntimeException {
    private static final long serialVersionUID = -8491929915611599716L;
    
    /**
     * Constructor for {@code page not loaded} exception with specified detail message.
     * 
     * @param message detail message
     */
    public PageNotLoadedException(final String message) {
        super(message);
    }
    
    /**
     * Constructor for {@code page not loaded} exception with specified cause.
     * 
     * @param cause cause of this exception
     */
    public PageNotLoadedException(final Throwable cause) {
        super(cause);
    }
    
    /**
     * Constructor for {@code page not loaded} exception with specified detail message and cause.
     * 
     * @param message detail message
     * @param cause cause of this exception
     */
    public PageNotLoadedException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
