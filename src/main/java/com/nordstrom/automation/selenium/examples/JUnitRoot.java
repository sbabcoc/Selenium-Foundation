package com.nordstrom.automation.selenium.examples;

import org.junit.BeforeClass;

import com.nordstrom.automation.selenium.junit.JUnitBase;

/**
 * This class provides a base for <b>JUnit</b> test classes with methods that target features of {@link ExamplePage}.
 */
public class JUnitRoot extends JUnitBase {

    /**
     * This <b>BeforeClass</b> method configures <b>Selenium Foundation</b> to target {@link ExamplePage}.
     */
    @BeforeClass
    public static void beforeClass() {
        ExamplePage.setHubAsTarget();
    }

}
