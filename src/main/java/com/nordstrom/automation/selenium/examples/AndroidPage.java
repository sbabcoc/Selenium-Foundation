package com.nordstrom.automation.selenium.examples;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.annotations.PageUrl;
import com.nordstrom.automation.selenium.model.Page;
import com.nordstrom.automation.selenium.support.SearchContextWait;

/**
 * This class is the model for the "Invoke Search" view of the Android API Demos app.
 */
@PageUrl(appPackage="io.appium.android.apis", value=".app.SearchInvoke")
public class AndroidPage extends Page {
    
    private static final boolean isSelenium3 = SeleniumConfig.getConfig().getVersion() == 3;

    /**
     * Constructor for main view context.
     * 
     * @param driver driver object
     */
    public AndroidPage(WebDriver driver) {
        super(driver);
    }
    
    /**
     * This enumeration defines element locator constants.
     */
    protected enum Using implements ByEnum {
        /** search query "prefill" field */
        QUERY_PREFILL("txt_query_prefill", "io.appium.android.apis:id/"),
        /** 'onSearchRequested' button */
        ACTIVATE_SEARCH("btn_start_search", "io.appium.android.apis:id/"),
        /** search query input field */
        QUERY_INPUT_FIELD("android:id/search_src_text", "");
        
        private final By locator;
        
        Using(String selector, String namespace) {
            this.locator = By.id(isSelenium3 ? selector : namespace + selector);
        }

        @Override
        public By locator() {
            return locator;
        }
    }
    
    /**
     * Submit the specified search query.
     * 
     * @param query search query
     */
    public void submitSearchQuery(String query) {
        findElement(Using.QUERY_PREFILL).sendKeys(query);
        findElement(Using.ACTIVATE_SEARCH).click();
        SearchContextWait wait = new SearchContextWait(driver, 30);
        wait.until(ExpectedConditions.visibilityOfElementLocated(Using.QUERY_INPUT_FIELD.locator));
    }
    
    /**
     * Get result of submitted search query.
     * 
     * @return search result
     */
    public String getSearchResult() {
        return findElement(Using.QUERY_INPUT_FIELD).getText();
    }
}
