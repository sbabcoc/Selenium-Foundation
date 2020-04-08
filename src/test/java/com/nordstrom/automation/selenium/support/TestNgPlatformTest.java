package com.nordstrom.automation.selenium.support;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.annotations.NoDriver;
import com.nordstrom.automation.selenium.platform.ExamplePlatform;
import com.nordstrom.automation.selenium.platform.TargetPlatform;

public class TestNgPlatformTest extends TestNgPlatformBase<ExamplePlatform> {

    public TestNgPlatformTest() {
        super(ExamplePlatform.class);
    }
    
    @Test
    @NoDriver
    public void testDefaultPlatform() {
        assertTrue(getTargetPlatform().matches(ExamplePlatform.PLATFORM_ONE_NAME));
    }
    
    @Test
    @NoDriver
    @TargetPlatform(ExamplePlatform.PLATFORM_TWO_NAME)
    public void testPlatformTwo() {
        assertTrue(getTargetPlatform().matches(ExamplePlatform.PLATFORM_TWO_NAME));
    }

    @Override
    public ExamplePlatform getDefaultPlatform() {
        return ExamplePlatform.PLATFORM_ONE;
    }

}
