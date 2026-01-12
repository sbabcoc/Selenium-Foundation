package com.nordstrom.automation.selenium.model;

import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.core.AlertTestCore;
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
}
