package com.nordstrom.automation.selenium.model;

import org.testng.SkipException;
import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.core.ModelTestCore;
import com.nordstrom.automation.selenium.examples.ExamplePage;
import com.nordstrom.automation.selenium.examples.TestNgRoot;
import com.nordstrom.automation.selenium.exceptions.ShadowRootContextException;

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
        try {
            ModelTestCore.testShadowRootByLocator(this).run();
        } catch (ShadowRootContextException e) {
            throw new SkipException(e.getMessage(), e);
        }
    }

    @Test
    public void testShadowRootByElement() {
        try {
            ModelTestCore.testShadowRootByElement(this).run();
        } catch (ShadowRootContextException e) {
            throw new SkipException(e.getMessage(), e);
        }
    }
    
    @Test
    public void testShadowRootList() {
        try {
            ModelTestCore.testShadowRootList(this).run();
        } catch (ShadowRootContextException e) {
            throw new SkipException(e.getMessage(), e);
        }
    }

    @Test
    public void testShadowRootMap() {
        try {
            ModelTestCore.testShadowRootMap(this).run();
        } catch (ShadowRootContextException e) {
            throw new SkipException(e.getMessage(), e);
        }
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
    public void testShadowParagraphs() {
        try {
            ModelTestCore.testShadowParagraphs(this).run();
        } catch (ShadowRootContextException e) {
            throw new SkipException(e.getMessage(), e);
        }
    }

}
