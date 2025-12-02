package com.nordstrom.automation.selenium.examples;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.nordstrom.automation.selenium.annotations.Resolver;
import com.nordstrom.automation.selenium.interfaces.DetectsLoadCompletion;
import com.nordstrom.automation.selenium.model.Page;

/**
 * This class is the abstract model for the tab pages opened by the "Open A/B Tab" button on the 'Example' page.
 * The {@link Resolver} annotation specifies the container resolver that selects the concrete subclass model for
 * the specific page that gets opened ({@link TabPageA} or {@link TabPageB}).
 */
@Resolver(TabPageResolver.class)
public class TabPage extends Page implements DetectsLoadCompletion<TabPage> {
    
    /**
     * Constructor for tab page view context.
     * 
     * @param driver driver object
     */
    public TabPage(WebDriver driver) {
        super(driver);
    }
    
    /**
     * This enumeration defines element locator constants.
     */
    enum Using implements ByEnum {
        /** page heading locator */
        HEADING(By.cssSelector("h1"));
        
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
     * Get content of tab page.
     * 
     * @return tab page content
     */
    public String getPageContent() {
        return findElement(Using.HEADING).getText();
    }

    /**
     * Verify content of tab page.
     * 
     * @return {@code true} if verification succeeds; otherwise {@code false}
     */
    public boolean verifyContent() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLoadComplete() {
        return verifyContent();
    }
}
