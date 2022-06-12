package com.nordstrom.automation.selenium.examples;

import org.junit.BeforeClass;

import com.nordstrom.automation.selenium.junit.JUnitTargetBase;

public class JUnitTargetRoot extends JUnitTargetBase {

    @BeforeClass
    public static void beforeClass() {
        ExamplePage.setHubAsTarget();
    }

}
