package com.nordstrom.automation.selenium.junit;

import org.junit.Test;
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

}
