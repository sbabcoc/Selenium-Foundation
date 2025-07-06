package com.nordstrom.automation.selenium.junit;

import static org.junit.Assume.assumeNoException;
import org.junit.Ignore;
import org.junit.Test;

import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.core.ModelTestCore;
import com.nordstrom.automation.selenium.examples.ExamplePage;
import com.nordstrom.automation.selenium.examples.JUnitTargetRoot;
import com.nordstrom.automation.selenium.exceptions.ElementReferenceRefreshFailureException;
import com.nordstrom.automation.selenium.exceptions.ShadowRootContextException;

@InitialPage(ExamplePage.class)
public class JUnitModelTest extends JUnitTargetRoot {

    @Test
    public void testBasicPage() {
        ModelTestCore.testBasicPage(this);
    }
    
    @Test
    @Ignore
    public void updateTextInputSameValue() {
        ModelTestCore.updateTextInputSameValue(this);
    }

    @Test
    @Ignore
    public void updateTextInputNewValue() {
        ModelTestCore.updateTextInputNewValue(this);
    }

    @Test
    @Ignore
    public void updateTextInputBoolValue() {
        ModelTestCore.updateTextInputBoolValue(this);
    }

    @Test
    @Ignore
    public void updateTextInputNullValue() {
        ModelTestCore.updateTextInputNullValue(this);
    }

    @Test
    @Ignore
    public void updateCheckboxSameValue() {
        ModelTestCore.updateCheckboxSameValue(this);
    }

    @Test
    @Ignore
    public void updateCheckboxNewValue() {
        ModelTestCore.updateCheckboxNewValue(this);
    }

    @Test
    @Ignore
    public void updateCheckboxStringValue() {
        ModelTestCore.updateCheckboxStringValue(this);
    }

    @Test
    @Ignore
    public void updateCheckboxNullValue() {
        ModelTestCore.updateCheckboxNullValue(this);
    }

    @Test
    @Ignore
    public void testParagraphs() {
        ModelTestCore.testParagraphs(this);
    }
    
    @Test
    @Ignore
    public void testTable() {
        ModelTestCore.testTable(this);
    }

    @Test
    @Ignore
    public void testFrameByLocator() {
        ModelTestCore.testFrameByLocator(this);
    }

    @Test
    @Ignore
    public void testFrameByElement() {
        ModelTestCore.testFrameByElement(this);
    }

    @Test
    @Ignore
    public void testFrameByIndex() {
        ModelTestCore.testFrameByIndex(this);
    }

    @Test
    @Ignore
    public void testFrameById() {
        ModelTestCore.testFrameById(this);
    }
    
    @Test
    @Ignore
    public void testComponentList() {
        ModelTestCore.testComponentList(this);
    }
    
    @Test
    @Ignore
    public void testComponentMap() {
        ModelTestCore.testComponentMap(this);
    }
    
    @Test
    @Ignore
    public void testFrameList() {
        ModelTestCore.testFrameList(this);
    }

    @Test
    @Ignore
    public void testFrameMap() {
        ModelTestCore.testFrameMap(this);
    }

    @Test
    @Ignore
    public void testShadowRootByLocator() {
        try {
            ModelTestCore.testShadowRootByLocator(this).run();
        } catch (ShadowRootContextException e) {
            assumeNoException(e);
        }
    }

    @Test
    @Ignore
    public void testShadowRootByElement() {
        try {
            ModelTestCore.testShadowRootByElement(this).run();
        } catch (ShadowRootContextException e) {
            assumeNoException(e);
        }
    }
    
    @Test
    @Ignore
    public void testShadowRootList() {
        try {
            ModelTestCore.testShadowRootList(this).run();
        } catch (ShadowRootContextException e) {
            assumeNoException(e);
        }
    }

    @Test
    @Ignore
    public void testShadowRootMap() {
        try {
            ModelTestCore.testShadowRootMap(this).run();
        } catch (ShadowRootContextException e) {
            assumeNoException(e);
        }
    }

    /**
     * This test verifies that stale elements are automatically refreshed
     * and that the search context chain gets refreshed efficiently.
     */
    @Test
    @Ignore
    public void testRefresh() {
        ModelTestCore.testRefresh(this);
    }

    @Test
    @Ignore
    public void testCssOptional() {
        ModelTestCore.testCssOptional(this);
    }
    
    @Test
    @Ignore
    public void testXpathOptional() {
        ModelTestCore.testXpathOptional(this);
    }
    
    @Test
    @Ignore
    public void testBogusOptional() {
        ModelTestCore.testBogusOptional(this);
    }

    @Test
    @Ignore
    public void testOptionalBehavior() {
        ModelTestCore.testOptionalBehavior(this);
    }

    @Test(expected = ElementReferenceRefreshFailureException.class)
    @Ignore
    public void testReferenceRefreshFailure() {
        ModelTestCore.testReferenceRefreshFailure(this);
    }
    
    @Test
    @Ignore
    public void testShadowParagraphs() {
        try {
            ModelTestCore.testShadowParagraphs(this).run();
        } catch (ShadowRootContextException e) {
            assumeNoException(e);
        }
    }
    
    @Test
    @Ignore
    public void testContainerResolution() {
        ModelTestCore.testContainerResolution(this);
    }

}
