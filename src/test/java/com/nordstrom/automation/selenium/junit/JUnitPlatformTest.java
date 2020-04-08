package com.nordstrom.automation.selenium.junit;

import org.junit.Test;
import org.openqa.selenium.WebDriver;

import com.nordstrom.automation.selenium.exceptions.PlatformActivationFailedException;
import com.nordstrom.automation.selenium.platform.ExamplePlatform;

public class JUnitPlatformTest extends JUnitPlatformBase<ExamplePlatform> {

    public JUnitPlatformTest() {
        super(ExamplePlatform.class);
    }
    
    @Test
    public void testDefaultPlatform() {
        
    }

    @Override
    public ExamplePlatform getDefaultPlatform() {
        return ExamplePlatform.PLATFORM_ONE;
    }

    @Override
    public ExamplePlatform activatePlatform(WebDriver driver, ExamplePlatform platform)
                    throws PlatformActivationFailedException {
        // TODO Auto-generated method stub
        return null;
    }

}
