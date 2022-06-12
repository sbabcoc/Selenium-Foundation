package com.nordstrom.automation.selenium.model;

import static com.nordstrom.automation.selenium.platform.TargetType.WEB_APP_NAME;

import org.testng.SkipException;
import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.core.ModelTestCore;
import com.nordstrom.automation.selenium.examples.ExamplePage;
import com.nordstrom.automation.selenium.examples.TestNgTargetRoot;
import com.nordstrom.automation.selenium.exceptions.ShadowRootContextException;
import com.nordstrom.automation.selenium.platform.TargetPlatform;

@InitialPage(ExamplePage.class)
public class ModelTest extends TestNgTargetRoot {
    
    @Test
    @TargetPlatform(WEB_APP_NAME)
    public void testBasicPage() {
        ModelTestCore.testBasicPage(this);
    }
    
    @Test
    @TargetPlatform(WEB_APP_NAME)
    public void testParagraphs() {
        ModelTestCore.testParagraphs(this);
    }
    
    @Test
    @TargetPlatform(WEB_APP_NAME)
    public void testTable() {
        ModelTestCore.testTable(this);
    }

    @Test
    @TargetPlatform(WEB_APP_NAME)
    public void testFrameByLocator() {
        ModelTestCore.testFrameByLocator(this);
    }

    @Test
    @TargetPlatform(WEB_APP_NAME)
    public void testFrameByElement() {
        ModelTestCore.testFrameByElement(this);
    }

    @Test
    @TargetPlatform(WEB_APP_NAME)
    public void testFrameByIndex() {
        ModelTestCore.testFrameByIndex(this);
    }

    @Test(enabled = false)
    @TargetPlatform(WEB_APP_NAME)
    public void testFrameById() {
        ModelTestCore.testFrameById(this);
    }
    
    @Test
    @TargetPlatform(WEB_APP_NAME)
    public void testComponentList() {
        ModelTestCore.testComponentList(this);
    }
    
    @Test
    @TargetPlatform(WEB_APP_NAME)
    public void testComponentMap() {
        ModelTestCore.testComponentMap(this);
    }
    
    @Test
    @TargetPlatform(WEB_APP_NAME)
    public void testFrameList() {
        ModelTestCore.testFrameList(this);
    }

    @Test
    @TargetPlatform(WEB_APP_NAME)
    public void testFrameMap() {
        ModelTestCore.testFrameMap(this);
    }
    
    @Test
    @TargetPlatform(WEB_APP_NAME)
    public void testShadowRootByLocator() {
        try {
            ModelTestCore.testShadowRootByLocator(this).run();
        } catch (ShadowRootContextException e) {
            throw new SkipException(e.getMessage(), e);
        }
    }

    @Test
    @TargetPlatform(WEB_APP_NAME)
    public void testShadowRootByElement() {
        try {
            ModelTestCore.testShadowRootByElement(this).run();
        } catch (ShadowRootContextException e) {
            throw new SkipException(e.getMessage(), e);
        }
    }
    
    @Test
    @TargetPlatform(WEB_APP_NAME)
    public void testShadowRootList() {
        try {
            ModelTestCore.testShadowRootList(this).run();
        } catch (ShadowRootContextException e) {
            throw new SkipException(e.getMessage(), e);
        }
    }

    @Test
    @TargetPlatform(WEB_APP_NAME)
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
    @TargetPlatform(WEB_APP_NAME)
    public void testRefresh() {
        ModelTestCore.testRefresh(this);
    }

    @Test
    @TargetPlatform(WEB_APP_NAME)
    public void testCssOptional() {
    	ModelTestCore.testCssOptional(this);
    }
    
    @Test
    @TargetPlatform(WEB_APP_NAME)
    public void testXpathOptional() {
    	ModelTestCore.testXpathOptional(this);
    }
    
    @Test
    @TargetPlatform(WEB_APP_NAME)
    public void testBogusOptional() {
    	ModelTestCore.testBogusOptional(this);
    }
    
    @Test
    @TargetPlatform(WEB_APP_NAME)
    public void testShadowParagraphs() {
        try {
            ModelTestCore.testShadowParagraphs(this).run();
        } catch (ShadowRootContextException e) {
            throw new SkipException(e.getMessage(), e);
        }
    }

}
