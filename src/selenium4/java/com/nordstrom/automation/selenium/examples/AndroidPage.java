package com.nordstrom.automation.selenium.examples;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.nordstrom.automation.selenium.annotations.PageUrl;
import com.nordstrom.automation.selenium.model.Page;

@PageUrl(appPackage="io.appium.android.apis", value=".app.SearchInvoke")
public class AndroidPage extends Page {

    public AndroidPage(WebDriver driver) {
        super(driver);
    }
    
    protected enum Using implements ByEnum {
        SEARCH_QUERY(By.id("txt_query_prefill")),
        SEARCH_BUTTON(By.id("btn_start_search")),
        SEARCH_RESULT(By.id("android:id/search_src_text"));
        
        private final By locator;
        
        Using(By locator) {
            this.locator = locator;
        }

        @Override
        public By locator() {
            return locator;
        }
    }
    
    public void submitSearchQuery(String query) {
        findElement(Using.SEARCH_QUERY).sendKeys(query);
        findElement(Using.SEARCH_BUTTON).click();
    }
    
    public String getSearchResult() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        WebElement searchResult = wait.until(ExpectedConditions.visibilityOfElementLocated(Using.SEARCH_RESULT.locator));
        return searchResult.getText();
    }
    
}
