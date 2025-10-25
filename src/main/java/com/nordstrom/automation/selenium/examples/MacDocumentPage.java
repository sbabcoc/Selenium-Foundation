package com.nordstrom.automation.selenium.examples;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.nordstrom.automation.selenium.model.Page;

public class MacDocumentPage extends Page {

    /**
     * Constructor for main view context.
     * 
     * @param driver driver object
     */
	public MacDocumentPage(WebDriver driver) {
		super(driver);
	}

    /**
     * This enumeration defines element locator constants.
     */
    protected enum Using implements ByEnum {
        TEXT_VIEW(By.id("First Text View"));
        
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
        findElement(Using.TEXT_VIEW).sendKeys(keys);
    }
    
    /**
     * Get text field content.
     * 
     * @return text field content
     */
    public String accessDocument() {
        return findElement(Using.TEXT_VIEW).getText();
    }
    
}
