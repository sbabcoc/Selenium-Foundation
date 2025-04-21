package com.nordstrom.automation.selenium.examples;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import com.nordstrom.automation.selenium.model.Page;

/**
 * This class is the model for the target view of the sample application used by the Appium Windows unit test.
 */
public class WindowsPage extends Page {

    /**
     * Constructor for main view context.
     * 
     * @param driver driver object
     */
    public WindowsPage(WebDriver driver) {
        super(driver);
    }
    
    /**
     * This enumeration defines element locator constants.
     */
    protected enum Using implements ByEnum {
        /** text field */
        EDIT_FIELD(By.className("Edit"));
        
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
     * Get document content.
     * 
     * @return document content
     */
    public String getDocument() {
        return findElement(Using.EDIT_FIELD).getText();
    }
    
}
