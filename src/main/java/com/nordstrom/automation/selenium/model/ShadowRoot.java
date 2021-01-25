package com.nordstrom.automation.selenium.model;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;

import com.nordstrom.automation.selenium.core.JsUtility;
import com.nordstrom.automation.selenium.core.WebDriverUtils;
import com.nordstrom.automation.selenium.exceptions.ShadowRootContextException;

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

    /**
     * Constructor for shadow root by element locator
     * 
     * @param locator component context element locator
     * @param parent component parent container
     */
    public ShadowRoot(final By locator, final ComponentContainer parent) {
        super(locator, parent);
        getWrappedContext();
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
        getWrappedContext();
    }
    
    /**
     * Constructor for shadow root by context element
     * 
     * @param element component context element
     * @param parent component parent
     */
    public ShadowRoot(final RobustWebElement element, final ComponentContainer parent) {
        super(element, parent);
        getWrappedContext();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public SearchContext getWrappedContext() {
        return getShadowRoot(context);
    }
    
    /**
     * Get the underlying shadow root for the specified context.
     * 
     * @param context search context
     * @return shadow root context
     * @throws ShadowRootContextException if unable to acquire shadow root
     */
    public static SearchContext getShadowRoot(SearchContext context) {
        WebDriver driver = WebDriverUtils.getDriver(context);
        SearchContext shadowRoot = JsUtility.runAndReturn(driver, SHADOW_ROOT, context);
        if (shadowRoot != null) return shadowRoot;
        throw new ShadowRootContextException();
    }
    
}
