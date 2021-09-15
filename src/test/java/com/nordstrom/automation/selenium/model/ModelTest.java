package com.nordstrom.automation.selenium.model;

import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.core.ModelTestCore;
import com.nordstrom.automation.selenium.example.ExamplePage;
import com.nordstrom.automation.selenium.example.TestNgRoot;

@InitialPage(ExamplePage.class)
public class ModelTest extends TestNgRoot {
    
    @Test
    public void testBasicPage() {
        ModelTestCore.testBasicPage(this);
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
        ModelTestCore.testShadowRootByLocator(this);
    }

    @Test
    public void testShadowRootByElement() {
        ModelTestCore.testShadowRootByElement(this);
    }
    
    @Test
    public void testShadowRootList() {
        ModelTestCore.testShadowRootList(this);
    }

    @Test
    public void testShadowRootMap() {
        ModelTestCore.testShadowRootMap(this);
    }

    /**
     * This test verifies that stale elements are automatically refreshed
     * and that the search context chain gets refreshed efficiently.
     */
    @Test
    public void testRefresh() {
        ModelTestCore.testRefresh(this);
    }

}
