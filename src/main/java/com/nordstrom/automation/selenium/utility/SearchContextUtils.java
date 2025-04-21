package com.nordstrom.automation.selenium.utility;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import com.nordstrom.automation.selenium.core.ByType;
import com.nordstrom.automation.selenium.core.JsUtility;
import com.nordstrom.automation.selenium.core.WebDriverUtils;
import com.nordstrom.automation.selenium.exceptions.ShadowRootContextException;
import com.nordstrom.automation.selenium.interfaces.WrapsContext;
import com.nordstrom.automation.selenium.model.ComponentContainer;
import com.nordstrom.automation.selenium.model.RobustWebElement;

/**
 * This utility class contains method for interacting with <b>Selenium Foundation</b> search contexts.
 */
public class SearchContextUtils {

    private static final String CHECK_SHADOW_HOST = JsUtility.getScriptResource("checkShadowHost.js");
    private static final String LOCATE_EVERY_BY_CSS = JsUtility.getScriptResource("locateEveryByCss.format");
    private static final String LOCATE_EVERY_BY_XPATH = JsUtility.getScriptResource("locateEveryByXpath.format");
    private static final String LOCATE_FIRST_BY_CSS = JsUtility.getScriptResource("locateFirstByCss.format");
    private static final String LOCATE_FIRST_BY_XPATH = JsUtility.getScriptResource("locateFirstByXpath.format");
    private static final String LOCATE_INDEX_BY_CSS = JsUtility.getScriptResource("locateIndexByCss.format");
    private static final String LOCATE_INDEX_BY_XPATH = JsUtility.getScriptResource("locateIndexByXpath.format");
    
    /**
     * This enumeration defines constants for JavaScript search context types.
     */
    public enum ContextType {
        /** document scope */
        DOCUMENT("document"),
        /** element scope */
        ELEMENT("arguments[0]"),
        /** shadow DOM scope */
        SHADOW("shadow");
        
        final String name;
        
        ContextType(String name) {
            this.name = name;
        }
    }
    
    /**
     * Private constructor to prevent instantiation.
     */
    private SearchContextUtils() {
        throw new AssertionError("SearchContextUtils is a static utility class that cannot be instantiated");
    }
    
    /**
     * Build a JavaScript to locate all elements matching the specified locator within contexts of the the same type
     * as the exemplar context.
     * 
     * @param context exemplar search context
     * @param locator locator for desired elements
     * @return JavaScript code string
     */
    public static String buildScriptToLocateElements(final WrapsContext context, final By locator) {
        return buildScriptToLocateElements(getContextType(context), locator);
    }
    
    /**
     * Build a JavaScript to locate all elements matching the specified locator within contexts of the specified type.
     * 
     * @param type context type: DOCUMENT, ELEMENT, or SHADOW
     * @param locator locator for the desired element
     * @return JavaScript code string
     */
    public static String buildScriptToLocateElements(final ContextType type, final By locator) {
        String format;
        String shadow = (type == ContextType.SHADOW) ? CHECK_SHADOW_HOST : "";
        
        String selector = quoteSelector(ByType.cssLocatorFor(locator));
        if (selector != null) {
            format = LOCATE_EVERY_BY_CSS;
        } else {
            selector = quoteSelector(ByType.xpathLocatorFor(locator));
            format = LOCATE_EVERY_BY_XPATH;
        }
        
        return String.format(shadow + format, type.name, selector);
    }
    
    /**
     * Build a JavaScript to locate the first element matching the specified locator within contexts of the the same
     * type as the exemplar context.
     * 
     * @param context exemplar search context
     * @param locator locator for desired elements
     * @param Index element index
     * @return JavaScript code string
     */
    public static String buildScriptToLocateElement(final WrapsContext context, final By locator, final int Index) {
        return buildScriptToLocateElement(getContextType(context), locator, Index);
    }
    
    /**
     * Build a JavaScript to locate the first element matching the specified locator within contexts of the specified
     * type.
     * 
     * @param type context type: DOCUMENT, ELEMENT, or SHADOW
     * @param locator locator for the desired element
     * @param index element index
     * @return JavaScript code string
     */
    public static String buildScriptToLocateElement(final ContextType type, final By locator, final int index) {
        boolean byCSS = true;
        String selector = quoteSelector(ByType.cssLocatorFor(locator));
        if (selector == null) {
            byCSS = false;
            selector = quoteSelector(ByType.xpathLocatorFor(locator));
        }
        
        String format;
        String shadow = (type == ContextType.SHADOW) ? CHECK_SHADOW_HOST : "";
        
        if (index > 0) {
            format = byCSS ? LOCATE_INDEX_BY_CSS : LOCATE_INDEX_BY_XPATH;
            return String.format(shadow + format, type.name, selector, index);
        } else {
            format = byCSS ? LOCATE_FIRST_BY_CSS : LOCATE_FIRST_BY_XPATH;
            return String.format(shadow + format, type.name, selector);
        }
    }
    
    /**
     * Quote the specified element selector, escaping embedded quotes as needed.
     * 
     * @param selector element selector (CSS or XPath)
     * @return quoted element selector
     */
    public static String quoteSelector(final String selector) {
        if (selector == null) return null;
        
        int quoteBits = 0;
        quoteBits |= (-1 != selector.indexOf("'")) ? 1 : 0;
        quoteBits |= (-1 != selector.indexOf("\"")) ? 2 : 0;
        
        switch (quoteBits) {
        case 2:
            return "'" + selector + "'";
            
        case 3:
            return "'" + selector.replaceAll("\"", "\\\"") + "'";
            
        default:
            return "\"" + selector + "\"";
        }
    }
    
    /**
     * Get the component container that holds the specified search context.
     * 
     * @param context target search context
     * @return containing {@link ComponentContainer}
     */
    public static ComponentContainer getContainingContext(final SearchContext context) {
        SearchContext unwrapped = unwrapContext(context);
        while (unwrapped != null) {
            if (unwrapped instanceof ComponentContainer) {
                return (ComponentContainer) unwrapped;
            }
            unwrapped = unwrapContext(unwrapped);
        }
        return null;
    }
    
    /**
     * Get the parent context for the specified search context.
     * 
     * @param context target search context
     * @return parent {@link SearchContext}
     */
    public static SearchContext unwrapContext(final SearchContext context) {
        if (context instanceof RobustWebElement) {
            return (SearchContext) ((RobustWebElement) context).getContext();
        }
        if (context instanceof WrapsContext) {
            return ((WrapsContext) context).getWrappedContext();
        }
        return null;
    }
    
    /**
     * Get the type constant for the specified context object.
     * 
     * @param context context object
     * @return {@link ContextType} constant
     */
    public static ContextType getContextType(final WrapsContext context) {
        return (context.getWrappedContext() instanceof WebElement) ? ContextType.ELEMENT : ContextType.DOCUMENT;
    }
    
    /**
     * Validate the specified search context as a {@code shadow host} element.
     * 
     * @param context search context to validate
     * @throws ShadowRootContextException if context isn't a valid shadow host
     */
    public static void validateShadowHost(final SearchContext context) {
        try {
            JsUtility.run(WebDriverUtils.getDriver(context), CHECK_SHADOW_HOST, context);
        } catch (WebDriverException e) {
            throw new ShadowRootContextException();
        }
    }
    
}
