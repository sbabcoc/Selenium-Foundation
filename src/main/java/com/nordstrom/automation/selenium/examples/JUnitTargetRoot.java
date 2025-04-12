package com.nordstrom.automation.selenium.examples;

import org.junit.BeforeClass;

import com.nordstrom.automation.selenium.junit.JUnitTargetBase;
import com.nordstrom.automation.selenium.platform.TargetType;

/**
 * This class provides a base for <b>JUnit</b> test classes with methods that target features of {@link ExamplePage}.
 */
public class JUnitTargetRoot extends JUnitTargetBase {

    /**
     * This <b>BeforeClass</b> method configures <b>Selenium Foundation</b> to target {@link ExamplePage}.
     */
    @BeforeClass
    public static void beforeClass() {
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
