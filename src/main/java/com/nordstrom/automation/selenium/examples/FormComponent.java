package com.nordstrom.automation.selenium.examples;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import com.nordstrom.automation.selenium.core.JsUtility;
import com.nordstrom.automation.selenium.model.ComponentContainer;
import com.nordstrom.automation.selenium.model.PageComponent;
import com.nordstrom.automation.selenium.model.RobustWebElement;

public class FormComponent extends PageComponent {

    public FormComponent(By locator, ComponentContainer parent) {
        super(locator, parent);
    }
    
    public FormComponent(RobustWebElement element, ComponentContainer parent) {
        super(element, parent);
    }
    
    private int refreshCount;
    protected static final String TOGGLE_OPTIONAL = JsUtility.getScriptResource("toggleOptionalNode.js");
    
    protected enum Using implements ByEnum {
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
    
    public RobustWebElement getOptional() {
        return findOptional(Using.OPTIONAL);
    }
    
    public WebElement getRequired() {
        return findElement(Using.OPTIONAL.locator);
    }

    public boolean toggleOptionalNode() {
        return JsUtility.runAndReturn(driver, TOGGLE_OPTIONAL);
    }

    @Override
    public SearchContext refreshContext(long expiration) {
        refreshCount++;
        return super.refreshContext(expiration);
    }
    
    public int getRefreshCount() {
        return refreshCount;
    }
}
