package com.nordstrom.automation.selenium.examples;

import org.testng.annotations.BeforeClass;

import com.nordstrom.automation.selenium.support.TestNgTargetBase;

public class TestNgTargetRoot extends TestNgTargetBase {
    
    @BeforeClass
    public void beforeClass() {
        ExamplePage.setHubAsTarget();
    }

}
