package com.nordstrom.automation.selenium.support;

import java.util.Optional;

import org.openqa.selenium.WebDriver;

import com.nordstrom.automation.selenium.exceptions.DriverNotAvailableException;
import com.nordstrom.automation.selenium.exceptions.InitialPageNotSpecifiedException;
import com.nordstrom.automation.selenium.model.Page;


public interface TestBase {

    default WebDriver getDriver() {
        Optional<WebDriver> optDriver = findDriver();
        if (optDriver.isPresent()) {
            return optDriver.get();
        }
        throw new DriverNotAvailableException();
    }

    Optional<WebDriver> findDriver();

    Optional<WebDriver> setDriver(WebDriver driver);
    
    default Page prepInitialPage(Page initialPage) {
        if (initialPage.getWindowHandle() == null) {
            initialPage.setWindowHandle(initialPage.getDriver().getWindowHandle());
        }
        // required when initial page is local file
        setDriver(initialPage.getDriver());
        return initialPage.enhanceContainer(initialPage);
    }

    default Page getInitialPage() {
        Optional<Page> optInitialPage = findInitialPage();
        if (optInitialPage.isPresent()) {
            return optInitialPage.get();
        }
        throw new InitialPageNotSpecifiedException();
    }

    default boolean hasInitialPage() {
        return findInitialPage().isPresent();
    }

    Optional<Page> findInitialPage();

    Optional<Page> setInitialPage(Page initialPage);

}
