package com.nordstrom.automation.selenium.examples;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import com.nordstrom.automation.selenium.core.JsUtility;
import com.nordstrom.automation.selenium.model.ComponentContainer;
import com.nordstrom.automation.selenium.model.PageComponent;
import com.nordstrom.automation.selenium.model.RobustWebElement;

/**
 * This class is the model for the example page form component.
 */
public class FormComponent extends PageComponent {

    /**
     * Constructor for page component by element locator
     * 
     * @param locator component context element locator
     * @param parent component parent container
     */
    public FormComponent(By locator, ComponentContainer parent) {
        super(locator, parent);
    }
    
    /**
     * Constructor for page component by context element
     * 
     * @param element component context element
     * @param parent component parent
     */
    public FormComponent(RobustWebElement element, ComponentContainer parent) {
        super(element, parent);
    }
    
    private int refreshCount;
    
    /** "toggle optional node" script */
    protected static final String TOGGLE_OPTIONAL = JsUtility.getScriptResource("toggleOptionalNode.js");
    
    /**
     * This enumeration defines element locator constants.
     */
    protected enum Using implements ByEnum {
        /** form component optional element */
        OPTIONAL(By.cssSelector("optional"));
        
        private final By locator;
        
        Using(By locator) {
            this.locator = locator;
        }

        @Override
        public By locator() {
            return locator;
        }
    }
    
    /**
     * Get optional reference to the form component "optional" element.
     * 
     * @return "optional" element of form component
     */
    public RobustWebElement getOptional() {
        return findOptional(Using.OPTIONAL);
    }
    
    /**
     * Get required reference to the form component "optional" element.
     * 
     * @return "optional" element of form component
     * @throws NoSuchElementException if no matching element is found
     */
    public WebElement getRequired() {
        return findElement(Using.OPTIONAL.locator);
    }

    /**
     * Toggle the existence of the form component "optional" element.
     * 
     * @return {@code true} if "optional" element was created; {@code false} if it was removed
     */
    public boolean toggleOptionalNode() {
        return JsUtility.runAndReturn(driver, TOGGLE_OPTIONAL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SearchContext refreshContext(long expiration) {
        refreshCount++;
        return super.refreshContext(expiration);
    }
    
    /**
     * Get form component refresh count.
     * 
     * @return form component refresh count
     */
    public int getRefreshCount() {
        return refreshCount;
    }
}
