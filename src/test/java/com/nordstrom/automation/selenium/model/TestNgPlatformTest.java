package com.nordstrom.automation.selenium.model;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.platform.ExamplePlatform;
import com.nordstrom.automation.selenium.support.TestNgPlatformBase;

public class TestNgPlatformTest extends TestNgPlatformBase<ExamplePlatform> {

    public TestNgPlatformTest() {
        super(ExamplePlatform.class);
    }
    
    @Test
    public void testDefaultPlatform() {
        assertTrue(getTargetPlatform().matches("platform_one"));
    }

    @Override
    public ExamplePlatform getDefaultPlatform() {
        return ExamplePlatform.PLATFORM_ONE;
    }

}
