package com.nordstrom.automation.selenium.model;

import org.openqa.selenium.Platform;
import org.testng.SkipException;
import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.core.AlertTestCore;
import com.nordstrom.automation.selenium.core.WebDriverUtils;
import com.nordstrom.automation.selenium.examples.ExamplePage;
import com.nordstrom.automation.selenium.examples.TestNgTargetRoot;

@InitialPage(ExamplePage.class)
public class AlertTest extends TestNgTargetRoot {
    
    @Test
    public void testAlertModal() {
        skipIfSafariOnIOS();
        AlertTestCore.testAlertModal(this);
    }
    
    @Test
    public void testConfirmModal() {
        skipIfSafariOnIOS();
        AlertTestCore.testConfirmModal(this);
    }
    
    @Test
    public void testDismissModal() {
        skipIfSafariOnIOS();
        AlertTestCore.testDismissModal(this);
    }
    
    @Test
    public void testSubmitPromptModal() {
        skipIfSafariOnIOS();
        AlertTestCore.testSubmitPromptModal(this);
    }
    
    @Test
    public void testDismissPromptModal() {
        skipIfSafariOnIOS();
        AlertTestCore.testDismissPromptModal(this);
    }
    
    private void skipIfSafariOnIOS() {
        // if running Safari on iOS
        if (Platform.IOS.equals(WebDriverUtils.getPlatform(getDriver()))
                && "Safari".equals(WebDriverUtils.getBrowserName(getDriver()))) {
            throw new SkipException("This scenario is unsupported on iOS Safari");
        }
    }
}
