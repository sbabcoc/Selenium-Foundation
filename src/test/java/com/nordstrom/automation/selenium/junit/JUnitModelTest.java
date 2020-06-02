package com.nordstrom.automation.selenium.junit;

import org.junit.Ignore;
import org.junit.Test;

import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.core.ModelTestCore;
import com.nordstrom.automation.selenium.model.ExamplePage;

@InitialPage(ExamplePage.class)
public class JUnitModelTest extends JUnitBase {

    @Test
    public void testBasicPage() {
        ModelTestCore.testBasicPage(this);
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
        ModelTestCore.testShadowRootByLocator(this);
    }

    @Test
    @Ignore
    public void testShadowRootByElement() {
        ModelTestCore.testShadowRootByElement(this);
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

}
