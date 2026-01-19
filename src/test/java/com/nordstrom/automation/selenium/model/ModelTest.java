package com.nordstrom.automation.selenium.model;

import org.testng.annotations.Test;
import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.core.ModelTestCore;
import com.nordstrom.automation.selenium.examples.ExamplePage;
import com.nordstrom.automation.selenium.examples.TestNgTargetRoot;
import com.nordstrom.automation.selenium.exceptions.ElementReferenceRefreshFailureException;

@InitialPage(ExamplePage.class)
public class ModelTest extends TestNgTargetRoot {
    
    @Test
    public void testBasicPage() {
        ModelTestCore.testBasicPage(this);
    }
    
    @Test
    public void updateTextInputSameValue() {
        ModelTestCore.updateTextInputSameValue(this);
    }

    @Test
    public void updateTextInputNewValue() {
        ModelTestCore.updateTextInputNewValue(this);
    }

    @Test
    public void updateTextInputBoolValue() {
        ModelTestCore.updateTextInputBoolValue(this);
    }

    @Test
    public void updateTextInputNullValue() {
        ModelTestCore.updateTextInputNullValue(this);
    }

    @Test
    public void updateCheckboxSameValue() {
        ModelTestCore.updateCheckboxSameValue(this);
    }

    @Test
    public void updateCheckboxNewValue() {
        ModelTestCore.updateCheckboxNewValue(this);
    }

    @Test
    public void updateCheckboxStringValue() {
        ModelTestCore.updateCheckboxStringValue(this);
    }

    @Test
    public void updateCheckboxNullValue() {
        ModelTestCore.updateCheckboxNullValue(this);
    }

    @Test
    public void testParagraphs() {
        ModelTestCore.testParagraphs(this);
    }
    
    @Test
    public void testTable() {
        ModelTestCore.testTable(this);
    }

    @Test
    public void testFrameByLocator() {
        ModelTestCore.testFrameByLocator(this);
    }

    @Test
    public void testFrameByElement() {
        ModelTestCore.testFrameByElement(this);
    }

    @Test
    public void testFrameByIndex() {
        ModelTestCore.testFrameByIndex(this);
    }

    @Test(enabled = false)
    public void testFrameById() {
        ModelTestCore.testFrameById(this);
    }
    
    @Test
    public void testComponentList() {
        ModelTestCore.testComponentList(this);
    }
    
    @Test
    public void testComponentMap() {
        ModelTestCore.testComponentMap(this);
    }
    
    @Test
    public void testFrameList() {
        ModelTestCore.testFrameList(this);
    }

    @Test
    public void testFrameMap() {
        ModelTestCore.testFrameMap(this);
    }
    
    @Test
    public void testShadowRootByLocator() {
        skipIfNoShadowDom();
        ModelTestCore.testShadowRootByLocator(this).run();
    }

    @Test
    public void testShadowRootByElement() {
        skipIfNoShadowDom();
        ModelTestCore.testShadowRootByElement(this).run();
    }
    
    @Test
    public void testShadowRootList() {
        skipIfNoShadowDom();
        ModelTestCore.testShadowRootList(this).run();
    }

    @Test
    public void testShadowRootMap() {
        skipIfNoShadowDom();
        ModelTestCore.testShadowRootMap(this).run();
    }

    /**
     * This test verifies that stale elements are automatically refreshed
     * and that the search context chain gets refreshed efficiently.
     */
    @Test
    public void testRefresh() {
        ModelTestCore.testRefresh(this);
    }

    @Test
    public void testCssOptional() {
        ModelTestCore.testCssOptional(this);
    }
    
    @Test
    public void testXpathOptional() {
        ModelTestCore.testXpathOptional(this);
    }
    
    @Test
    public void testBogusOptional() {
        ModelTestCore.testBogusOptional(this);
    }

    @Test
    public void testOptionalBehavior() {
        ModelTestCore.testOptionalBehavior(this);
    }
    
    @Test(expectedExceptions = {ElementReferenceRefreshFailureException.class})
    public void testFailedReferenceRefreshAttempt() {
        ModelTestCore.testReferenceRefreshFailure(this);
    }
    
    @Test
    public void testShadowParagraphs() {
        skipIfNoShadowDom();
        ModelTestCore.testShadowParagraphs(this).run();
    }
    
    @Test
    public void testContainerResolution() {
        skipIfSafariOnIOS();
        ModelTestCore.testContainerResolution(this);
    }
}
