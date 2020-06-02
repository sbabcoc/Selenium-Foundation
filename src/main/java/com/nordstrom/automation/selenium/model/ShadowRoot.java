package com.nordstrom.automation.selenium.model;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;

import com.nordstrom.automation.selenium.core.JsUtility;
import com.nordstrom.automation.selenium.exceptions.ShadowRootContextException;

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
        verifyShadowRoot();
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
        verifyShadowRoot();
    }
    
    /**
     * Constructor for shadow root by context element
     * 
     * @param element component context element
     * @param parent component parent
     */
    public ShadowRoot(final RobustWebElement element, final ComponentContainer parent) {
        super(element, parent);
        verifyShadowRoot();
    }
    
    /**
     * 
     */
    private void verifyShadowRoot() {
        if (null == getWrappedContext()) {
            throw new ShadowRootContextException();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public SearchContext getWrappedContext() {
        return JsUtility.runAndReturn(driver, SHADOW_ROOT, context);
    }
    
}
