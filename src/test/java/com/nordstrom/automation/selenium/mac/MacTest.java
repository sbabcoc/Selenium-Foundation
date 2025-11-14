package com.nordstrom.automation.selenium.mac;

import static com.nordstrom.automation.selenium.platform.TargetType.MAC_APP_NAME;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.examples.TextEditApplication;
import com.nordstrom.automation.selenium.examples.TextEditDocumentWindow;
import com.nordstrom.automation.selenium.examples.TextEditManagementPanel;
import com.nordstrom.automation.selenium.platform.TargetPlatform;
import com.nordstrom.automation.selenium.support.TestNgTargetBase;

@InitialPage(TextEditApplication.class)
public class MacTest extends TestNgTargetBase {
    
    @Test
    @TargetPlatform(MAC_APP_NAME)
    public void testEditing() {
        TextEditApplication application = getInitialPage();
        TextEditManagementPanel managementPanel = application.openManagementPanel();
        TextEditDocumentWindow documentWindow = managementPanel.openNewDocument();
        documentWindow.modifyDocument("Hello world!");
        assertEquals(documentWindow.getDocumentContent(), "Hello world!");
        documentWindow.closeDocumentWithoutSaving();
    }
    
}
