package com.nordstrom.automation.selenium.examples;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.nordstrom.automation.selenium.model.Page;

public class MacPage extends Page {

    public MacPage(WebDriver driver) {
        super(driver);
    }
    
    protected enum Using implements ByEnum {
        EDIT_FIELD(By.className("XCUIElementTypeTextView"));
        
        private final By locator;
        
        Using(By locator) {
            this.locator = locator;
        }

        @Override
        public By locator() {
            return locator;
        }
    }
    
    public void modifyDocument(String keys) {
        findElement(Using.EDIT_FIELD).sendKeys(keys);
    }
    
    public String accessDocument() {
        return findElement(Using.EDIT_FIELD).getText();
    }
    
}
