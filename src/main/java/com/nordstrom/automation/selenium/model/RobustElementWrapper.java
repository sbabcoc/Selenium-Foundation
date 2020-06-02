package com.nordstrom.automation.selenium.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.internal.FindsByCssSelector;
import org.openqa.selenium.internal.FindsByXPath;

import com.nordstrom.automation.selenium.AbstractSeleniumConfig.WaitType;
import com.nordstrom.automation.selenium.core.ByType;
import com.nordstrom.automation.selenium.core.JsUtility;
import com.nordstrom.automation.selenium.core.WebDriverUtils;
import com.nordstrom.automation.selenium.exceptions.ElementReferenceRefreshFailureException;
import com.nordstrom.automation.selenium.exceptions.OptionalElementNotAcquiredException;
import com.nordstrom.automation.selenium.interfaces.WrapsContext;
import com.nordstrom.automation.selenium.model.RobustElementFactory.InterceptionAccessor;
import com.nordstrom.automation.selenium.support.Coordinator;
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
@SuppressWarnings("squid:S1200")
public class RobustElementWrapper implements ReferenceFetcher {
    
    /** wraps 1st matched reference */
    public static final int CARDINAL = -1;
    /** wraps an optional reference */
    public static final int OPTIONAL = -2;
    /** boolean methods that return 'false' when optional element is absent */
    private static final List<String> BOOLEAN_METHODS = Arrays.asList("isDisplayed", "isEnabled");
    
    private static final String LOCATE_BY_CSS = JsUtility.getScriptResource("locateByCss.js");
    private static final String LOCATE_BY_XPATH = JsUtility.getScriptResource("locateByXpath.js");
    
    private enum Strategy { LOCATOR, JS_XPATH, JS_CSS }
    
    private final WebDriver driver;
    private final WrapsContext context;
    private WebElement wrapped;
    private By locator;
    private int index;
    
    private String selector;
    private RobustElementWrapper.Strategy strategy = Strategy.LOCATOR;
    
    private long acquiredAt;
    
    private NoSuchElementException deferredException;
    
    private final boolean findsByCssSelector;
    private final boolean findsByXPath;
    
    /**
     * Main robust web element constructor
     * 
     * @param element element reference to be wrapped (may be 'null')
     * @param context element search context
     * @param locator element locator
     * @param index element index
     */
    @SuppressWarnings({"squid:S3776", "squid:MethodCyclomaticComplexity"})
    public RobustElementWrapper(
                    final WebElement element, final WrapsContext context, final By locator, final int index) {
        
        // if specified element is already robust
        if (element instanceof RobustWebElement) {
            RobustElementWrapper wrapper = ((InterceptionAccessor) element).getInterceptor();
            this.acquiredAt = wrapper.acquiredAt;
            
            this.wrapped = wrapper.wrapped;
            this.context = wrapper.context;
            this.locator = wrapper.locator;
            this.index = wrapper.index;
        } else {
            Objects.requireNonNull(context, "[context] must be non-null");
            Objects.requireNonNull(locator, "[locator] must be non-null");
            if (index < OPTIONAL) {
                throw new IndexOutOfBoundsException("Specified index is invalid");
            }
            
            this.wrapped = element;
            this.context = context;
            this.locator = locator;
            this.index = index;
        }
        
        driver = WebDriverUtils.getDriver(this.context.getWrappedContext());
        
        findsByCssSelector = (driver instanceof FindsByCssSelector);
        findsByXPath = (driver instanceof FindsByXPath);
        
        if ((this.index == OPTIONAL) || (this.index > 0)) {
            if (findsByXPath && ( ! (this.locator instanceof By.ByCssSelector))) {
                selector = ByType.xpathLocatorFor(this.locator);
                if (this.index > 0) {
                    selector += "[" + (this.index + 1) + "]";
                }
                strategy = Strategy.JS_XPATH;
                
                this.locator = By.xpath(this.selector);
            } else if (findsByCssSelector) {
                selector = ByType.cssLocatorFor(this.locator);
                if (selector != null) {
                    strategy = Strategy.JS_CSS;
                }
            }
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
     * This is the method that intercepts component container methods in "enhanced" model objects.
     * 
     * @param obj "enhanced" object upon which the method was invoked
     * @param method {@link Method} object for the invoked method
     * @param args method invocation arguments
     * @return {@code anything} (the result of invoking the intercepted method)
     * @throws Exception {@code anything} (exception thrown by the intercepted method)
     */
    @RuntimeType
    @BindingPriority(Integer.MAX_VALUE)
    public Object intercept(@This final Object obj, @Origin final Method method,
                    @AllArguments final Object[] args) throws Exception { //NOSONAR
        try {
            return invoke(method, args);
        } catch (StaleElementReferenceException sere) {
            refreshReference(sere);
            return invoke(method, args);
        }
    }
    
    /**
     * Invoke the specified method with arguments provided.
     * 
     * @param method {@link Method} object for the method to be invoked
     * @param args method invocation arguments
     * @return {@code anything} (the result of invoking the specified method)
     * @throws Exception {@code anything} (exception thrown by the specified method)
     */
    private Object invoke(Method method, Object... args) throws Exception { // NOSONAR
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
     * {@inheritDoc}
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
        return RobustElementFactory.getElement(this, locator, OPTIONAL);
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
        } catch (TimeoutException e) { //NOSONAR
            if (refreshTrigger == null) {
                throw new ElementReferenceRefreshFailureException(e.getMessage(), e.getCause());
            }
        } catch (WebDriverException e) {
            if (refreshTrigger == null) {
                throw e;
            }
        }
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
                } catch (StaleElementReferenceException e) { //NOSONAR
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
     * Acquire the element reference that's wrapped by the specified robust element wrapper.
     * 
     * @param wrapper robust element wrapper
     * @return wrapped element reference
     */
    @SuppressWarnings({"squid:S3776", "squid:S134"})
    private static RobustElementWrapper acquireReference(final RobustElementWrapper wrapper) {
        NoSuchElementException thrown = null;
        SearchContext context = wrapper.context.getWrappedContext();
        
        if (wrapper.strategy == Strategy.LOCATOR) {
            Timeouts timeouts = wrapper.driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
            try {
                if (wrapper.index > 0) {
                    List<WebElement> elements = context.findElements(wrapper.locator);
                    if (wrapper.index < elements.size()) {
                        wrapper.wrapped = elements.get(wrapper.index);
                    } else {
                        thrown = new NoSuchElementException(
                                String.format("Too few elements located %s: need: %d; have: %d", 
                                        wrapper.locator, wrapper.index + 1, elements.size()));
                    }
                } else {
                    try {
                        wrapper.wrapped = context.findElement(wrapper.locator);
                    } catch (NoSuchElementException e) {
                        // if context is a web element
                        if (context instanceof WebElement) {
                            // force exception if context stale
                            ((WebElement) context).getTagName();
                        }
                        thrown = e;
                    }
                }
            } finally {
                timeouts.implicitlyWait(WaitType.IMPLIED.getInterval(), TimeUnit.SECONDS);
            }
        } else {
            List<Object> args = new ArrayList<>();
            List<WebElement> contextArg = new ArrayList<>();
            if (context instanceof WebElement) {
                contextArg.add((WebElement) context);
            }
            
            String js;
            args.add(contextArg);
            args.add(wrapper.selector);
            
            if (wrapper.strategy == Strategy.JS_XPATH) {
                js = LOCATE_BY_XPATH;
            } else {
                js = LOCATE_BY_CSS;
                args.add(wrapper.index);
            }
            
            wrapper.wrapped = JsUtility.runAndReturn(wrapper.driver, js, args.toArray());
            
            if (wrapper.wrapped == null) {
                String message;
                if (wrapper.index > 0) {
                    message = String.format("Cannot locate an element at index %d using %s",
                            wrapper.index, wrapper.selector);
                } else {
                    message = String.format("Cannot locate an element using %s", wrapper.selector);
                }
                thrown = new NoSuchElementException(message);
            }
        }
        
        if (thrown != null) {
            wrapper.wrapped = null;
            if (wrapper.index != OPTIONAL) {
                throw thrown;
            }
            wrapper.deferredException = thrown;
        } else {
            wrapper.acquiredAt = System.currentTimeMillis();
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
    @SuppressWarnings({"squid:S1142", "squid:S1126"})
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof RobustWebElement)) //NOSONAR
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
