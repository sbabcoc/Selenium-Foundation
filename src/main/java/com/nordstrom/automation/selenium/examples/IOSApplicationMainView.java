package com.nordstrom.automation.selenium.examples;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.nordstrom.automation.selenium.interfaces.DetectsLoadCompletion;
import com.nordstrom.automation.selenium.model.Page;

/**
 * This class is the model for the main view of the sample application used by the Appium iOS unit test.
 */
public class IOSApplicationMainView extends Page implements DetectsLoadCompletion<IOSApplicationMainView> {

    /**
     * Constructor for main view context.
     * 
     * @param driver driver object
     */
    public IOSApplicationMainView(WebDriver driver) {
        super(driver);
    }
    
    /**
     * This enumeration defines element locator constants.
     */
    protected enum Using implements ByEnum {
        /** view heading */
        HEADING(By.xpath("//XCUIElementTypeStaticText[@role='AXHeader']")),
        /** 'Echo Box' link */
        ECHO_BOX(By.id("Echo Box"));
        
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
        return getViewHeading().equals("TheApp");
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
     * Open the 'Echo Screen' view.
     * 
     * @return new {@link IOSApplicationEchoScreenView} instance
     */
    public IOSApplicationEchoScreenView openEchoScreen() {
        findElement(Using.ECHO_BOX).click();
        return new IOSApplicationEchoScreenView(driver);
    }
}
