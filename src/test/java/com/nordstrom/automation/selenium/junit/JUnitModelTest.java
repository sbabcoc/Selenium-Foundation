package com.nordstrom.automation.selenium.junit;

import static org.junit.Assume.assumeNoException;
import static com.nordstrom.automation.selenium.platform.TargetType.WEB_APP_NAME;

import org.junit.Ignore;
import org.junit.Test;

import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.core.ModelTestCore;
import com.nordstrom.automation.selenium.examples.ExamplePage;
import com.nordstrom.automation.selenium.examples.JUnitTargetRoot;
import com.nordstrom.automation.selenium.exceptions.ShadowRootContextException;
import com.nordstrom.automation.selenium.platform.TargetPlatform;

@InitialPage(ExamplePage.class)
public class JUnitModelTest extends JUnitTargetRoot {

    @Test
    @TargetPlatform(WEB_APP_NAME)
    public void testBasicPage() {
        ModelTestCore.testBasicPage(this);
    }
    
    @Test
    @Ignore
    @TargetPlatform(WEB_APP_NAME)
    public void testParagraphs() {
        ModelTestCore.testParagraphs(this);
    }
    
    @Test
    @Ignore
    @TargetPlatform(WEB_APP_NAME)
    public void testTable() {
        ModelTestCore.testTable(this);
    }

    @Test
    @Ignore
    @TargetPlatform(WEB_APP_NAME)
    public void testFrameByLocator() {
        ModelTestCore.testFrameByLocator(this);
    }

    @Test
    @Ignore
    @TargetPlatform(WEB_APP_NAME)
    public void testFrameByElement() {
        ModelTestCore.testFrameByElement(this);
    }

    @Test
    @Ignore
    @TargetPlatform(WEB_APP_NAME)
    public void testFrameByIndex() {
        ModelTestCore.testFrameByIndex(this);
    }

    @Test
    @Ignore
    @TargetPlatform(WEB_APP_NAME)
    public void testFrameById() {
        ModelTestCore.testFrameById(this);
    }
    
    @Test
    @Ignore
    @TargetPlatform(WEB_APP_NAME)
    public void testComponentList() {
        ModelTestCore.testComponentList(this);
    }
    
    @Test
    @Ignore
    @TargetPlatform(WEB_APP_NAME)
    public void testComponentMap() {
        ModelTestCore.testComponentMap(this);
    }
    
    @Test
    @Ignore
    @TargetPlatform(WEB_APP_NAME)
    public void testFrameList() {
        ModelTestCore.testFrameList(this);
    }

    @Test
    @Ignore
    @TargetPlatform(WEB_APP_NAME)
    public void testFrameMap() {
        ModelTestCore.testFrameMap(this);
    }

    @Test
    @Ignore
    @TargetPlatform(WEB_APP_NAME)
    public void testShadowRootByLocator() {
        try {
            ModelTestCore.testShadowRootByLocator(this).run();
        } catch (ShadowRootContextException e) {
            assumeNoException(e);
        }
    }

    @Test
    @Ignore
    @TargetPlatform(WEB_APP_NAME)
    public void testShadowRootByElement() {
        try {
            ModelTestCore.testShadowRootByElement(this).run();
        } catch (ShadowRootContextException e) {
            assumeNoException(e);
        }
    }
    
    @Test
    @Ignore
    @TargetPlatform(WEB_APP_NAME)
    public void testShadowRootList() {
        try {
            ModelTestCore.testShadowRootList(this).run();
        } catch (ShadowRootContextException e) {
            assumeNoException(e);
        }
    }

    @Test
    @Ignore
    @TargetPlatform(WEB_APP_NAME)
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
    @TargetPlatform(WEB_APP_NAME)
    public void testRefresh() {
        ModelTestCore.testRefresh(this);
    }

    @Test
    @Ignore
    @TargetPlatform(WEB_APP_NAME)
    public void testCssOptional() {
    	ModelTestCore.testCssOptional(this);
    }
    
    @Test
    @Ignore
    @TargetPlatform(WEB_APP_NAME)
    public void testXpathOptional() {
    	ModelTestCore.testXpathOptional(this);
    }
    
    @Test
    @Ignore
    @TargetPlatform(WEB_APP_NAME)
    public void testBogusOptional() {
    	ModelTestCore.testBogusOptional(this);
    }
    
    @Test
    @Ignore
    @TargetPlatform(WEB_APP_NAME)
    public void testShadowParagraphs() {
        try {
            ModelTestCore.testShadowParagraphs(this).run();
        } catch (ShadowRootContextException e) {
            assumeNoException(e);
        }
    }

}
