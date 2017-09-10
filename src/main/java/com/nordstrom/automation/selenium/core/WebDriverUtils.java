package com.nordstrom.automation.selenium.core;

import java.util.Iterator;
import java.util.List;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.WrapsDriver;

import com.nordstrom.automation.selenium.model.RobustJavascriptExecutor;

/**
 * This static utility class contains a collection of methods primarily focused on extracting useful interfaces from
 * search contexts. It also includes a method that removes hidden elements from lists of elements.
 */
public final class WebDriverUtils {
    
    private WebDriverUtils() {
        throw new UnsupportedOperationException("WebDriverUtils is a static utility class that cannot be instantiated");
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

}
