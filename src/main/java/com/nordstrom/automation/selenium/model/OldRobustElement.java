package com.nordstrom.automation.selenium.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.internal.Coordinates;
import org.openqa.selenium.internal.FindsByClassName;
import org.openqa.selenium.internal.FindsByCssSelector;
import org.openqa.selenium.internal.FindsById;
import org.openqa.selenium.internal.FindsByLinkText;
import org.openqa.selenium.internal.FindsByName;
import org.openqa.selenium.internal.FindsByTagName;
import org.openqa.selenium.internal.FindsByXPath;
import org.openqa.selenium.internal.HasIdentity;
import org.openqa.selenium.internal.Locatable;
import org.openqa.selenium.internal.WrapsElement;

import com.nordstrom.automation.selenium.SeleniumConfig.WaitType;
import com.nordstrom.automation.selenium.core.ByType;
import com.nordstrom.automation.selenium.core.JsUtility;
import com.nordstrom.automation.selenium.core.WebDriverUtils;
import com.nordstrom.automation.selenium.exceptions.OptionalElementNotAcquiredException;
import com.nordstrom.automation.selenium.interfaces.WrapsContext;
import com.nordstrom.automation.selenium.support.Coordinator;
import com.nordstrom.common.base.UncheckedThrow;

/**
 * This is a wrapper class for objects that implement the {@link WebElement} interface. If a 
 * {@link StaleElementReferenceException} failure is encountered on invocation of any of the
 * {@link WebElement} methods, an attempt is made to refresh the reference and its search 
 * context chain down to the parent page object. 
 * <p>
 * This class also implements support for 'optional' elements, which provide an efficient 
 * mechanism for handling elements that may be absent in certain scenarios.
 */
public class OldRobustElement implements WebElement, FindsByLinkText, FindsById, FindsByName,
                FindsByTagName, FindsByClassName, FindsByCssSelector, FindsByXPath, Locatable,
                HasIdentity, TakesScreenshot, WrapsElement, WrapsContext
{
    /** wraps 1st matched reference */
    public static final int CARDINAL = -1;
    /** wraps an optional reference */
    public static final int OPTIONAL = -2;
    
    private static final String LOCATE_BY_CSS = JsUtility.getScriptResource("locateByCss.js");
    private static final String LOCATE_BY_XPATH = JsUtility.getScriptResource("locateByXpath.js");
    
    private enum Strategy { LOCATOR, JS_XPATH, JS_CSS }
    
    private final WebDriver driver;
    private final WrapsContext context;
    private WebElement wrapped;
    private By locator;
    private int index;
    
    private String selector;
    private Strategy strategy = Strategy.LOCATOR;
    
    private Long acquiredAt;
    
    private NoSuchElementException deferredException;
    
    private final boolean findsByLinkText;
    private final boolean findsById;
    private final boolean findsByName;
    private final boolean findsByTagName;
    private final boolean findsByClassName;
    private final boolean findsByCssSelector;
    private final boolean findsByXPath;
    private final boolean locatable;
    private final boolean hasIdentity;
    private final boolean takesScreenshot;
    
    /**
     * Basic robust web element constructor
     * 
     * @param context element search context
     * @param locator element locator
     */
    public OldRobustElement(WrapsContext context, By locator) {
        this(null, context, locator, CARDINAL);
    }
    
    /**
     * Constructor for wrapping an existing element reference 
     * 
     * @param element element reference to be wrapped
     * @param context element search context
     * @param locator element locator
     */
    public OldRobustElement(WebElement element, WrapsContext context, By locator) {
        this(element, context, locator, CARDINAL);
    }
    
    /**
     * Main robust web element constructor
     * 
     * @param element element reference to be wrapped (may be 'null')
     * @param context element search context
     * @param locator element locator
     * @param index element index
     */
    public OldRobustElement(WebElement element, WrapsContext context, By locator, int index) {
        
        // if specified element is already robust
        if (element instanceof OldRobustElement) {
            OldRobustElement robust = (OldRobustElement) element;
            this.acquiredAt = robust.acquiredAt;
            
            this.wrapped = robust.wrapped;
            this.context = robust.context;
            this.locator = robust.locator;
            this.index = robust.index;
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
        
        findsByLinkText = (driver instanceof FindsByLinkText);
        findsById = (driver instanceof FindsById);
        findsByName = (driver instanceof FindsByName);
        findsByTagName = (driver instanceof FindsByTagName);
        findsByClassName = (driver instanceof FindsByClassName);
        findsByCssSelector = (driver instanceof FindsByCssSelector);
        findsByXPath = (driver instanceof FindsByXPath);
        locatable = (driver instanceof Locatable);
        hasIdentity = (driver instanceof HasIdentity);
        takesScreenshot = (driver instanceof TakesScreenshot);
        
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
        } else if (acquiredAt == null) {
            acquiredAt = Long.valueOf(System.currentTimeMillis());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <X> X getScreenshotAs(final OutputType<X> arg0) {
        if (!takesScreenshot) {
            throw new UnsupportedOperationException();
        }
        try {
            return getWrappedElement().getScreenshotAs(arg0);
        } catch (StaleElementReferenceException e) {
            return refreshReference(e).getScreenshotAs(arg0);
        } catch (NullPointerException e) {
            throw deferredException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        try {
            getWrappedElement().clear();
        } catch (StaleElementReferenceException e) {
            refreshReference(e).clear();
        } catch (NullPointerException e) {
            throw deferredException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void click() {
        try {
            getWrappedElement().click();
        } catch (StaleElementReferenceException e) {
            refreshReference(e).click();
        } catch (NullPointerException e) {
            throw deferredException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebElement findElement(final By by) {
        return getElement(this, by);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WebElement> findElements(final By by) {
        return getElements(this, by);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAttribute(String name) {
        try {
            return getWrappedElement().getAttribute(name);
        } catch (StaleElementReferenceException e) {
            return refreshReference(e).getAttribute(name);
        } catch (NullPointerException e) {
            throw deferredException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCssValue(String propertyName) {
        try {
            return getWrappedElement().getCssValue(propertyName);
        } catch (StaleElementReferenceException e) {
            return refreshReference(e).getCssValue(propertyName);
        } catch (NullPointerException e) {
            throw deferredException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Point getLocation() {
        try {
            return getWrappedElement().getLocation();
        } catch (StaleElementReferenceException e) {
            return refreshReference(e).getLocation();
        } catch (NullPointerException e) {
            throw deferredException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Rectangle getRect() {
        try {
            return getWrappedElement().getRect();
        } catch (StaleElementReferenceException e) {
            return refreshReference(e).getRect();
        } catch (NullPointerException e) {
            throw deferredException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getSize() {
        try {
            return getWrappedElement().getSize();
        } catch (StaleElementReferenceException e) {
            return refreshReference(e).getSize();
        } catch (NullPointerException e) {
            throw deferredException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTagName() {
        try {
            return getWrappedElement().getTagName();
        } catch (StaleElementReferenceException e) {
            return refreshReference(e).getTagName();
        } catch (NullPointerException e) {
            throw deferredException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getText() {
        try {
            return getWrappedElement().getText();
        } catch (StaleElementReferenceException e) {
            return refreshReference(e).getText();
        } catch (NullPointerException e) {
            throw deferredException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDisplayed() {
        try {
            return getWrappedElement().isDisplayed();
        } catch (StaleElementReferenceException e) {
            return refreshReference(e).isDisplayed();
        } catch (NullPointerException e) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        try {
            return getWrappedElement().isEnabled();
        } catch (StaleElementReferenceException e) {
            return refreshReference(e).isEnabled();
        } catch (NullPointerException e) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSelected() {
        try {
            return getWrappedElement().isSelected();
        } catch (StaleElementReferenceException e) {
            return refreshReference(e).isSelected();
        } catch (NullPointerException e) {
            throw deferredException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendKeys(CharSequence... keysToSend) {
        try {
            getWrappedElement().sendKeys(keysToSend);
        } catch (StaleElementReferenceException e) {
            refreshReference(e).sendKeys(keysToSend);
        } catch (NullPointerException e) {
            throw deferredException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void submit() {
        try {
            getWrappedElement().submit();
        } catch (StaleElementReferenceException e) {
            refreshReference(e).submit();
        } catch (NullPointerException e) {
            throw deferredException();
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
     * Determine if this robust element wraps a valid reference.
     * 
     * @return 'true' if reference was acquired; otherwise 'false'
     */
    public boolean hasReference() {
        if ((index == OPTIONAL) && (wrapped == null)) {
            acquireReference(this);
            return (null != wrapped);
        } else {
            return true;
        }
    }
    
    /**
     * Get the search context for this element.
     * 
     * @return element search context
     */
    public WrapsContext getContext() {
        return context;
    }
    
    /**
     * Get the locator for this element.
     * 
     * @return element locator
     */
    public By getLocator() {
        return locator;
    }
    
    /**
     * Get the element index.
     * <p>
     * <b>NOTE</b>: {@link #CARDINAL} = 1st matched reference; {@link #OPTIONAL} = an optional reference
     * 
     * @return element index (see NOTE)
     */
    public int getIndex() {
        return index;
    }
    
    /**
     * Refresh the wrapped element reference.
     * 
     * @param refreshTrigger {@link StaleElementReferenceException} that necessitates reference refresh
     * @return this robust web element with refreshed reference
     */
    WebElement refreshReference(final StaleElementReferenceException refreshTrigger) {
        try {
            WaitType.IMPLIED.getWait((SearchContext) context).until(referenceIsRefreshed(this));
            return this;
        } catch (TimeoutException e) {
            if (refreshTrigger == null) {
                throw UncheckedThrow.throwUnchecked(e.getCause());
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
     * @param element robust web element object
     * @return wrapped element reference (refreshed)
     */
    private static Coordinator<WebElement> referenceIsRefreshed(final OldRobustElement element) {
        return new Coordinator<WebElement>() {

            @Override
            public WebElement apply(SearchContext context) {
                try {
                    return acquireReference(element);
                } catch (StaleElementReferenceException e) {
                    ((WrapsContext) context).refreshContext(((WrapsContext) context).acquiredAt());
                    return acquireReference(element);
                }
            }

            @Override
            public String toString() {
                return "element reference to be refreshed";
            }
        };
        
    }
    
    /**
     * Acquire the element reference that's wrapped by the specified robust element.
     * 
     * @param element robust web element object
     * @return wrapped element reference
     */
    private static WebElement acquireReference(OldRobustElement element) {
        SearchContext context = element.context.getWrappedContext();
        
        if (element.strategy == Strategy.LOCATOR) {
            Timeouts timeouts = element.driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
            try {
                if (element.index > 0) {
                    List<WebElement> elements = context.findElements(element.locator);
                    if (element.index < elements.size()) {
                        element.wrapped = elements.get(element.index);
                    } else {
                        throw new NoSuchElementException(
                                String.format("Too few elements located %s: need: %d; have: %d", 
                                        element.locator, element.index + 1, elements.size()));
                    }
                } else {
                    element.wrapped = context.findElement(element.locator);
                }
            } catch (NoSuchElementException e) {
                if (element.index != OPTIONAL) {
                    throw e;
                }
                
                element.deferredException = e;
                element.wrapped = null;
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
            args.add(element.selector);
            
            if (element.strategy == Strategy.JS_XPATH) {
                js = LOCATE_BY_XPATH;
            } else {
                js = LOCATE_BY_CSS;
                args.add(element.index);
            }
            
            element.wrapped = JsUtility.runAndReturn(element.driver, js, args.toArray());
        }
        
        if (element.wrapped != null) {
            element.acquiredAt = System.currentTimeMillis();
            element.deferredException = null;
        }
        
        return element;
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
    public SearchContext refreshContext(Long expiration) {
        // refresh wrapped element reference if it's past the expiration
        return (expiration.compareTo(acquiredAt()) >= 0) ? refreshReference(null) : this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long acquiredAt() {
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
     * Get a wrapped reference to the first element matching the specified locator.
     * <p>
     * <b>NOTE</b>: Use {@link OldRobustElement#hasReference()} to determine if a valid reference was acquired.
     * 
     * @param by the locating mechanism
     * @return robust web element
     */
    public OldRobustElement findOptional(By by) {
        return OldRobustElement.getElement(this, by, OldRobustElement.OPTIONAL);
    }
    
    /**
     * Get the list of elements that match the specified locator in the indicated context.
     * 
     * @param context element search context
     * @param locator element locator
     * @return list of robust elements in context that match the locator
     */
    public static List<WebElement> getElements(WrapsContext context, By locator) {
        List<WebElement> elements;
        try {
            elements = context.getWrappedContext().findElements(locator);
            for (int index = 0; index < elements.size(); index++) {
                elements.set(index, new OldRobustElement(elements.get(index), context, locator, index));
            }
        } catch (StaleElementReferenceException e) {
            elements = context.refreshContext(context.acquiredAt()).findElements(locator);
        }
        return elements;
    }
    
    /**
     * Get the first element that matches the specified locator in the indicated context.
     * 
     * @param context element search context
     * @param locator element locator
     * @return robust element in context that matches the locator
     */
    public static OldRobustElement getElement(WrapsContext context, By locator) {
        return getElement(context, locator, CARDINAL);
    }
    
    /**
     * Get the item at the specified index in the list of elements matching the specified 
     * locator in the indicated context.
     * 
     * @param context element search context
     * @param locator element locator
     * @param index element index
     * @return indexed robust element in context that matches the locator
     */
    public static OldRobustElement getElement(WrapsContext context, By locator, int index) {
        return new OldRobustElement(null, context, locator, index);
    }
    
    /**
     * Throw the deferred exception that was stored upon failing to acquire the reference for an optional element.<br>
     * <p>
     * <b>NOTE</b>:
     * The deferred exception is not thrown directly - it's wrapped in a OptionalElementNotAcquiredException to
     * indicate that the failure was caused by utilizing an optional element for which no actual reference could be
     * acquired.
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
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OldRobustElement other = (OldRobustElement) obj;
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
    public String getId() {
        if (!hasIdentity) {
            throw new UnsupportedOperationException();
        }
        try {
            return ((HasIdentity) getWrappedElement()).getId();
        } catch (StaleElementReferenceException e) {
            return ((HasIdentity) refreshReference(e)).getId();
        } catch (NullPointerException e) {
            throw deferredException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebElement findElementByXPath(String xpath) {
        if (!findsByXPath) {
            throw new UnsupportedOperationException();
        }
        return findElement(By.xpath(xpath));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WebElement> findElementsByXPath(String xpath) {
        if (!findsByXPath) {
            throw new UnsupportedOperationException();
        }
        return findElements(By.xpath(xpath));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebElement findElementByTagName(String name) {
        By loc = By.tagName(name);
        if (!findsByTagName) {
            loc = getSupportedLocator(loc);
        }
        return findElement(loc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WebElement> findElementsByTagName(String name) {
        By loc = By.tagName(name);
        if (!findsByTagName) {
            loc = getSupportedLocator(loc);
        }
        return findElements(loc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebElement findElementByName(String name) {
        By loc = By.name(name);
        if (!findsByName) {
            loc = getSupportedLocator(loc);
        }
        return findElement(loc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WebElement> findElementsByName(String name) {
        By loc = By.name(name);
        if (!findsByName) {
            loc = getSupportedLocator(loc);
        }
        return findElements(loc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebElement findElementByLinkText(String linkText) {
        By loc = By.linkText(linkText);
        if (!findsByLinkText) {
            loc = getSupportedLocator(loc);
        }
        return findElement(loc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebElement findElementByPartialLinkText(String linkText) {
        By loc = By.partialLinkText(linkText);
        if (!findsByLinkText) {
            loc = getSupportedLocator(loc);
        }
        return findElement(loc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WebElement> findElementsByLinkText(String linkText) {
        By loc = By.linkText(linkText);
        if (!findsByLinkText) {
            loc = getSupportedLocator(loc);
        }
        return findElements(loc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WebElement> findElementsByPartialLinkText(String linkText) {
        By loc = By.partialLinkText(linkText);
        if (!findsByLinkText) {
            loc = getSupportedLocator(loc);
        }
        return findElements(loc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebElement findElementById(String id) {
        By loc = By.id(id);
        if (!findsById) {
            loc = getSupportedLocator(loc);
        }
        return findElement(loc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WebElement> findElementsById(String id) {
        By loc = By.id(id);
        if (!findsById) {
            loc = getSupportedLocator(loc);
        }
        return findElements(loc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebElement findElementByCssSelector(String sel) {
        By loc = By.cssSelector(sel);
        if (!findsByCssSelector) {
            loc = getSupportedLocator(loc);
        }
        return findElement(loc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WebElement> findElementsByCssSelector(String sel) {
        By loc = By.cssSelector(sel);
        if (!findsByCssSelector) {
            loc = getSupportedLocator(loc);
        }
        return findElements(loc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebElement findElementByClassName(String className) {
        By loc = By.className(className);
        if (!findsByClassName) {
            loc = getSupportedLocator(loc);
        }
        return findElement(loc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WebElement> findElementsByClassName(String className) {
        By loc = By.className(className);
        if (!findsByClassName) {
            loc = getSupportedLocator(loc);
        }
        return findElements(loc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Coordinates getCoordinates() {
        if (!locatable) {
            throw new UnsupportedOperationException();
        }
        try {
            return ((Locatable) getWrappedElement()).getCoordinates();
        } catch (StaleElementReferenceException e) {
            return ((Locatable) refreshReference(e)).getCoordinates();
        } catch (NullPointerException e) {
            throw deferredException();
        }
    }
    
    /**
     * Transform the specified locator to a type that's supported by the current driver.
     * 
     * @param loc unsupported locator
     * @return transformed locator of supported type
     * @throws UnsupportedOperationException if specified locator cannot be transformed
     */
    private By getSupportedLocator(By loc) {
        if (findsByCssSelector) {
            String sel = ByType.cssLocatorFor(loc);
            if (sel != null) {
                return By.cssSelector(sel);
            }
        }
        if (findsByXPath) {
            String xpath = ByType.xpathLocatorFor(loc);
            if (xpath != null) {
                return By.xpath(xpath);
            }
        }
        throw new UnsupportedOperationException();
    }
}
