package com.nordstrom.automation.selenium.model;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;

public class FrameComponent extends Frame {
    
    public FrameComponent(By locator, ComponentContainer parent) {
        super(locator, parent);
    }
    
    public FrameComponent(By locator, int index, ComponentContainer parent) {
        super(locator, index, parent);
    }
    
    public FrameComponent(RobustWebElement element, ComponentContainer parent) {
        super(element, parent);
    }
    
    public FrameComponent(int index, ComponentContainer parent) {
        super(index, parent);
    }
    
    public FrameComponent(String nameOrId, ComponentContainer parent) {
        super(nameOrId, parent);
    }
    
    private enum Using implements ByEnum {
        HEADING(By.cssSelector("h1"));
        
        private By locator;
        
        Using(By locator) {
            this.locator = locator;
        }

        @Override
        public By locator() {
            return locator;
        }
    }
    
    public String getPageContent() {
        return findElement(Using.HEADING).getText();
    }

    public static Object getKey(SearchContext context) {
        WebDriver driver = switchTo(context);
        Object key = driver.findElement(Using.HEADING.locator).getText();
        switchToParentFrame((RobustWebElement) context);
        return key;
    }

}
