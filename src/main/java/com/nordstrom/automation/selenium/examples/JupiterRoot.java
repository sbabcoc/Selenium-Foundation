package com.nordstrom.automation.selenium.examples;

import org.junit.jupiter.api.BeforeAll;

import com.nordstrom.automation.selenium.jupiter.JupiterBase;

/**
 * This class provides a base for <b>Jupiter</b> test classes with methods that target features of {@link ExamplePage}.
 */
public class JupiterRoot extends JupiterBase {

    /**
     * This <b>BeforeAll</b> method configures <b>Selenium Foundation</b> to target {@link ExamplePage}.
     */
    @BeforeAll
    public static void beforeAll() {
        ExamplePage.setHubAsTarget();
    }

}
