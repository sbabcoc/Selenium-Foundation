package com.nordstrom.automation.selenium.model;

import org.testng.annotations.Test;
import com.nordstrom.automation.selenium.AbstractSeleniumConfig.SeleniumSettings;
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
    public void testOptionalFrameBehavior() {
        // the absent-context case waits out the full WAIT_TIMEOUT before surfacing (by design - an optional
        // context's existence may be contingent on application state that hasn't happened yet), so shorten it
        // for the duration of this test rather than eating the real (default 15-second) timeout on every run
        String key = SeleniumSettings.WAIT_TIMEOUT.key();
        String saved = System.getProperty(key);
        System.setProperty(key, "1");
        try {
            ModelTestCore.testOptionalFrameBehavior(this);
        } finally {
            if (saved != null) {
                System.setProperty(key, saved);
            } else {
                System.clearProperty(key);
            }
        }
    }
    
    @Test
    public void testOptionalComponentBehavior() {
        // same rationale as testOptionalFrameBehavior, but this path waits out IMPLIED_TIMEOUT (via
        // RobustElementWrapper.refreshReference), not WAIT_TIMEOUT (via switchTo()) - a different wait entirely
        String key = SeleniumSettings.IMPLIED_TIMEOUT.key();
        String saved = System.getProperty(key);
        System.setProperty(key, "1");
        try {
            ModelTestCore.testOptionalComponentBehavior(this);
        } finally {
            if (saved != null) {
                System.setProperty(key, saved);
            } else {
                System.clearProperty(key);
            }
        }
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
        // this scenario waits out the full implied-wait timeout while confirming the element is truly gone
        // (not just temporarily stale), so shorten it for the duration of this test rather than eating the
        // real (default 15-second) timeout on every run
        String key = SeleniumSettings.IMPLIED_TIMEOUT.key();
        String saved = System.getProperty(key);
        System.setProperty(key, "1");
        try {
            ModelTestCore.testReferenceRefreshFailure(this);
        } finally {
            if (saved != null) {
                System.setProperty(key, saved);
            } else {
                System.clearProperty(key);
            }
        }
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
