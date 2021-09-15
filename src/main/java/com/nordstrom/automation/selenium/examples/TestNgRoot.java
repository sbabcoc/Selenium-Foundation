package com.nordstrom.automation.selenium.examples;

import org.testng.annotations.BeforeClass;

import com.nordstrom.automation.selenium.support.TestNgBase;

public class TestNgRoot extends TestNgBase {
    
    @BeforeClass
    public void beforeClass() {
        ExamplePage.setHubAsTarget();
    }

}
