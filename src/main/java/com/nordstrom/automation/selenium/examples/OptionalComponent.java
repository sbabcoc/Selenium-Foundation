package com.nordstrom.automation.selenium.examples;

import org.openqa.selenium.By;

import com.nordstrom.automation.selenium.model.ComponentContainer;
import com.nordstrom.automation.selenium.model.PageComponent;
import com.nordstrom.automation.selenium.model.RobustWebElement;

/**
 * This class is the model for the example page's optional component - a page component whose own context
 * (root) element is obtained via {@code findOptional} and may or may not exist at any given time.
 */
public class OptionalComponent extends PageComponent {

    /**
     * Constructor for page component by context element
     * 
     * @param element component context element
     * @param parent component parent
     */
    public OptionalComponent(RobustWebElement element, ComponentContainer parent) {
        super(element, parent);
    }
    
    /**
     * This enumeration defines element locator constants.
     */
    protected enum Using implements ByEnum {
        /** the component's child element */
        CHILD(By.cssSelector("#optional-component-child"));
        
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
     * Get text content of this component's child element.
     * 
     * @return text content of the child element
     */
    public String getChildText() {
        return findElement(Using.CHILD).getText();
    }
    
}
