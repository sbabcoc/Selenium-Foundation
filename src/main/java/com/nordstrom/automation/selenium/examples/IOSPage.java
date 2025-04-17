package com.nordstrom.automation.selenium.examples;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.nordstrom.automation.selenium.model.Page;

/**
 * This class is the model for the target view of the sample application used by the Appium iOS unit test.
 */
public class IOSPage extends Page {

    /**
     * Constructor for main view context.
     * 
     * @param driver driver object
     */
    public IOSPage(WebDriver driver) {
        super(driver);
    }
    
    /**
     * This enumeration defines element locator constants.
     */
    protected enum Using implements ByEnum {
        /** text field */
        TEXT_FIELD(By.className("XCUIElementTypeTextField"));
        
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
     * Populate text field with the specified string.
     * 
     * @param keys string to type into text field
     */
    public void modifyField(String keys) {
        findElement(Using.TEXT_FIELD).click();
        findElement(Using.TEXT_FIELD).sendKeys(keys);
    }
    
    /**
     * Get text field content.
     * 
     * @return text field content
     */
    public String accessField() {
        return findElement(Using.TEXT_FIELD).getText();
    }
    
}
