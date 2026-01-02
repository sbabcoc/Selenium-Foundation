package com.nordstrom.automation.selenium.examples;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import com.nordstrom.automation.selenium.interfaces.DetectsLoadCompletion;
import com.nordstrom.automation.selenium.model.Page;

/**
 * This class is the model for the 'Echo Screen' view of the sample application used by the Appium iOS unit test.
 */
public class IOSApplicationEchoScreenView extends Page implements DetectsLoadCompletion<IOSApplicationEchoScreenView> {

    /**
     * Constructor for main view context.
     * 
     * @param driver driver object
     */
    public IOSApplicationEchoScreenView(WebDriver driver) {
        super(driver);
    }
    
    /**
     * This enumeration defines element locator constants.
     */
    protected enum Using implements ByEnum {
        /** view heading */
        HEADING(By.xpath("//XCUIElementTypeStaticText[contains(concat(' ',@traits,' '), ' Header ')]")),
        /** 'back' button */
        BACK_BUTTON(By.id("BackButton")),
        /** message input */
        MESSAGE_INPUT(By.id("messageInput")),
        /** 'Save' button */
        SAVE_BUTTON(By.id("messageSaveBtn")),
        /** saved message */
        SAVED_MESSAGE(By.id("savedMessage"));
        
        private final By locator;
        
        Using(By locator) {
            this.locator = locator;
        }

        @Override
        public By locator() {
            return locator;
        }
    }
    
    @Override
    public boolean isLoadComplete() {
        return getViewHeading().equals("Echo Screen");
    }
    
    /**
     * Get the view heading.
     * 
     * @return view heading
     */
    public String getViewHeading() {
        return findElement(Using.HEADING).getText();
    }
    
    /**
     * Get the 'Echo Screen' saved message.
     * 
     * @return saved message
     */
    public String getSavedMessage() {
        return findElement(Using.SAVED_MESSAGE).getText();
    }
    
    /**
     * Set the 'Echo Screen' saved message.
     * 
     * @param message message to be saved
     */
    public void setSavedMessage(final String message) {
        findElement(Using.MESSAGE_INPUT).sendKeys(message);
        findElement(Using.SAVE_BUTTON).click();
    }
}
