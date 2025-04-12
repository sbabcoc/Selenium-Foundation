package com.nordstrom.automation.selenium.ios;

import static com.nordstrom.automation.selenium.platform.TargetType.IOS_APP_NAME;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.examples.IOSPage;
import com.nordstrom.automation.selenium.platform.TargetPlatform;
import com.nordstrom.automation.selenium.support.TestNgTargetBase;

@InitialPage(IOSPage.class)
public class IOSTest extends TestNgTargetBase {
    
    @Test
    @TargetPlatform(IOS_APP_NAME)
    public void testEditing() {
        IOSPage page = getInitialPage();
        page.modifyField("Hello world!");
        assertEquals(page.accessField(), "Hello world!");
    }
    
}
