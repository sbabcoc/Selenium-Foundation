package com.nordstrom.automation.selenium.core;

import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nordstrom.automation.selenium.model.ComponentContainer.ByEnum;

/**
 * This utility class defines static methods and interfaces related to Selenium {@link By} objects.
 */
public final class ByType {
    
    private static final String UNSUPPORTED_FOR_CSS = "Cannot get CSS locator string for '{}' locator";
    private static final String UNSUPPORTED_FOR_XPATH = "Cannot get XPath locator string for '{}' locator";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ByType.class);
    
    /**
     * Private constructor to prevent instantiation.
     */
    private ByType() {
        throw new AssertionError("ByType is a static utility class that cannot be instantiated");
    }
    
    /**
     * Get the CSS locator string that reproduces the specified locator constant
     * 
     * @param constant locator constant
     * @return CSS locator string; 'null' if unconvertible
     */
    public static String cssLocatorFor(final ByEnum constant) {
        return cssLocatorFor(constant.locator());
    }
    
    /**
     * Get the CSS locator string that reproduces the specified Selenium locator
     * 
     * @param locator Selenium locator
     * @return CSS locator string; 'null' if unconvertible
     */
    public static String cssLocatorFor(final By locator) {
        
        String val = valueOf(locator);
        
        if (locator instanceof By.ByClassName) {
            return "." + val;
        } else if (locator instanceof By.ByCssSelector) {
            return val;
        } else if (locator instanceof By.ById) {
            return "#" + val;
        } else if (locator instanceof By.ByLinkText) {
            LOGGER.warn(UNSUPPORTED_FOR_CSS, "ByLinkText");
        } else if (locator instanceof By.ByName) {
            return "[name=" + val + "]";
        } else if (locator instanceof By.ByPartialLinkText) {
            LOGGER.warn(UNSUPPORTED_FOR_CSS, "ByPartialLinkText");
        } else if (locator instanceof By.ByTagName) {
            return val;
        } else if (locator instanceof By.ByXPath) {
            LOGGER.warn(UNSUPPORTED_FOR_CSS, "ByXPath");
        }
        
        return null;
    }
    
    /**
     * Get the XPath locator string that reproduces the specified locator constant
     * 
     * @param constant locator constant
     * @return XPath locator string; 'null' if unconvertible
     */
    public static String xpathLocatorFor(final ByEnum constant) {
        return xpathLocatorFor(constant.locator());
    }
    
    /**
     * Get the XPath locator string that reproduces the specified Selenium locator
     * 
     * @param locator Selenium locator
     * @return XPath locator string; 'null' if unconvertible
     */
    public static String xpathLocatorFor(final By locator) {
        
        String val = valueOf(locator);
        
        if (locator instanceof By.ByClassName) {
            return ".//*[contains(concat(' ',@class,' '),' " + val + " ')]";
        } else if (locator instanceof By.ByCssSelector) {
            LOGGER.warn(UNSUPPORTED_FOR_XPATH, "ByCssSelector");
        } else if (locator instanceof By.ById) {
            return ".//*[@id='" + val + "']";
        } else if (locator instanceof By.ByLinkText) {
            return ".//a[.='" + val + "']";
        } else if (locator instanceof By.ByName) {
            return ".//*[@name='" + val + "']";
        } else if (locator instanceof By.ByPartialLinkText) {
            return ".//a[text()[contains(.,'" + val + "')]]";
        } else if (locator instanceof By.ByTagName) {
            return ".//" + val;
        } else if (locator instanceof By.ByXPath) {
            return val;
        }
        
        return null;
    }
    
    /**
     * Get the underlying value of the specified Selenium locator
     * 
     * @param locator Selenium locator
     * @return value extracted from the specified locator
     */
    private static String valueOf(final By locator) {
        String str = locator.toString();
        int i = str.indexOf(':');
        return str.substring(i + 1).trim();
    }
}
