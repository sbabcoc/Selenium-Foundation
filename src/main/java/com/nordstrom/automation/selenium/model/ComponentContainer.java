package com.nordstrom.automation.selenium.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.nordstrom.automation.selenium.AbstractSeleniumConfig.SeleniumSettings;
import com.nordstrom.automation.selenium.AbstractSeleniumConfig.WaitType;
import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.annotations.PageUrl;
import com.nordstrom.automation.selenium.core.WebDriverUtils;
import com.nordstrom.automation.selenium.exceptions.ContainerVacatedException;
import com.nordstrom.automation.selenium.exceptions.LandingPageMismatchException;
import com.nordstrom.automation.selenium.exceptions.PageNotLoadedException;
import com.nordstrom.automation.selenium.interfaces.WrapsContext;
import com.nordstrom.automation.selenium.model.Page.WindowState;
import com.nordstrom.automation.selenium.support.Coordinator;
import com.nordstrom.automation.selenium.support.SearchContextWait;
import com.nordstrom.common.base.UncheckedThrow;

/**
 * This is a abstract base class for all of the container classes defined by <b>Selenium Foundation</b>.
 */
public abstract class ComponentContainer
                        extends Enhanceable<ComponentContainer> 
                        implements SearchContext, WrapsContext {
    
    /**
     * This interface provides common methods for collections of Selenium locators ({@link By} objects)
     */
    public interface ByEnum {
        
        /**
         * Get the Selenium locator for this enumerated constant.
         * 
         * @return Selenium locator ({@link By} object) for this constant
         */
        By locator();
    }

    protected WebDriver driver;
    protected SearchContext context;
    protected ComponentContainer parent;
    protected ContainerVacatedException vacated;
    protected SearchContextWait wait;
    private List<Class<?>> bypassClasses;
    private List<String> bypassMethods;
    
    public static final By SELF = By.xpath(".");
    private static final String PLACEHOLDER = "{}";
    private static final Class<?>[] BYPASS_CLASSES = {WrapsContext.class};
    private static final String[] BYPASS_METHODS = {"validateParent", "getDriver", "getContext", "getParent",
            "getParentPage", "getWait", "switchTo", "switchToContext", "getVacated", "setVacated", "getArgumentTypes",
            "getArguments", "enhanceContainer", "myBypassClasses", "myBypassMethods", "getLogger", "hashCode",
            "equals"};
    
    private static final Class<?>[] ARG_TYPES = {SearchContext.class, ComponentContainer.class};
    private static final Class<?>[] COLLECTIBLE_ARGS = {RobustWebElement.class, ComponentContainer.class};
    private static final String ELEMENT_MESSAGE = "[element] must be non-null";
    private static final int PARAM_NAME_ONLY = 1;
    private static final int NAME_WITH_VALUE = 2;
    private static final String LOOPBACK = "http://127.0.0.1/";
    
    private final Logger logger;
    
    /**
     * Constructor for component container
     * 
     * @param context container search context
     * @param parent container parent (may be {@code null} for {@link Page} objects
     */
    public ComponentContainer(final SearchContext context, final ComponentContainer parent) {
        Objects.requireNonNull(context, "[context] must be non-null");
        validateParent(parent);
        
        this.context = context;
        this.driver = WebDriverUtils.getDriver(context);
        this.parent = parent;
        
        logger = LoggerFactory.getLogger(getContainerClass(this));
    }
    
    /**
     * Validate the specified parent object.
     * <p>
     * <b>NOTE</b>: This one-liner exists so the {@link Page} class can override it to eliminate the check. 
     * 
     * @param parent container parent
     */
    protected void validateParent(final ComponentContainer parent) {
        Objects.requireNonNull(parent, "[parent] must be non-null");
    }

    /**
     * Get the driver associated with this container.
     * 
     * @return container driver
     * @deprecated This method is redundant and is slated for removal. Use {@link #getWrappedDriver()} instead.
     */
    public WebDriver getDriver() {
        return driver;
    }
    
    /**
     * Get the container search context
     * 
     * @return container search context
     */
    public SearchContext getContext() {
        return context;
    }
    
    /**
     * Get the parent of this container
     * 
     * @return parent container
     */
    public ComponentContainer getParent() {
        return parent;
    }
    
    /**
     * Get the parent page for this container
     * 
     * @return container parent page
     */
    public Page getParentPage() {
        if (parent != null) {
            return parent.getParentPage();
        }
        return (Page) this;
    }
    
    /**
     * Convenience method to get a search context wait object for this container
     * 
     * @return {@link SearchContextWait} object with timeout specified by {@link SeleniumSettings#WAIT_TIMEOUT}
     */
    public SearchContextWait getWait() {
        if (wait == null) {
            wait = WaitType.WAIT.getWait(this);
        }
        return wait;
    }
    
    /**
     * Get SearchContextWait object with 15 second timeout
     * 
     * @param context search context
     * @return new SearchContextWait object
     */
    public static SearchContextWait getWait(SearchContext context) {
        return new SearchContextWait(context, WaitType.WAIT.getInterval());
    }
    
    /**
     * Wait until with specified condition is met
     * 
     * @param <T> return type of the specified condition
     * @param condition 'condition' function object
     * @return output of the specified condition
     */
    public <T> T waitUntil(Function<SearchContext, T> condition) {
        try {
            return getWait().until(condition);
        } catch (TimeoutException e) {
            if (e.getClass().equals(TimeoutException.class) && (condition instanceof Coordinator)) {
                TimeoutException d = ((Coordinator<T>) condition).differentiateTimeout(e);
                d.setStackTrace(e.getStackTrace());
                throw d;
            }
            throw e;
        }
    }
    
    /**
     * Convenience method to get a search context wait object of the specified type for this container
     * 
     * @param waitType wait type being requested
     * @return {@link SearchContextWait} object of the specified type for this container
     */
    public SearchContextWait getWait(final WaitType waitType) {
        return waitType.getWait(this);
    }
    
    /**
     * Switch focus to this container's search context.
     * <p>
     * <b>NOTE</b>: This method walks down the container lineage to the parent page object, then back up to this 
     * container, focusing the driver on each container as it goes.
     * 
     * @return this container's context
     */
    @Override
    public SearchContext switchTo() {
        return getWait().until(contextIsSwitched(this));
    }
    
    /**
     * Returns a 'wait' proxy that switches focus to the specified context
     * 
     * @param context search context on which to focus
     * @return target search context
     */
    static Coordinator<SearchContext> contextIsSwitched(final ComponentContainer context) {
        return new Coordinator<SearchContext>() {
            
            /**
             * {@inheritDoc}
             */
            @Override
            public SearchContext apply(final SearchContext ignore) {
                if (context.parent != null) {
                    context.parent.switchTo();
                }
                
                try {
                    return context.switchToContext();
                } catch (StaleElementReferenceException e) { //NOSONAR
                    return context.refreshContext(context.acquiredAt());
                }
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString() {
                return "context to be switched";
            }
        };
    }
    
    /**
     * Switch focus to this container's search context.
     * <p>
     * <b>NOTE</b>: This method walks down the container lineage to the parent page object, then back up to this 
     * container, focusing the driver on each container as it goes.
     * 
     * @return this container's context
     */
    protected abstract SearchContext switchToContext();
    
    /**
     * Get validity indication for this container and its ancestors.
     * 
     * @return {@link ContainerVacatedException} for vacated container; 'null' if container is still valid
     */
    ContainerVacatedException getVacated() {
        // if child container looks valid 
        if ((vacated == null) && (parent != null)) {
            // propagate ancestor validity
            vacated = parent.getVacated();
        }
        return vacated;
    }
    
    /**
     * Set the method that caused this container to be vacated.
     * 
     * @param vacated {@link ContainerVacatedException} for vacated container
     */
    void setVacated(final ContainerVacatedException vacated) {
        this.vacated = vacated;
        // if has ancestor
        if (parent != null) {
            // propagate to ancestor
            parent.setVacated(vacated);
        }
    }
    
    /**
     * Find all elements within the current context using the given locator constant.
     * 
     * @param constant the locator constant
     * @return a list of all WebElements, or an empty list if nothing matches
     */
    public List<WebElement> findElements(final ByEnum constant) {
        return findElements(constant.locator());
    }
    
    /**
     * Find all elements within the current context using the given mechanism.
     * 
     * @param by the locating mechanism
     * @return a list of all WebElements, or an empty list if nothing matches
     */
    @Override
    public List<WebElement> findElements(final By by) {
        return RobustElementFactory.getElements(this, by);
    }
    
    /**
     * Find the first WebElement using the given locator constant.
     * 
     * @param constant the locator constant
     * @return the first matching element on the current context
     */
    public WebElement findElement(final ByEnum constant) {
        return findElement(constant.locator());
    }
    
    /**
     * Find the first WebElement using the given method.
     * 
     * @param by the locating mechanism
     * @return the first matching element on the current context
     */
    @Override
    public WebElement findElement(final By by) {
        return RobustElementFactory.getElement(this, by);
    }
    
    /**
     * Get a wrapped reference to the first element matching the specified locator constant.
     * <p>
     * <b>NOTE</b>: Use {@link RobustWebElement#hasReference()} to determine if a valid reference was acquired.
     * 
     * @param constant the locator constant
     * @return robust web element
     */
    public RobustWebElement findOptional(final ByEnum constant) {
        return findOptional(constant.locator());
    }
    
    /**
     * Get a wrapped reference to the first element matching the specified locator.
     * <p>
     * <b>NOTE</b>: Use {@link RobustWebElement#hasReference()} to determine if a valid reference was acquired.
     * 
     * @param by the locating mechanism
     * @return robust web element
     */
    public RobustWebElement findOptional(final By by) {
        return (RobustWebElement) RobustElementFactory.getElement(this, by, RobustElementWrapper.OPTIONAL);
    }
    
    /**
     * Get the driver object associated with this container.
     * 
     * @return container driver object
     */
    @Override
    public WebDriver getWrappedDriver() {
        return driver;
    }
    
    /**
     * Update the specified element with the indicated value
     * 
     * @param element target element (checkbox)
     * @param value desired value
     * @return 'true' if element value changed; otherwise 'false'
     */
    public static boolean updateValue(final WebElement element, final boolean value) {
        Objects.requireNonNull(element, ELEMENT_MESSAGE);
        
        String tagName = element.getTagName().toLowerCase();
        if ("input".equals(tagName) && "checkbox".equals(element.getAttribute("type"))) {
            if (element.isSelected() != value) {
                element.click();
                return true;
            } else {
                return false;
            }
        }
        
        return updateValue(element, Boolean.toString(value));
    }
    
    /**
     * Update the specified element with the indicated value
     * 
     * @param element target element (input, select)
     * @param value desired value
     * @return 'true' if element value changed; otherwise 'false'
     */
    public static boolean updateValue(final WebElement element, final String value) {
        Objects.requireNonNull(element, ELEMENT_MESSAGE);
        
        String tagName = element.getTagName().toLowerCase();
        if ("input".equals(tagName)) {
            if ("checkbox".equals(element.getAttribute("type"))) {
                return updateValue(element, Boolean.parseBoolean(value));
            } else if (!valueEquals(element, value)) {
                if (value == null) {
                    element.clear();
                } else {
                    WebDriverUtils.getExecutor(element).executeScript("arguments[0].select();", element);
                    element.sendKeys(value);
                }
                return true;
            }
        } else if ("select".equals(tagName) && !valueEquals(element, value)) {
            new Select(element).selectByValue(value);
            return true;
        }
        return false;
    }
    
    /**
     * Determine if the specified element has the desired value.
     * 
     * @param element target element (input, select)
     * @param value desired value
     * @return 'true' if element has the desired value; otherwise 'false'
     */
    private static boolean valueEquals(final WebElement element, final String value) {
        Objects.requireNonNull(element, ELEMENT_MESSAGE);
        
        String exist = element.getAttribute("value");
        return (exist != null) ? exist.equals(value) : (value == null);
    }
    
    /**
     * Scroll the specified element into view
     * 
     * @param element target element
     * @return the specified element
     */
    public static WebElement scrollIntoView(final WebElement element) {
        WebDriverUtils.getExecutor(element).executeScript("arguments[0].scrollIntoView(true);", element);
        return element;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    Class<?>[] getArgumentTypes() {
        return Arrays.copyOf(ARG_TYPES, ARG_TYPES.length);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    Object[] getArguments() {
        return new Object[] {context, parent};
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Class<?>> getBypassClasses() {
        if (bypassClasses == null) {
            bypassClasses = super.getBypassClasses();
            Collections.addAll(bypassClasses, myBypassClasses());
        }
        return Collections.unmodifiableList(bypassClasses);
    }
    
    /**
     * Returns an array of classes whose methods should not be intercepted
     * 
     * @return array of bypass classes
     */
    Class<?>[] myBypassClasses() {
        return Arrays.copyOf(BYPASS_CLASSES, BYPASS_CLASSES.length);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> getBypassMethods() {
        if (bypassMethods == null) {
            bypassMethods = super.getBypassMethods();
            Collections.addAll(bypassMethods, myBypassMethods());
        }
        return Collections.unmodifiableList(bypassMethods);
    }
    
    /**
     * Returns an array of names for methods that should not be intercepted
     * 
     * @return array of bypass method names
     */
    String[] myBypassMethods() {
        return Arrays.copyOf(BYPASS_METHODS, BYPASS_METHODS.length);
    }
    
    /**
     * Get the logger for this container
     * 
     * @return logger object
     */
    protected Logger getLogger() {
        return logger;
    }
    
    /**
     * Open the page defined by the {@link PageUrl} annotation of the specified page class.
     * 
     * @param <T> page class
     * @param pageClass type of page object to instantiate
     * @param newWindow 'true' to open page in new window; 'false' to open page in current window
     * @return new instance of the specified page class
     */
    public <T extends Page> T openAnnotatedPage(final Class<T> pageClass, final boolean newWindow) {
        PageUrl pageUrl = pageClass.getAnnotation(PageUrl.class);
        String url = getPageUrl(pageUrl, SeleniumConfig.getConfig().getTargetUri());
        Objects.requireNonNull(url, pageClass.toString() 
                        + " has no @PageUrl annotation, or the specified @PageUrl has no value");
        return openPageAtUrl(pageClass, url, newWindow);
    }
    
    /**
     * Open the specified relative path under the current target URI.
     * 
     * @param <T> page class
     * @param pageClass type of page object to instantiate
     * @param path path to open
     * @param newWindow 'true' to open page in new window; 'false' to open page in current window
     * @return new instance of the specified page class
     */
    public <T extends Page> T openPageAtPath(final Class<T> pageClass, final String path, final boolean newWindow) {
        URIBuilder builder = new URIBuilder(SeleniumConfig.getConfig().getTargetUri());
        builder.setPath(URI.create(LOOPBACK + builder.getPath() + "/").resolve("./" + path).getPath());
        return openPageAtUrl(pageClass, builder.toString(), newWindow);
    }
    
    /**
     * Open the specified URL.
     * 
     * @param <T> page class
     * @param pageClass type of page object to instantiate
     * @param url URL to open
     * @param newWindow 'true' to open page in new window; 'false' to open page in current window
     * @return new instance of the specified page class
     */
    public <T extends Page> T openPageAtUrl(final Class<T> pageClass, final String url, final boolean newWindow) {
        Objects.requireNonNull(pageClass, "[pageClass] must be non-null");
        Objects.requireNonNull(url, "[url] must be non-null");
        
        T pageObj = Page.newPage(pageClass, driver);
        if (newWindow) {
            pageObj.setWindowState(WindowState.WILL_OPEN);
            WebDriverUtils.getExecutor(driver).executeScript("window.open('" + url + "','_blank');");
        } else {
            driver.get(url);
        }
        return pageObj;
    }
    
    /**
     * Get the URL defined by the specified {@link PageUrl} annotation.
     * <p>
     * <b>NOTES</b>: <ul>
     *     <li>If the {@code pageUrl} argument is {@code null} or the {@code value} element of the specified
     *         {@link PageUrl} annotation is unspecified, this method returns {@code null}.
     *     <li>If {@code scheme} of the specified {@code pageUrl} argument is unspecified or set to {@code http/https},
     *         the specified {@code targetUri} is overlaid by the elements of the {@link PageUrl} annotation to
     *         produce the fully-qualified <b>HTTP</b> target page URL.<ul>
     *         <li>If the {@code value} element specifies an absolute path, this path is returned as-is.</li>
     *         <li>If the {@code value} element specifies a relative path, this is appended to the path specified by
     *             {@code targetUri} to resolve the page URL.</li>
     *         <li>If the {@code scheme} element is specified, its value overrides the scheme of {@code targetUri}.
     *             If the value of the {@code scheme} element is empty, the scheme of {@code targetUri} is set to
     *             {@code null}.</li>
     *         <li>If the {@code userInfo} element is specified, its value overrides the userInfo of {@code targetUrl}.
     *             If the value of the {@code userInfo} element is empty, the userInfo of {@code targetUri} is set to
     *             {@code null}.</li>
     *         <li>If the {@code host} element is specified, its value overrides the host of {@code targetUrl}. If the
     *             value of the {@code host} element is empty, the host of {@code targetUri} is set to {@code null}.
     *             </li>
     *         <li>If the {@code port} element is specified, its value overrides the port of {@code targetUri}. If the
     *             value of the {@code port} element is empty, the port of {@code targetUri} is set to <b>0</b>.</li>
     *     </ul></li>
     *     <li>For <b>HTTP</b> URLs that require query parameters, these parameters must be included in the
     *         {@code value} element of the specified {@link PageUrl} annotation. The {@code params} element of the
     *         annotation is only used for pattern-based landing page verification.</li>
     *     <li>If {@code scheme} of the specified {@code pageUrl} is set to {@code file}, the value of the
     *         {@code targetUri} argument is ignored. The only element of the {@link PageUrl} annotation that
     *         is used to produce the fully-qualified <b>FILE</b> target page URL is {@code value}. The value of the
     *         {@code value} element specifies the relative path of a file within your project's resources, which is
     *         resolved via {@link ClassLoader#getResource}.</li>
     * </ul>
     * 
     * @param pageUrl page URL annotation
     * @param targetUri target URI
     * @return defined page URL as a string (may be 'null')
     */
    public static String getPageUrl(final PageUrl pageUrl, final URI targetUri) {
        if (pageUrl == null || PLACEHOLDER.equals(pageUrl.value())) {
            return null;
        }
        
        String result = null;
        String scheme = pageUrl.scheme();
        String path = pageUrl.value();
        
        if ("file".equals(scheme)) {
            result = Thread.currentThread().getContextClassLoader().getResource(path).toString();
        } else {
            Objects.requireNonNull(targetUri, "[targetUri] must be non-null");
            
            String userInfo = pageUrl.userInfo();
            String host = pageUrl.host();
            String port = pageUrl.port();
            
            URIBuilder builder = new URIBuilder(targetUri);
            
            if (!path.isEmpty()) {
                URI pathUri = URI.create(path);
                if (pathUri.isAbsolute()) {
                    return pathUri.toString();
                } else {
                    builder.setPath(URI.create(LOOPBACK + targetUri.getPath() + "/").resolve("./" + path).getPath());
                }
            }
            
            if (!PLACEHOLDER.equals(scheme)) {
                builder.setScheme(scheme.isEmpty() ? null : scheme);
            }
            
            if (!PLACEHOLDER.equals(userInfo)) {
                builder.setUserInfo(userInfo.isEmpty() ? null : userInfo);
            }
            
            if (!PLACEHOLDER.equals(host)) {
                builder.setHost(host.isEmpty() ? null : host);
            }
            
            if (!PLACEHOLDER.equals(port)) {
                builder.setPort(port.isEmpty() ? 0 : Integer.parseInt(port));
            }
            
            result = builder.toString();
        }
        
        return result;
    }
    
    /**
     * Wait for the expected landing page to appear in the target browser window.
     * 
     * @param pageObj target page object
     */
    static void waitForLandingPage(final Page pageObj) {
        SearchContextWait wait = (SearchContextWait)
                pageObj.getWait(WaitType.PAGE_LOAD).ignoring(LandingPageMismatchException.class);
        wait.until(landingPageAppears());
    }
    
    /**
     * Returns a 'wait' proxy that determines if the expected landing page has appeared.
     * 
     * @return 'true' if the expected landing page has appeared
     */
    private static Coordinator<Boolean> landingPageAppears() {
        return new Coordinator<Boolean>() {
            
            /**
             * {@inheritDoc}
             */
            @Override
            public Boolean apply(final SearchContext context) {
                ContainerMethodInterceptor.scanForErrors(context);
                verifyLandingPage((Page) context);
                return Boolean.TRUE;
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString() {
                return "expected landing page to appear";
            }
        };
    }
    
    /**
     * Verify actual landing page against elements of the {@link PageUrl} annotation of the specified page object.
     * <p>
     * <b>NOTES</b>: <ul>
     *     <li>The values and patterns used to verify the actual landing page URL are provided by the {@link PageUrl}
     *         annotation of the specified page object combined with the configured {@link SeleniumConfig#getTargetUri
     *         target URI}.</li>
     *     <li>Expected path can be specified by either explicit value or pattern. If the {@code pattern} element of
     *         the {@link PageUrl} annotation is specified, its value provides a template to verify the actual path.
     *         Otherwise, the actual path must match the path component of the specified {@code value} element of the
     *         {@link PageUrl} annotation.</li>
     *     <li>Expected parameters can be specified by either explicit query or a collection of name/pattern pairs.
     *         If the {@code params} element of the {@link PageUrl} annotation is specified, its value provides the
     *         collection of name/pattern pairs used to verify the actual parameters. Otherwise, the actual query
     *         parameters must include all of the name/value pairs in the query component of the specified {@code
     *         value} element of the {@link PageUrl} annotation.</li>
     * </ul>
     * 
     * @param pageObj page object whose landing page is to be verified
     */
    private static void verifyLandingPage(final Page pageObj) {
        Class<?> pageClass = getContainerClass(pageObj);
        PageUrl pageUrl = pageClass.getAnnotation(PageUrl.class);
        if (pageUrl != null) {
            URI targetUri = SeleniumConfig.getConfig().getTargetUri();
            verifyLandingPage(pageObj, pageClass, pageUrl, targetUri);
        }
    }

    /**
     * <b>INTERNAL</b>: Verify actual landing page against elements of the {@link PageUrl} annotation of the specified
     * page object.
     * 
     * @param pageObj page object whose landing page is to be verified
     * @param pageClass class of the specified page object
     * @param pageUrl {@link PageUrl} annotation for the indicate page class
     * @param targetUri configured target URI
     */
    @SuppressWarnings("deprecation")
    protected static final void verifyLandingPage(final Page pageObj, Class<?> pageClass, PageUrl pageUrl, URI targetUri) {
        String actual;
        String expect;
        
        URI actualUri = URI.create(pageObj.getCurrentUrl());
        String expectUrl = getPageUrl(pageUrl, targetUri);
        URI expectUri = (expectUrl != null) ? URI.create(expectUrl) : null;
        if (expectUri != null) {
            actual = actualUri.getScheme();
            expect = expectUri.getScheme();
            if ( ! StringUtils.equals(actual, expect)) {
                throw new LandingPageMismatchException(pageClass, "scheme", actual, expect);
            }
            
            actual = actualUri.getHost();
            expect = expectUri.getHost();
            if ( ! StringUtils.equals(actual, expect)) {
                throw new LandingPageMismatchException(pageClass, "host", actual, expect);
            }
            
            actual = actualUri.getUserInfo();
            expect = expectUri.getUserInfo();
            if ( ! StringUtils.equals(actual, expect)) {
                throw new LandingPageMismatchException(pageClass, "user info", actual, expect);
            }
            
            actual = Integer.toString(actualUri.getPort());
            expect = Integer.toString(expectUri.getPort());
            if ( ! StringUtils.equals(actual, expect)) {
                throw new LandingPageMismatchException(pageClass, "port", actual, expect);
            }
        }
        
        String pattern = pageUrl.pattern();
        if (!PLACEHOLDER.equals(pattern)) {
            actual = actualUri.getPath();
            String target = targetUri.getPath();
            if (StringUtils.isNotBlank(target)) {
                int actualLen = actual.length();
                int targetLen = target.length();
                
                if ((actualLen > targetLen) && (actual.startsWith(target))) {
                    actual = actual.substring(targetLen);
                } else {
                    throw new LandingPageMismatchException(pageClass, "base path", actual, target);
                }
            }
            
            if ( ! actual.matches(pattern)) {
                throw new LandingPageMismatchException(pageClass, pageObj.getCurrentUrl());
            }
        } else if (expectUri != null) {
            actual = actualUri.getPath();
            expect = expectUri.getPath();
            if ( ! StringUtils.equals(actual, expect)) {
                throw new LandingPageMismatchException(pageClass, "path", actual, expect);
            }
        }
        
        List<NameValuePair> actualParams = URLEncodedUtils.parse(actualUri, "UTF-8");
        
        for (NameValuePair expectPair : getExpectedParams(pageUrl, expectUri)) {
            if (!hasExpectedParam(actualParams, expectPair)) {
                throw new LandingPageMismatchException(
                                pageClass, "query parameter", actualUri.getQuery(), expectPair.toString());
            }
        }
    }

    /**
     * Check the specified page-load condition to determine if this condition has been met.<br>
     * NOTE - This method indicates failure to meet the condition by throwing {@link PageNotLoadedException}.
     * 
     * @param <T> coordinator type parameter
     * @param condition expected page-load condition
     * @param message the detail message for the {@link PageNotLoadedException} thrown if the condition isn't met
     * @return result from the {@link Function#apply(Object) apply} method of the specified coordinator
     */
    public <T> T checkPageLoadCondition(final Coordinator<T> condition, final String message) {
        T result = null;
        Throwable cause = null;
        try {
            result = condition.apply(getContext());
        } catch (RuntimeException t) {
            cause = t;
        }
        if (cause != null) {
            throw new PageNotLoadedException(message, cause);
        } else if (result == null || result == Boolean.FALSE) {
            throw new PageNotLoadedException(message);
        }
        return result;
    }
    
    /**
     * Get list of expected query parameters.
     * 
     * @param pageUrl page URL annotation
     * @param expectUri expected landing page URI
     * @return list of expected query parameters
     */
    @SuppressWarnings("deprecation")
    private static List<NameValuePair> getExpectedParams(final PageUrl pageUrl, final URI expectUri) {
        List<NameValuePair> expectParams = new ArrayList<>();
        String[] params = pageUrl.params();
        if (params.length > 0) {
            for (String param : params) {
                String name = null;
                String value = null;
                String[] nameValueBits = param.split("=");
                switch (nameValueBits.length) {
                    case NAME_WITH_VALUE:
                        value = nameValueBits[1].trim();
                        
                    case PARAM_NAME_ONLY:
                        name = nameValueBits[0].trim();
                        expectParams.add(new BasicNameValuePair(name, value));
                        break;
                        
                    default:
                        throw new IllegalArgumentException("Format of PageUrl parameter '" + param
                            + "' does not conform to template [name] or [name]=[pattern]");
                }
            }
        } else if (expectUri != null) {
            expectParams = URLEncodedUtils.parse(expectUri, "UTF-8");
        }
        return expectParams;
    }
    
    /**
     * Determine if actual query parameters include all expected name/value pairs.
     * 
     * @param actualParams actual query parameters of landing page
     * @param expectPair expected query parameters
     * @return 'true' of actual query parameters include all expected name/value pairs; otherwise 'false'
     */
    private static boolean hasExpectedParam(final List<NameValuePair> actualParams, final NameValuePair expectPair) {
        Iterator<NameValuePair> iterator = actualParams.iterator();
        while (iterator.hasNext()) {
            NameValuePair actualPair = iterator.next();
            if ( ! actualPair.getName().equals(expectPair.getName())) {
                continue;
            }
            
            String actualValue = actualPair.getValue();
            String expectValue = expectPair.getValue();
            
            if ((actualValue == null) ^ (expectValue == null)) {
                continue;
            }
            
            if ((actualValue == null) || (actualValue.matches(expectValue))) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get {@link Method} object for the static {@code getKey(SearchContext)} method declared by the specified
     * container type.
     * 
     * @param <T> component container type
     * @param containerType target container type
     * @return method object for getKey(SearchContext) 
     * @throws UnsupportedOperationException The required method is missing
     */
    static <T extends ComponentContainer> Method getKeyMethod(final Class<T> containerType) {
        try {
            Method method = containerType.getMethod("getKey", SearchContext.class);
            if (Modifier.isStatic(method.getModifiers())) {
                return method;
            }
        } catch (NoSuchMethodException e) { //NOSONAR
            // fall through to 'throw' statement below
        }
        throw new UnsupportedOperationException(
                "Container class must declare method: public static Object getKey(SearchContext)");
    }
    
    /**
     * Verify that the specified container type declares the required constructor.
     * 
     * @param <T> component container type
     * @param containerType target container type
     * @throws UnsupportedOperationException The required constructor is missing
     */
    static <T extends ComponentContainer> void verifyCollectible(final Class<T> containerType) {
        try {
            containerType.getConstructor(COLLECTIBLE_ARGS);
        } catch (NoSuchMethodException | SecurityException e) { //NOSONAR
            String format = 
                    "Container class must declare constructor: public %s(RobustWebElement, ComponentContainer)";
            throw new UnsupportedOperationException(String.format(format, containerType.getSimpleName()));
        }
    }
    
    /**
     * Get the types of the arguments used to instantiate collectible containers.
     * 
     * @return an array of constructor argument types
     */
    static Class<?>[] getCollectibleArgs() {
        return Arrays.copyOf(COLLECTIBLE_ARGS, COLLECTIBLE_ARGS.length);
    }
    
    /**
     * Instantiate a new container of the specified type with the supplied arguments.
     * 
     * @param <T> component container type
     * @param containerType type of container to instantiate
     * @param argumentTypes array of constructor argument types
     * @param arguments array of constructor argument values
     * @return new container of the specified type
     */
    public static <T extends ComponentContainer> T newContainer(
                    final Class<T> containerType, final Class<?>[] argumentTypes, final Object... arguments) {
        try {
            Constructor<T> ctor = containerType.getConstructor(argumentTypes);
            return ctor.newInstance(arguments);
        } catch (InvocationTargetException e) { //NOSONAR
            throw UncheckedThrow.throwUnchecked(e.getCause());
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException |
                NoSuchMethodException | InstantiationException e) {
            throw UncheckedThrow.throwUnchecked(e);
        }
    }
    
    /**
     * Instantiate a list of page components of the specified type.<br>
     * <b>NOTE</b>: The specified page component class must declare a constructor with arguments
     * (RobustWebElement, ComponentContainer).
     * 
     * @param <T> page component type
     * @param componentType page component type
     * @param locator locator for page component container elements
     * @return list of page components
     * @see #verifyCollectible
     */
    public <T extends PageComponent> List<T> newComponentList(final Class<T> componentType, final By locator) {
        return new ComponentList<>(this, componentType, locator);
    }
    
    /**
     * Instantiate a map of page components of the specified type, using self-generated keys.<br>
     * <b>NOTE</b>: The specified page component class must declare a constructor with arguments
     * (RobustWebElement, ComponentContainer).<br>
     * <b>NOTE</b>: The specified page component class must declare a static {@code getKey} method that generates a
     * unique key for each map entry.
     * 
     * @param <T> page component type
     * @param componentType page component type
     * @param locator locator for page component container elements
     * @return map of page components
     * @see #verifyCollectible
     * @see #getKeyMethod
     */
    public <T extends PageComponent> Map<Object, T> newComponentMap(final Class<T> componentType, final By locator) {
        return new ComponentMap<>(this, componentType, locator);
    }
    
    /**
     * Instantiate a list of frames of the specified type.<br>
     * <b>NOTE</b>: The specified frame class must declare a constructor with arguments
     * (RobustWebElement, ComponentContainer).
     * 
     * @param <T> frame type
     * @param frameType frame type
     * @param locator locator for frame container elements
     * @return list of frames
     * @see #verifyCollectible
     */
    public <T extends Frame> List<T> newFrameList(final Class<T> frameType, final By locator) {
        return new FrameList<>(this, frameType, locator);
    }
    
    /**
     * Instantiate a map of frames of the specified type, using self-generated keys.<br>
     * <b>NOTE</b>: The specified frame class must declare a constructor with arguments
     * (RobustWebElement, ComponentContainer).<br>
     * <b>NOTE</b>: The specified frame class must declare a static {@code getKey} method that generates a
     * unique key for each map entry.
     * 
     * @param <T> frame type
     * @param frameType frame type
     * @param locator locator for frame container elements
     * @return map of frames
     * @see #verifyCollectible
     * @see #getKeyMethod
     */
    public <T extends Frame> Map<Object, T> newFrameMap(final Class<T> frameType, final By locator) {
        return new FrameMap<>(this, frameType, locator);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + context.hashCode();
        result = prime * result + ((parent == null) ? 0 : parent.hashCode());
        result = prime * result + ((bypassClasses == null) ? 0 : bypassClasses.hashCode());
        result = prime * result + ((bypassMethods == null) ? 0 : bypassMethods.hashCode());
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
        if (getClass() != obj.getClass())
            return false;
        ComponentContainer other = (ComponentContainer) obj;
        if (!context.equals(other.context))
            return false;
        if (parent == null) {
            if (other.parent != null)
                return false;
        } else if (!parent.equals(other.parent))
            return false;
        if (bypassClasses == null) {
            if (other.bypassClasses != null)
                return false;
        } else if (!bypassClasses.equals(other.bypassClasses))
            return false;
        if (bypassMethods == null) {
            if (other.bypassMethods != null)
                return false;
        } else if (!bypassMethods.equals(other.bypassMethods))
            return false;
        return true;
    }
}
