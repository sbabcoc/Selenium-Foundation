package com.nordstrom.automation.selenium.examples;

import org.testng.annotations.BeforeClass;

import com.nordstrom.automation.selenium.platform.TargetType;
import com.nordstrom.automation.selenium.support.TestNgTargetBase;

/**
 * This class provides a base for <b>TestNG</b> test classes with methods that target features of {@link ExamplePage}.
 */
public class TestNgTargetRoot extends TestNgTargetBase {
    
    /**
     * This <b>BeforeClass</b> method configures <b>Selenium Foundation</b> to target {@link ExamplePage}.
     */
    @BeforeClass
    public void beforeClass() {
        ExamplePage.setHubAsTarget();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TargetType getDefaultPlatform() {
        return TargetType.WEB_APP;
    }

}
