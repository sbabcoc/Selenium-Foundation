package com.nordstrom.automation.selenium.example;

import org.junit.BeforeClass;

import com.nordstrom.automation.selenium.junit.JUnitBase;

public class JUnitRoot extends JUnitBase {

    @BeforeClass
    public static void beforeClass() {
        ExamplePage.setHubAsTarget();
    }

}
