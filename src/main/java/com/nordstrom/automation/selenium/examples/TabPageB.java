package com.nordstrom.automation.selenium.examples;

import org.openqa.selenium.WebDriver;

import com.nordstrom.automation.selenium.annotations.PageUrl;

/**
 * This class is the model for the second concrete tab pages opened by the "Open A/B Tab" button on the 'Example'
 * page.
 */
@PageUrl("/grid/admin/FrameB_Servlet")
public class TabPageB extends TabPage {
    
    /** expected content */
    public static String EXPECT_CONTENT = "Frame B";

    /**
     * Constructor for tab page view context.
     * 
     * @param driver driver object
     */
    public TabPageB(WebDriver driver) {
        super(driver);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean verifyContent() {
        return getPageContent().equals(EXPECT_CONTENT);
    }
}
