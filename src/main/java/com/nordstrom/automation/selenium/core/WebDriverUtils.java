package com.nordstrom.automation.selenium.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.WrapsDriver;

import com.nordstrom.automation.selenium.model.RobustJavascriptExecutor;

/**
 * This static utility class contains a collection of methods primarily focused on extracting useful interfaces from
 * search contexts. It also includes a method that removes hidden elements from lists of elements.
 */
public final class WebDriverUtils {
    
    private static final List<Class<? extends WebDriverException>> reportableException =
                    Collections.unmodifiableList(
                            Arrays.asList(
                                    NotFoundException.class, ElementNotVisibleException.class,
                                    UnhandledAlertException.class, StaleElementReferenceException.class,
                                    TimeoutException.class));
    
    private static final Pattern frameworkPackage = Pattern.compile(
                    "^(?:sun\\.reflect|java\\.lang|"
                    + "org\\.(?:openqa|testng|junit|hamcrest)|"
                    + "com\\.nordstrom\\.automation\\.selenium)\\.");
            
    private WebDriverUtils() {
        throw new AssertionError("WebDriverUtils is a static utility class that cannot be instantiated");
    }
    
    /**
     * Get the driver associated with the specified search context
     * 
     * @param context search context
     * @return search context driver
     */
    public static WebDriver getDriver(SearchContext context) {
        if (context instanceof WebDriver) {
            return (WebDriver) context;
        } else if (context instanceof WrapsDriver) {
            return ((WrapsDriver) context).getWrappedDriver();
        } else {
            throw new UnsupportedOperationException("Unable to extract the driver from the specified context");
        }
    }
    
    /**
     * Get a JavaScript code executor for the specified search context
     * 
     * @param context search context
     * @return context-specific {@link JavascriptExecutor}
     */
    public static JavascriptExecutor getExecutor(SearchContext context) {
        WebDriver driver = getDriver(context);
        if (driver instanceof JavascriptExecutor) {
            return new RobustJavascriptExecutor(driver);
        } else {
            throw new UnsupportedOperationException("The specified context is unable to execute JavaScript");
        }
    }

    /**
     * Get the browser name for the specified context
     *  
     * @param context search context
     * @return context browser name
     */
    public static String getBrowserName(SearchContext context) {
        return getCapabilities(context).getBrowserName();
    }
    
    /**
     * Get the capabilities of the specified search context
     * 
     * @param context search context
     * @return context capabilities
     */
    public static Capabilities getCapabilities(SearchContext context) {
        WebDriver driver = getDriver(context);
        
        if (driver instanceof HasCapabilities) {
            return ((HasCapabilities) driver).getCapabilities();
        } else {
            throw new UnsupportedOperationException("The specified context is unable to describe its capabilities");
        }
    }

    /**
     * Remove hidden elements from specified list
     * 
     * @param elements list of elements
     * @return 'true' if no visible elements were found; otherwise 'false'
     */
    public static boolean filterHidden(List<WebElement> elements) {
        Iterator<WebElement> iter = elements.iterator();
        while (iter.hasNext()) {
            if ( ! iter.next().isDisplayed()) {
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
    public static Throwable getReportableCause(Throwable exception) {
        for (Throwable throwable = exception; throwable != null; throwable = throwable.getCause()) {
            Class<?> clazz = throwable.getClass();
            if (clazz == WebDriverException.class) {
                return throwable;
            }
            for (Class<? extends WebDriverException> reportable : reportableException) {
                if (reportable.isAssignableFrom(clazz)) {
                    return throwable;
                }
            }
        }
        return exception;
    }
    
    /**
     * Get the client code breakpoint from the stack trace of the specified exception.
     * 
     * @param exception exception whose stack trace is to be analyzed
     * @return first stack trace element that exists in client code; 'null' if unable to identify breakpoint
     */
    public static StackTraceElement getClientBreakpoint(Throwable exception) {
        if (exception != null) {
            for (StackTraceElement element : exception.getStackTrace()) {
                // if line number is unavailable
                if (element.getLineNumber() < 0) {
                    continue;
                }
                
                Matcher matcher = frameworkPackage.matcher(element.getClassName());
                
                // if not in framework code
                if (!matcher.lookingAt()) {
                    return element;
                }
            }
        }
        return null;
    }

}
