package com.nordstrom.automation.selenium.examples;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.nordstrom.automation.selenium.model.Page;

public class IOSPage extends Page {

    public IOSPage(WebDriver driver) {
        super(driver);
    }
    
    protected enum Using implements ByEnum {
        TEXT_FIELD(By.className("XCUIElementTypeTextField"));
        
        private By locator;
        
        Using(By locator) {
            this.locator = locator;
        }

        @Override
        public By locator() {
            return locator;
        }
    }
    
    public void modifyField(String keys) {
        findElement(Using.TEXT_FIELD).click();
        findElement(Using.TEXT_FIELD).sendKeys(keys);
    }
    
    public String accessField() {
        return findElement(Using.TEXT_FIELD).getText();
    }
    
}
