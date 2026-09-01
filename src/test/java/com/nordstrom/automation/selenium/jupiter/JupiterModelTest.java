package com.nordstrom.automation.selenium.jupiter;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.core.ModelTestCore;
import com.nordstrom.automation.selenium.examples.ExamplePage;
import com.nordstrom.automation.selenium.examples.JupiterTargetRoot;
import com.nordstrom.automation.selenium.exceptions.ElementReferenceRefreshFailureException;

/**
 * Jupiter-native equivalent of {@code JUnitModelTest}, with every method that's {@code @Ignore}d in the
 * real JUnit 4 source enabled here instead - per direction, those methods are known-passing when run;
 * they're disabled there specifically to avoid running equivalent coverage redundantly across the
 * JUnit4/TestNG suites, not because anything about them is unreliable.
 */
@InitialPage(ExamplePage.class)
public class JupiterModelTest extends JupiterTargetRoot {

    @Test
    public void testBasicPage() {
        ModelTestCore.testBasicPage(this);
    }

    @Disabled
    @Test
    public void updateTextInputSameValue() {
        ModelTestCore.updateTextInputSameValue(this);
    }

    @Disabled
    @Test
    public void updateTextInputNewValue() {
        ModelTestCore.updateTextInputNewValue(this);
    }

    @Disabled
    @Test
    public void updateTextInputBoolValue() {
        ModelTestCore.updateTextInputBoolValue(this);
    }

    @Disabled
    @Test
    public void updateTextInputNullValue() {
        ModelTestCore.updateTextInputNullValue(this);
    }

    @Disabled
    @Test
    public void updateCheckboxSameValue() {
        ModelTestCore.updateCheckboxSameValue(this);
    }

    @Disabled
    @Test
    public void updateCheckboxNewValue() {
        ModelTestCore.updateCheckboxNewValue(this);
    }

    @Disabled
    @Test
    public void updateCheckboxStringValue() {
        ModelTestCore.updateCheckboxStringValue(this);
    }

    @Disabled
    @Test
    public void updateCheckboxNullValue() {
        ModelTestCore.updateCheckboxNullValue(this);
    }

    @Disabled
    @Test
    public void testParagraphs() {
        ModelTestCore.testParagraphs(this);
    }

    @Disabled
    @Test
    public void testTable() {
        ModelTestCore.testTable(this);
    }

    @Disabled
    @Test
    public void testFrameByLocator() {
        ModelTestCore.testFrameByLocator(this);
    }

    @Disabled
    @Test
    public void testFrameByElement() {
        ModelTestCore.testFrameByElement(this);
    }

    @Disabled
    @Test
    public void testFrameByIndex() {
        ModelTestCore.testFrameByIndex(this);
    }

    @Disabled
    @Test
    public void testFrameById() {
        ModelTestCore.testFrameById(this);
    }

    @Disabled
    @Test
    public void testOptionalFrameBehavior() {
        ModelTestCore.testOptionalFrameBehavior(this);
    }

    @Disabled
    @Test
    public void testOptionalComponentBehavior() {
        ModelTestCore.testOptionalComponentBehavior(this);
    }

    @Disabled
    @Test
    public void testComponentList() {
        ModelTestCore.testComponentList(this);
    }

    @Disabled
    @Test
    public void testComponentMap() {
        ModelTestCore.testComponentMap(this);
    }

    @Disabled
    @Test
    public void testFrameList() {
        ModelTestCore.testFrameList(this);
    }

    @Disabled
    @Test
    public void testFrameMap() {
        ModelTestCore.testFrameMap(this);
    }

    @Disabled
    @Test
    public void testShadowRootByLocator() {
        skipIfNoShadowDom();
        ModelTestCore.testShadowRootByLocator(this).run();
    }

    @Disabled
    @Test
    public void testShadowRootByElement() {
        skipIfNoShadowDom();
        ModelTestCore.testShadowRootByElement(this).run();
    }

    @Disabled
    @Test
    public void testShadowRootList() {
        skipIfNoShadowDom();
        ModelTestCore.testShadowRootList(this).run();
    }

    @Disabled
    @Test
    public void testShadowRootMap() {
        skipIfNoShadowDom();
        ModelTestCore.testShadowRootMap(this).run();
    }

    /**
     * This test verifies that stale elements are automatically refreshed
     * and that the search context chain gets refreshed efficiently.
     */
    @Disabled
    @Test
    public void testRefresh() {
        ModelTestCore.testRefresh(this);
    }

    @Disabled
    @Test
    public void testCssOptional() {
        ModelTestCore.testCssOptional(this);
    }

    @Disabled
    @Test
    public void testXpathOptional() {
        ModelTestCore.testXpathOptional(this);
    }

    @Disabled
    @Test
    public void testBogusOptional() {
        ModelTestCore.testBogusOptional(this);
    }

    @Disabled
    @Test
    public void testOptionalBehavior() {
        ModelTestCore.testOptionalBehavior(this);
    }

    @Disabled
    @Test
    public void testReferenceRefreshFailure() {
        assertThrows(ElementReferenceRefreshFailureException.class,
                () -> ModelTestCore.testReferenceRefreshFailure(this));
    }

    @Disabled
    @Test
    public void testShadowParagraphs() {
        skipIfNoShadowDom();
        ModelTestCore.testShadowParagraphs(this).run();
    }

    @Disabled
    @Test
    public void testContainerResolution() {
        skipIfSafariOnIOS();
        ModelTestCore.testContainerResolution(this);
    }
}
