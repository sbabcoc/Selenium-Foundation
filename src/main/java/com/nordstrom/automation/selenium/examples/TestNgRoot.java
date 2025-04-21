package com.nordstrom.automation.selenium.examples;

import org.testng.annotations.BeforeClass;

import com.nordstrom.automation.selenium.support.TestNgBase;

/**
 * This class provides a base for <b>TestNG</b> test classes with methods that target features of {@link ExamplePage}.
 */
public class TestNgRoot extends TestNgBase {
    
    /**
     * This <b>BeforeClass</b> method configures <b>Selenium Foundation</b> to target {@link ExamplePage}.
     */
    @BeforeClass
    public void beforeClass() {
        ExamplePage.setHubAsTarget();
    }

}
