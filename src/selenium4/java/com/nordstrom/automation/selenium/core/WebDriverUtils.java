package com.nordstrom.automation.selenium.core;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.Platform;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsDriver;
import com.nordstrom.automation.selenium.model.RobustJavascriptExecutor;

/**
 * This static utility class contains a collection of methods primarily focused
 * on extracting useful interfaces from search contexts. It also includes a
 * method that removes hidden elements from lists of elements.
 */
public final class WebDriverUtils {

    private static final List<Class<? extends WebDriverException>> REPORTABLE_EXCEPTION = Collections
            .unmodifiableList(Arrays.asList(NotFoundException.class, ElementNotInteractableException.class,
                    UnhandledAlertException.class, StaleElementReferenceException.class, TimeoutException.class));

    private static final Pattern FRAMEWORK_PACKAGE = Pattern.compile(
            "^(?:sun\\.reflect|java\\.lang"
            + "|org\\.(?:openqa|testng|junit|hamcrest)"
            + "|com\\.nordstrom\\.automation\\.selenium)\\.");

    /**
     * Private constructor to prevent instantiation.
     */
    private WebDriverUtils() {
        throw new AssertionError("WebDriverUtils is a static utility class that cannot be instantiated");
    }

    /**
     * Get the driver associated with the specified search context.
     * 
     * @param context search context
     * @return search context driver
     * @throws UnsupportedOperationException if unable to extract driver from
     *                                       specified context
     */
    public static WebDriver getDriver(final SearchContext context) {
        if (context instanceof WebDriver) {
            return (WebDriver) context;
        } else if (context instanceof WrapsDriver) {
            return ((WrapsDriver) context).getWrappedDriver();
        } else {
            throw new UnsupportedOperationException("Unable to extract the driver from the specified context");
        }
    }

    /**
     * Get a JavaScript code executor for the specified search context.
     * 
     * @param context search context
     * @return context-specific {@link JavascriptExecutor}
     * @throws UnsupportedOperationException if specified context doesn't support
     *                                       JavaScript
     */
    public static JavascriptExecutor getExecutor(final SearchContext context) {
        return new RobustJavascriptExecutor(getDriver(context));
    }

    /**
     * Determine if the specified context supports JavaScript.
     * 
     * @param context search context
     * @return {@code true} if the specified context supports JavaScript; otherwise
     *         {@code false}
     */
    public static boolean isJavascriptEnabled(final SearchContext context) {
        Capabilities caps = getCapabilities(context);
        if (caps != null) {
            Object javascriptEnabled = caps.getCapability("javascriptEnabled");
            return (javascriptEnabled == null) || caps.is("javascriptEnabled");
        }
        return false;
    }

    /**
     * Get the browser name for the specified context.
     * 
     * @param context search context
     * @return context browser name; "(unknown)" if context doesn't describe its
     *         capabilities
     */
    public static String getBrowserName(final SearchContext context) {
        Capabilities caps = getCapabilities(context);
        return (caps != null) ? caps.getBrowserName() : "(unknown)";
    }

    /**
     * Get the platform on which the browser is running.
     * 
     * @param context search context
     * @return {@link Platform} hosting the browser; {@code null} if context doesn't
     *         describe its capabilities
     */
    public Platform getPlatform(final SearchContext context) {
        Capabilities caps = getCapabilities(context);
        return (caps != null) ? caps.getPlatformName() : null;
    }

    /**
     * Get the capabilities of the specified search context
     * 
     * @param context search context
     * @return context capabilities; {@code null} if context doesn't describe its
     *         capabilities
     */
    public static Capabilities getCapabilities(final SearchContext context) {
        WebDriver driver = getDriver(context);
        return (driver instanceof HasCapabilities) ? ((HasCapabilities) driver).getCapabilities() : null;
    }

    /**
     * Get the value of the named DOM property of the specified WebElement.
     * 
     * @param element the target element
     * @param name the name of the property
     * @return the property's current value or {@code null} if the value is not set
     */
    public static String getDomPropertyOf(final WebElement element, final String name) {
        return element.getDomProperty(name);
    }
    
    /**
     * Get the value of the named DOM attribute of the specified WebElement.
     * 
     * @param element the target element
     * @param name the name of the attribute
     * @return the attribute's value or {@code null} if the value is not set
     */
    public static String getDomAttributeOf(final WebElement element, final String name) {
        return element.getDomAttribute(name);
    }
    
    /**
     * Specifies the amount of time the driver should wait when searching for an element if it is
     * not immediately present.
     *
     * <p>When searching for a single element, the driver should poll the page until the element has
     * been found, or this timeout expires before throwing a {@link NoSuchElementException}. When
     * searching for multiple elements, the driver should poll the page until at least one element
     * has been found or this timeout has expired.
     *
     * <p>Increasing the implicit wait timeout should be used judiciously as it will have an adverse
     * effect on test run time, especially when used with slower location strategies like XPath.
     *
     * <p>If the timeout is negative, not null, or greater than 2e16 - 1, an error code with invalid
     * argument will be returned.
     *
     * @param timeouts target {@link Timeouts} object
     * @param duration The duration to wait.
     * @return A self reference.
     */
    public static Timeouts implicitlyWait(final Timeouts timeouts, final Duration duration) {
        return timeouts.implicitlyWait(duration);
    }
    
    /**
     * Sets the amount of time to wait for a page load to complete before throwing an error. If the
     * timeout is negative, not null, or greater than 2e16 - 1, an error code with invalid argument
     * will be returned.
     *
     * @param timeouts target {@link Timeouts} object
     * @param duration The timeout value.
     * @return A Timeouts interface.
     * @see <a href="https://www.w3.org/TR/webdriver/#set-timeouts">W3C WebDriver</a>
     * @see <a href="https://www.w3.org/TR/webdriver/#dfn-timeouts-configuration">W3C WebDriver</a>
     */
    public static Timeouts pageLoadTimeout(final Timeouts timeouts, final Duration duration) {
        try {
            return timeouts.pageLoadTimeout(duration);
        } catch (WebDriverException eaten) {
            // unsupported
            return timeouts;
        }
    }
    
    /**
     * Remove hidden elements from specified list.
     * 
     * @param elements list of elements
     * @return {@code true} if no visible elements were found; otherwise
     *         {@code false}
     */
    public static boolean filterHidden(final List<WebElement> elements) {
        Iterator<WebElement> iter = elements.iterator();
        while (iter.hasNext()) {
            if (!iter.next().isDisplayed()) {
                iter.remove();
            }
        }
        return elements.isEmpty();
    }

    /**
     * Unwrap the specified exception to reveal its report-able cause.
     * 
     * @param exception exception to be unwrapped
     * @return report-able cause for the specified exception
     */
    public static Throwable getReportableCause(final Throwable exception) {
        for (Throwable throwable = exception; throwable != null; throwable = throwable.getCause()) {
            Class<?> clazz = throwable.getClass();
            if (clazz == WebDriverException.class) {
                return throwable;
            }
            for (Class<? extends WebDriverException> reportable : REPORTABLE_EXCEPTION) {
                if (reportable.isAssignableFrom(clazz)) {
                    return throwable;
                }
            }
        }
        return exception;
    }

    /**
     * Get the client code breakpoint from the stack trace of the specified
     * exception.
     * 
     * @param exception exception whose stack trace is to be analyzed
     * @return first stack trace element that exists in client code; {@code null} if
     *         unable to identify breakpoint
     */
    public static StackTraceElement getClientBreakpoint(final Throwable exception) {
        if (exception != null) {
            for (StackTraceElement element : exception.getStackTrace()) {
                // if line number is unavailable
                if (element.getLineNumber() < 0) {
                    continue;
                }

                Matcher matcher = FRAMEWORK_PACKAGE.matcher(element.getClassName());

                // if not in framework code
                if (!matcher.lookingAt()) {
                    return element;
                }
            }
        }
        return null;
    }

}
