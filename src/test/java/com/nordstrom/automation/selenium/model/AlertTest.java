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
        AlertTestCore.testAlertModal(this);
    }
    
    @Test
    public void testConfirmModal() {
        AlertTestCore.testConfirmModal(this);
    }
    
    @Test
    public void testDismissModal() {
        AlertTestCore.testDismissModal(this);
    }
    
    @Test
    public void testSubmitPromptModal() {
        AlertTestCore.testSubmitPromptModal(this);
    }
    
    @Test
    public void testDismissPromptModal() {
        AlertTestCore.testDismissPromptModal(this);
    }
}
