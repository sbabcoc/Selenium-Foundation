package com.nordstrom.automation.selenium.windows;

import static com.nordstrom.automation.selenium.platform.TargetType.WINDOWS_NAME;
import static org.testng.Assert.assertEquals;

import org.openqa.selenium.Keys;
import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.examples.WindowsPage;
import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.platform.TargetPlatform;
import com.nordstrom.automation.selenium.support.TestNgTargetBase;

@InitialPage(WindowsPage.class)
public class WindowsTest extends TestNgTargetBase {
    
    @Test
    @TargetPlatform(WINDOWS_NAME)
    public void testEditing() {
        WindowsPage page = getInitialPage();
        page.modifyDocument("Hello world!");
        assertEquals(page.getDocumentContent(), "Hello world!");
        page.modifyDocument(Keys.CONTROL + "z");
    }
    
}
