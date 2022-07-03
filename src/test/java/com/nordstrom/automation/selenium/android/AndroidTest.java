package com.nordstrom.automation.selenium.android;

import static com.nordstrom.automation.selenium.platform.TargetType.ANDROID_NAME;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.examples.AndroidPage;
import com.nordstrom.automation.selenium.examples.TestNgTargetRoot;
import com.nordstrom.automation.selenium.platform.TargetPlatform;

@InitialPage(AndroidPage.class)
public class AndroidTest extends TestNgTargetRoot {
    
//    private final String MY_PACKAGE = "com.example.myapplication";
//    private final String SETTINGS_ACTIVITY = ".SettingsActivity";
    
//    private final String KEY_SIGNATURE = "signature";
//    private final String KEY_REPLY = "reply";
//    private final String KEY_SYNC = "sync";
//    private final String KEY_ATTACHMENT = "attachment";
    
    @Test
    @TargetPlatform(ANDROID_NAME)
    public void testSearchActivity() {
        AndroidPage page = getInitialPage();
        page.submitSearchQuery("Hello world!");
        assertEquals(page.getSearchResult(), "Hello world!");
    }
    
}
