package com.nordstrom.automation.selenium.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WebDriver.Timeouts;

import com.nordstrom.automation.selenium.AbstractSeleniumConfig.SeleniumSettings;
import com.nordstrom.automation.selenium.AbstractSeleniumConfig.WaitType;
import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.core.JsUtility;
import com.nordstrom.automation.selenium.core.WebDriverUtils;
import com.nordstrom.automation.selenium.exceptions.ElementReferenceRefreshFailureException;
import com.nordstrom.automation.selenium.exceptions.OptionalElementNotAcquiredException;
import com.nordstrom.automation.selenium.interfaces.WrapsContext;
import com.nordstrom.automation.selenium.model.RobustElementFactory.InterceptionAccessor;
import com.nordstrom.automation.selenium.support.Coordinator;
import com.nordstrom.automation.selenium.utility.SearchContextUtils;
import com.nordstrom.common.base.UncheckedThrow;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.BindingPriority;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

/**
 * This class defines the generic interceptor for the methods of wrapped web element references. It also provides
 * implementations for methods that acquire web element references and recover from StaleElementReferenceException
 * failures.  
 */
public class RobustElementWrapper implements ReferenceFetcher {
    
    /** wraps 1st matched reference */
    public static final int CARDINAL = -1;
    /** wraps an optional reference */
    public static final int OPTIONAL = -2;
    /** boolean methods that return 'false' when optional element is absent */
    private static final List<String> BOOLEAN_METHODS = Arrays.asList("isDisplayed", "isEnabled");
    
    private enum Strategy { LOCATOR, SCRIPT }
    
    private final WebDriver driver;
    private final WrapsContext context;
    private final String script;
    private final Object[] scriptArgs;
    private final By locator;
    private final int index;
    private final Strategy strategy;
    
    private WebElement wrapped;
    
    private long acquiredAt;
    
    private NoSuchElementException deferredException;
    
    /**
     * Main robust web element constructor
     * 
     * @param element element reference to be wrapped (may be 'null')
     * @param context element search context
     * @param locator element locator
     * @param index element index
     */
    public RobustElementWrapper(
                    final WebElement element, final WrapsContext context, final By locator, final int index) {
        
        // if specified element is already robust
        if (element instanceof RobustWebElement) {
            RobustElementWrapper wrapper = ((InterceptionAccessor) element).getInterceptor();
            
            this.driver = wrapper.driver;
            this.context = wrapper.context;
            
            this.script = wrapper.script;
            this.scriptArgs = wrapper.scriptArgs;
            this.locator = wrapper.locator;
            this.index = wrapper.index;
            this.strategy = wrapper.strategy;
            
            this.wrapped = wrapper.wrapped;
            this.acquiredAt = wrapper.acquiredAt;
        } else {
            Objects.requireNonNull(context, "[context] must be non-null");
            Objects.requireNonNull(locator, "[locator] must be non-null");
            
            if (index < OPTIONAL) {
                throw new IndexOutOfBoundsException("Specified index is invalid");
            }
            
            this.driver = context.getWrappedDriver();
            this.context = context;
            this.script = null;
            this.scriptArgs = new Object[0];
            this.locator = locator;
            this.index = index;
            this.strategy = Strategy.LOCATOR;
            this.wrapped = element;
        }
        
        if (this.wrapped == null) {
            if (this.index == OPTIONAL) {
                acquireReference(this);
            } else {
                refreshReference(null);
            }
        } else if (acquiredAt == 0) {
            acquiredAt = System.currentTimeMillis();
        }
    }
    
    /**
     * Robust web element constructor for script-based element location, with script arguments.
     * <p>
     * {@code scriptArgs}, if given, are replayed verbatim on every future refresh, not just this initial
     * acquisition - a script that depends on runtime arguments to run correctly (a search term, a
     * threshold value, anything beyond the container itself) needs those arguments to survive staleness
     * recovery just as much as it needs them the first time.
     * 
     * @param element element reference to be wrapped (may be 'null')
     * @param context element search context
     * @param script JavaScript to locate the wrapped element
     * @param scriptArgs arguments to pass to {@code script} on this and every future invocation - the
     *      container itself is prepended automatically when {@code context} is element-relative; this
     *      array should hold only the caller's own additional arguments
     */
    public RobustElementWrapper(
            final WebElement element, final WrapsContext context, final String script,
            final Object... scriptArgs) {
        
        this.driver = context.getWrappedDriver();
        this.context = context;
        
        this.script = script;
        this.scriptArgs = scriptArgs;
        this.locator = null;
        this.index = CARDINAL;
        this.strategy = Strategy.SCRIPT;
        
        this.wrapped = element;
        
        if (this.wrapped == null) {
            refreshReference(null);
        } else if (acquiredAt == 0) {
            acquiredAt = System.currentTimeMillis();
        }
    }
    
    /**
     * This is the method that intercepts <b>WebElement</b> methods in "wrapped" element objects.
     * 
     * @param obj "wrapped" object upon which the method was invoked
     * @param method {@link Method} object for the invoked method
     * @param args method invocation arguments
     * @return {@code anything} (the result of invoking the intercepted method)
     * @throws Exception {@code anything} (exception thrown by the intercepted method)
     */
    @RuntimeType
    @BindingPriority(Integer.MAX_VALUE)
    public Object intercept(@This final Object obj, @Origin final Method method, @AllArguments final Object[] args)
            throws Exception {
        SeleniumConfig config = SeleniumConfig.getConfig();
        long retryTimeout = config.getLong(SeleniumSettings.REFRESH_RETRY_TIMEOUT.key());
        int retryMaximum = config.getInt(SeleniumSettings.REFRESH_RETRY_MAXIMUM.key());
        
        int attempts = 0;
        long deadline = System.currentTimeMillis() + (retryTimeout * 1_000);
        StaleElementReferenceException sere = null;

        while (true) {
            try {
                return invoke(method, args);
            } catch (StaleElementReferenceException e) {
                sere = e;
                // if retry count exceeded or time limit expired
                if (++attempts > retryMaximum || System.currentTimeMillis() >= deadline)
                    break; // exit
                
                // try ref refresh
                refreshReference(e);
                // if ref not acquired
                if (this.wrapped == null) 
                    break; // exit
            }
        }
        // re-throw
        throw sere;
    }

    /**
     * Invoke the specified method with arguments provided.
     * 
     * @param method {@link Method} object for the method to be invoked
     * @param args method invocation arguments
     * @return {@code anything} (the result of invoking the specified method)
     * @throws Exception {@code anything} (exception thrown by the specified method)
     */
    private Object invoke(Method method, Object... args) throws Exception {
        WebElement target = getWrappedElement();
        
        if (target == null) {
            if (BOOLEAN_METHODS.contains(method.getName())) {
                return Boolean.FALSE;
            }
            throw deferredException();
        }
        
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException ite) {
            throw UncheckedThrow.throwUnchecked(ite.getCause());
        }
    }
    
    /**
     * Get the <b>WebElement</b> object contained within this wrapper.
     * 
     * @return unwrapped {@link WebElement} object
     */
    @Override
    public WebElement getWrappedElement() {
        if (wrapped == null) {
            refreshReference(null);
        }
        return wrapped;
    }
    
    /**
     * Search for the specified optional element
     * 
     * @param by the locating mechanism
     * @return web element
     */
    public WebElement findOptional(final By by) {
        return RobustElementFactory.getElement(this, by, OPTIONAL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasReference() {
        if ((index == OPTIONAL) && (wrapped == null)) {
            acquireReference(this);
            return (null != wrapped);
        } else {
            return true;
        }
    }
    
    /**
     * Get the exception deferred upon failing to acquire the reference for an optional element.
     * <p>
     * Package-private: exists for test support only, so tests can assert on the specific cause of an
     * acquisition failure without reflecting into the raw field.
     * 
     * @return deferred exception; {@code null} if no acquisition has failed
     */
    NoSuchElementException getDeferredException() {
        return deferredException;
    }
    
    /**
     * Get the currently wrapped element reference, without attempting acquisition.
     * <p>
     * Package-private: exists for test support only. Unlike {@link #getWrappedElement()}, this never triggers
     * {@link #refreshReference}, so it's safe to call after a failed acquisition to confirm the reference was
     * actually cleared, rather than left dangling.
     * 
     * @return wrapped element reference; {@code null} if not currently acquired
     */
    WebElement getWrapped() {
        return wrapped;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public WrapsContext getContext() {
        return context;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public By getLocator() {
        return locator;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getIndex() {
        return index;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public RobustElementWrapper refreshReference(final StaleElementReferenceException refreshTrigger) {
        try {
            WaitType.IMPLIED.getWait((SearchContext) context).until(referenceIsRefreshed(this));
            return this;
        } catch (TimeoutException e) {
            // if not auto-recovery
            if (refreshTrigger == null) {
                throw UncheckedThrow.throwUnchecked(e.getCause());
            } else {
                StaleElementReferenceException refreshFailure = 
                        new ElementReferenceRefreshFailureException(refreshTrigger.getMessage(), refreshTrigger.getCause());
                refreshFailure.setStackTrace(refreshTrigger.getStackTrace());
                throw refreshFailure;
            }
        } catch (WebDriverException e) {
            // if not auto-recovery
            if (refreshTrigger == null) {
                throw e;
            }
        }
        // re-throw exception
        throw refreshTrigger;
    }
    
    /**
     * Returns a 'wait' proxy that refreshes the wrapped reference of the specified robust element.
     * 
     * @param wrapper robust element wrapper
     * @return wrapped element reference (refreshed)
     */
    private static Coordinator<RobustElementWrapper> referenceIsRefreshed(final RobustElementWrapper wrapper) {
        return new Coordinator<RobustElementWrapper>() {
            
            /**
             * {@inheritDoc}
             */
            @Override
            public RobustElementWrapper apply(final SearchContext context) {
                try {
                    return acquireReference(wrapper);
                } catch (StaleElementReferenceException e) {
                    ((WrapsContext) context).refreshContext(((WrapsContext) context).acquiredAt());
                    return acquireReference(wrapper);
                }
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString() {
                return "element reference to be refreshed";
            }
        };
        
    }
    
    /**
     * Build the effective argument list for a script-based acquisition or refresh: the underlying native
     * search context itself, prepended automatically when {@code context} is element-relative, followed by
     * the caller's own {@code scriptArgs}.
     * <p>
     * Takes the underlying native {@link SearchContext} (what {@code WrapsContext.getWrappedContext()}
     * returns), not a {@code WrapsContext} - only an actual browser-side handle can be marshaled as a
     * script argument at all, since {@code WrapsContext} is a pure Java-side abstraction with no wire
     * representation.
     * <p>
     * Used identically for a script's initial invocation and every subsequent refresh, so a script that
     * depends on the context being {@code arguments[0]} sees it in the same position every time.
     * 
     * @param context underlying native search context (element- or document-relative)
     * @param scriptArgs the caller's own script arguments (not including the context itself)
     * @return effective argument list to pass to the script
     */
    public static Object[] buildScriptArgs(final SearchContext context, final Object... scriptArgs) {
        if (!SearchContextUtils.isElementContext(context)) {
            return scriptArgs;
        }
        Object[] combined = new Object[scriptArgs.length + 1];
        combined[0] = context;
        System.arraycopy(scriptArgs, 0, combined, 1, scriptArgs.length);
        return combined;
    }
    
    /**
     * Acquire the element reference that's wrapped by the specified robust element wrapper.
     * <p>
     * Package-private (rather than {@code private}): the sole intended caller remains this class's own
     * instance methods, but tests need to invoke it directly to exercise acquisition failure in isolation,
     * without going through {@link #refreshReference}'s wait-and-retry loop.
     * 
     * @param wrapper robust element wrapper
     * @return wrapped element reference
     * @throws StaleElementReferenceElementException if container element has gone stale
     * @throws NoSuchElementException if unable to find element specified by the wrapper
     */
    static RobustElementWrapper acquireReference(final RobustElementWrapper wrapper) {
        NoSuchElementException thrown = null;
        
        if (!wrapper.context.hasReference()) {
            // parent context is an unresolved optional element/component - nothing to search
            thrown = new NoSuchElementException(
                    String.format("Unable to search for %s: parent context is an absent optional element",
                            wrapper.locator));
        } else if (wrapper.strategy == Strategy.LOCATOR) {
            SearchContext context = wrapper.context.getWrappedContext();
            // disable implicit wait
            Timeouts timeouts = wrapper.driver.manage().timeouts();
            WebDriverUtils.implicitlyWait(timeouts, Duration.ZERO);
            try {
                // if index specified
                if (wrapper.index > 0) {
                    // find elements specified by wrapper locator
                    List<WebElement> elements = context.findElements(wrapper.locator);
                    // if sufficient elements were found
                    if (wrapper.index < elements.size()) {
                        // get element at specified index
                        wrapper.wrapped = elements.get(wrapper.index);
                    } else {
                        thrown = new NoSuchElementException(
                                String.format("Too few elements located %s: need: %d; have: %d", 
                                        wrapper.locator, wrapper.index + 1, elements.size()));
                    }
                } else {
                    try {
                        // find element specified by wrapper locator
                        wrapper.wrapped = context.findElement(wrapper.locator);
                    } catch (NoSuchElementException e) {
                        // if context is a web element
                        if (SearchContextUtils.isElementContext(context)) {
                            // force exception if context stale
                            ((WebElement) context).getTagName();
                        }
                        thrown = e;
                    }
                }
            } finally {
                // restore implicit wait
                WebDriverUtils.implicitlyWait(timeouts, Duration.ofSeconds(WaitType.IMPLIED.getInterval()));
            }
        } else {
            SearchContext context = wrapper.context.getWrappedContext();
            try {
                Object[] callArgs = buildScriptArgs(context, wrapper.scriptArgs);
                wrapper.wrapped = JsUtility.runAndReturn(wrapper.driver, wrapper.script, callArgs);
            } catch (NoSuchElementException e) {
                // e.g. an indexed-extraction script's own bounds-check failure (via __wd.fail)
                thrown = e;
            }
            
            // if no reference acquired
            if (thrown == null && wrapper.wrapped == null) {
                String message;
                // if context is a container element
                if (SearchContextUtils.isElementContext(context)) {
                    message = String.format("Failed to locate element using script: %s\nin context: %s",
                            wrapper.script.trim(), context);
                } else {
                    message = String.format("Failed to locate element using script: %s", wrapper.script.trim());
                }
                thrown = new NoSuchElementException(message);
            }
        }
        
        // if exception thrown
        if (thrown != null) {
            // discard wrapped ref
            wrapper.wrapped = null;
            // if element isn't optional
            if (wrapper.index != OPTIONAL) {
                // throw now
                throw thrown;
            }
            // store deferred exception
            wrapper.deferredException = thrown;
        } else {
            // set acquisition time for future refresh
            wrapper.acquiredAt = System.currentTimeMillis();
            // clear deferred exception
            wrapper.deferredException = null;
        }
        
        return wrapper;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public SearchContext getWrappedContext() {
        return getWrappedElement();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SearchContext refreshContext(final long expiration) {
        // if wrapped element has expired
        if (expiration >= acquiredAt()) {
            // refresh context ancestry
            context.refreshContext(expiration);
            // refresh wrapped element
            return refreshReference(null);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long acquiredAt() {
        return acquiredAt;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public WebDriver getWrappedDriver() {
        return driver;
    }
    
    /**
     * Throw the deferred exception that was stored upon failing to acquire the reference for an optional element.
     * <br><p>
     * <b>NOTE</b>:
     * The deferred exception is not thrown directly - it's wrapped in a OptionalElementNotAcquiredException to
     * indicate that the failure was caused by utilizing an optional element for which no actual reference could
     * be acquired.
     * 
     * @return nothing (always throws deferred exception wrapped in OptionalElementNotAcquiredException)
     */
    private OptionalElementNotAcquiredException deferredException() {
        throw new OptionalElementNotAcquiredException(deferredException);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SearchContext switchTo() {
        return context.switchTo();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SearchContext switchToParentFrame() {
        return context.switchToParentFrame();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + context.hashCode();
        result = prime * result + locator.hashCode();
        result = prime * result + index;
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof RobustWebElement))
            return false;
        RobustElementWrapper other = ((InterceptionAccessor) obj).getInterceptor();
        if (!context.equals(other.context))
            return false;
        if (!locator.equals(other.locator))
            return false;
        if (index != other.index)
            return false;
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebElement findElement(final By locator) {
        return RobustElementFactory.getElement(this, locator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WebElement> findElements(final By locator) {
        return RobustElementFactory.getElements(this, locator);
    }
}
