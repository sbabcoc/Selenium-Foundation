package com.nordstrom.automation.selenium.examples;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.nordstrom.automation.selenium.annotations.PageUrl;
import com.nordstrom.automation.selenium.model.Page;

/**
 * This class is the model for the "Invoke Search" view of the Android API Demos app.
 */
@PageUrl(appPackage="io.appium.android.apis", value=".app.SearchInvoke")
public class AndroidPage extends Page {

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
        QUERY_PREFILL(By.id("io.appium.android.apis:id/txt_query_prefill")),
        /** 'onSearchRequested' button */
        ACTIVATE_SEARCH(By.id("io.appium.android.apis:id/btn_start_search")),
        /** search query input field */
        QUERY_INPUT_FIELD(By.id("android:id/search_src_text"));
        
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
     * Submit the specified search query.
     * 
     * @param query search query
     */
    public void submitSearchQuery(String query) {
        findElement(Using.QUERY_PREFILL).sendKeys(query);
        findElement(Using.ACTIVATE_SEARCH).click();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
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
