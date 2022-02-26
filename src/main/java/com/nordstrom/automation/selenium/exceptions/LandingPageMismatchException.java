package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.WebDriverException;

import com.nordstrom.automation.selenium.annotations.PageUrl;

/**
 * This exception is thrown when page load verification detects that the actual landing page differs from expectations.
 */
public class LandingPageMismatchException extends WebDriverException {

    private static final long serialVersionUID = -2375529252543798864L;
    
    /**
     * Constructor for pattern-based landing page mismatch exception
     * 
     * @param pageClass landing page class that defines the path/parameter pattern
     * @param url landing page URL that doesn't match the defined pattern
     */
    public LandingPageMismatchException(final Class<?> pageClass, final String url) {
        super(getMessage(pageClass, url));
    }
    
    /**
     * Constructor for property-based landing page mismatch exception
     * 
     * @param pageClass landing page class that defines the expected properties and values
     * @param propName name of property that doesn't match the expected value
     * @param actual actual value of the property that doesn't match
     * @param expect expected value of the property that doesn't match
     */
    public LandingPageMismatchException(
                    final Class<?> pageClass, final String propName, final String actual, final String expect) {
        super(getMessage(propName, pageClass, actual, expect));
    }
    
    /**
     * Assemble the message for pattern-based landing page mismatch exception
     * 
     * @param pageClass landing page class that defines the path/parameter pattern
     * @param url landing page URL that doesn't match the defined pattern
     * @return message for pattern-based landing page mismatch exception
     */
    private static String getMessage(final Class<?> pageClass, final String url) {
        return "Landing page for '" + pageClass.getSimpleName() + "' doesn't match expected pattern:\nactual: "
                + url + "\npattern: " + pageClass.getAnnotation(PageUrl.class).pattern();
    }
    
    /**
     * Assemble the message for property-based landing page mismatch exception
     * 
     * @param propName name of property that doesn't match the expected value
     * @param pageClass landing page class that defines the expected properties and values
     * @param actual actual value of the property that doesn't match
     * @param expect expected value of the property that doesn't match
     * @return message for property-based landing page mismatch exception
     */
    private static String getMessage(String propName, Class<?> pageClass, String actual, String expect) {
        return "Landing page for '" + pageClass.getSimpleName() + "' doesn't match expected property => " + propName
                + ":\nactual: " + actual + "\nexpected: " + expect;
    }
}
