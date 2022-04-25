package com.nordstrom.automation.selenium.model;

import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import com.nordstrom.automation.selenium.core.JsUtility;
import com.nordstrom.automation.selenium.core.WebDriverUtils;
import com.nordstrom.automation.selenium.exceptions.ShadowRootContextException;
import com.nordstrom.automation.selenium.utility.SearchContextUtils;

/**
 * Extend this class when modeling a shadow root element, which is the attachment point for a shadow DOM.
 * <p>
 * This class defines three constructors:
 * <ol>
 *     <li>Create {@link #ShadowRoot(By, ComponentContainer) shadow root by locator}.</li>
 *     <li>Create {@link #ShadowRoot(By, int, ComponentContainer) shadow root by locator and index}.</li>
 *     <li>Create {@link #ShadowRoot(RobustWebElement, ComponentContainer) shadow root by host element}.</li>
 * </ol>
 * Your shadow root class can implement any of these constructors, but #3 ({@code shadow root by host element}) is
 * required if you wish to collect multiple instances in a {@link ComponentList} or {@link ComponentMap}. Also note
 * that you must override {@link #hashCode()} and {@link #equals(Object)} if you add significant fields.
 */
public class ShadowRoot extends PageComponent {
    
    private static final String SHADOW_ROOT = "return arguments[0].shadowRoot;";
    private static final String ROOT_KEY = "shadow-6066-11e4-a52e-4f735466cecf";

    /**
     * Constructor for shadow root by element locator
     * 
     * @param locator component context element locator
     * @param parent component parent container
     */
    public ShadowRoot(final By locator, final ComponentContainer parent) {
        super(locator, parent);
        SearchContextUtils.validateShadowHost(context);
    }
    
    /**
     * Constructor for shadow root by element locator and index
     * 
     * @param locator component context element locator
     * @param index component context index (-1 = non-indexed)
     * @param parent component parent container
     */
    public ShadowRoot(final By locator, final int index, final ComponentContainer parent) {
        super(locator, index, parent);
        SearchContextUtils.validateShadowHost(context);
    }
    
    /**
     * Constructor for shadow root by context element
     * 
     * @param element component context element
     * @param parent component parent
     */
    public ShadowRoot(final RobustWebElement element, final ComponentContainer parent) {
        super(element, parent);
        SearchContextUtils.validateShadowHost(context);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public SearchContext getWrappedContext() {
        return getShadowRoot(this);
    }
    
    /**
     * Get the underlying shadow root for the specified context.
     * 
     * @param context search context
     * @return shadow root context
     * @throws ShadowRootContextException if unable to acquire shadow root
     */
    @SuppressWarnings("unchecked")
    public static SearchContext getShadowRoot(final SearchContext context) {
        try {
            // invoke special handling for Firefox
            SearchContext shadowRoot = FirefoxShadowRoot.getShadowRoot(context);
            // if Firefox shadow root created, use it 
            if (shadowRoot != null) return shadowRoot;
        } catch (Exception eaten) {
            // nothing to do here
        }
        
        WebDriver driver = WebDriverUtils.getDriver(context);
        Object result = JsUtility.runAndReturn(driver, SHADOW_ROOT, context);
        // if shadow DOM context was acquired
        if (result instanceof SearchContext) {
            return (SearchContext) result;
        }
        
        // if response is W3C-compliant
        // https://github.com/SeleniumHQ/selenium/issues/10050
        if (result instanceof Map) {
            try {
                // build shadow root remote web element
                RemoteWebElement shadowRoot = new RemoteWebElement();
                shadowRoot.setParent((RemoteWebDriver) driver);
                shadowRoot.setId(((Map<String, String>) result).get(ROOT_KEY));
                shadowRoot.setFileDetector(((RemoteWebDriver) driver).getFileDetector());
                return shadowRoot;
            } catch (Exception eaten) {
                // nothing to do here
            }
        }
        
        throw new ShadowRootContextException();
    }
    
}
