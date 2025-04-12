package com.nordstrom.automation.selenium.mac;

import static com.nordstrom.automation.selenium.platform.TargetType.MAC_APP_NAME;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.examples.MacPage;
import com.nordstrom.automation.selenium.platform.TargetPlatform;
import com.nordstrom.automation.selenium.support.TestNgTargetBase;

@InitialPage(MacPage.class)
public class MacTest extends TestNgTargetBase {
    
    @Test
    @TargetPlatform(MAC_APP_NAME)
    public void testEditing() {
        MacPage page = getInitialPage();
        page.modifyDocument("Hello world!");
        assertEquals(page.accessDocument(), "Hello world!");
    }
    
}
