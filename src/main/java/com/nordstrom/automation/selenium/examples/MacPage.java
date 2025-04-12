package com.nordstrom.automation.selenium.examples;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.nordstrom.automation.selenium.model.Page;

/**
 * This class is the model for the target view of the sample application used by the Appium Macintosh unit test.
 */
public class MacPage extends Page {

    /**
     * Constructor for main view context.
     * 
     * @param driver driver object
     */
    public MacPage(WebDriver driver) {
        super(driver);
    }
    
    /**
     * This enumeration defines element locator constants.
     */
    protected enum Using implements ByEnum {
        /** text field */
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
    
    /**
     * Populate text field with the specified string.
     * 
     * @param keys string to type into text field
     */
    public void modifyDocument(String keys) {
        findElement(Using.EDIT_FIELD).sendKeys(keys);
    }
    
    /**
     * Get text field content.
     * 
     * @return text field content
     */
    public String accessDocument() {
        return findElement(Using.EDIT_FIELD).getText();
    }
    
}
