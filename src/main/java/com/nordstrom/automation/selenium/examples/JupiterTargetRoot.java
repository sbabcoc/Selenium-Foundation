package com.nordstrom.automation.selenium.examples;

import org.junit.jupiter.api.BeforeAll;

import com.nordstrom.automation.selenium.jupiter.JupiterTargetBase;
import com.nordstrom.automation.selenium.platform.TargetType;

/**
 * This class provides a base for <b>Jupiter</b> test classes with methods that target features of {@link ExamplePage}.
 */
public class JupiterTargetRoot extends JupiterTargetBase {

    /**
     * This <b>BeforeAll</b> method configures <b>Selenium Foundation</b> to target {@link ExamplePage}.
     */
    @BeforeAll
    public static void beforeAll() {
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
