package com.nordstrom.automation.selenium.plugins;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.nordstrom.automation.selenium.DriverPlugin;

import net.bytebuddy.implementation.Implementation;

/**
 * This class provides the base plugin implementation for drivers that extent {@code RemoteWebDriver}.
 */
public abstract class RemoteWebDriverPlugin implements DriverPlugin {
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Implementation getWebElementCtor(WebDriver driver, Class<? extends WebElement> refClass) {
        return null;
    }
}
