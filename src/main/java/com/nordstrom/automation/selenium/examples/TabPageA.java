package com.nordstrom.automation.selenium.examples;

import org.openqa.selenium.WebDriver;

import com.nordstrom.automation.selenium.annotations.PageUrl;

/**
 * This class is the model for the first concrete tab pages opened by the "Open A/B Tab" button on the 'Example'
 * page.
 */
@PageUrl("/grid/admin/FrameA_Servlet")
public class TabPageA extends TabPage {
    
    /** expected content */
    public static String EXPECT_CONTENT = "Frame A";

    /**
     * Constructor for tab page view context.
     * 
     * @param driver driver object
     */
    public TabPageA(WebDriver driver) {
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
