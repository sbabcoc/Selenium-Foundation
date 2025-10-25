package com.nordstrom.automation.selenium.examples;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.nordstrom.automation.selenium.model.Page;

/**
 * This class is the model for the target view of the sample application used by the Appium Macintosh unit test.
 */
public class MacLaunchPage extends Page {

    /**
     * Constructor for main view context.
     * 
     * @param driver driver object
     */
    public MacLaunchPage(WebDriver driver) {
        super(driver);
    }
    
    /**
     * This enumeration defines element locator constants.
     */
    protected enum Using implements ByEnum {
        NEW_DOCUMENT(By.id("New Document"));
        
        private final By locator;
        
        Using(By locator) {
            this.locator = locator;
        }

        @Override
        public By locator() {
            return locator;
        }
    }
    
    public MacDocumentPage openNewDocument() {
        findElement(Using.NEW_DOCUMENT).click();
        setWindowState(WindowState.WILL_CLOSE);
        MacDocumentPage newPage = new MacDocumentPage(driver);
        newPage.setWindowState(WindowState.WILL_CLOSE);
        return newPage;
    }
    
}
