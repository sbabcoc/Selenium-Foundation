package com.nordstrom.automation.selenium.core;

import static com.nordstrom.automation.selenium.examples.ExamplePage.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.examples.ExamplePage;
import com.nordstrom.automation.selenium.examples.FrameComponent;
import com.nordstrom.automation.selenium.examples.ShadowRootComponent;
import com.nordstrom.automation.selenium.examples.TableComponent;

@InitialPage(ExamplePage.class)
public class ModelTestCore {

    public static void testBasicPage(TestBase instance) {
        ExamplePage page = instance.getInitialPage();
        assertEquals(page.getTitle(), TITLE);
    }
    
    public static void testParagraphs(TestBase instance) {
        ExamplePage page = instance.getInitialPage();
        List<String> paraList = page.getParagraphs();
        assertEquals(paraList.size(), 3);
        assertArrayEquals(paraList.toArray(), PARAS);
    }
    
    public static void testTable(TestBase instance) {
        ExamplePage page = instance.getInitialPage();
        TableComponent component = page.getTable();
        verifyTable(component);
    }
    
    /**
     * Verify the contents of the specified table component
     * 
     * @param component table component to be verified
     */
    private static void verifyTable(TableComponent component) {
        assertArrayEquals(component.getHeadings().toArray(), HEADINGS);
        List<List<String>> content = component.getContent();
        assertEquals(content.size(), 3);
        assertArrayEquals(content.get(0).toArray(), CONTENT[0]);
        assertArrayEquals(content.get(1).toArray(), CONTENT[1]);
        assertArrayEquals(content.get(2).toArray(), CONTENT[2]);
    }
    
    public static void testFrameByLocator(TestBase instance) {
        ExamplePage page = instance.getInitialPage();
        FrameComponent component = page.getFrameByLocator();
        assertEquals(component.getPageContent(), FRAME_A);
    }
    
    public static void testFrameByElement(TestBase instance) {
        ExamplePage page = instance.getInitialPage();
        FrameComponent component = page.getFrameByElement();
        assertEquals(component.getPageContent(), FRAME_B);
    }
    
    public static void testFrameByIndex(TestBase instance) {
        ExamplePage page = instance.getInitialPage();
        FrameComponent component = page.getFrameByIndex();
        assertEquals(component.getPageContent(), FRAME_C);
    }
    
    public static void testFrameById(TestBase instance) {
        ExamplePage page = instance.getInitialPage();
        FrameComponent component = page.getFrameById();
        assertEquals(component.getPageContent(), FRAME_D);
    }
    
    public static void testComponentList(TestBase instance) {
        ExamplePage page = instance.getInitialPage();
        List<TableComponent> componentList = page.getTableList();
        verifyTable(componentList.get(0));
    }
    
    public static void testComponentMap(TestBase instance) {
        ExamplePage page = instance.getInitialPage();
        Map<Object, TableComponent> componentMap = page.getTableMap();
        verifyTable(componentMap.get(TABLE_ID));
    }
    
    public static void testFrameList(TestBase instance) {
        ExamplePage page = instance.getInitialPage();
        List<FrameComponent> frameList = page.getFrameList();
        assertEquals(frameList.size(), 4);
        assertEquals(frameList.get(0).getPageContent(), FRAME_A);
        assertEquals(frameList.get(1).getPageContent(), FRAME_B);
        assertEquals(frameList.get(2).getPageContent(), FRAME_C);
        assertEquals(frameList.get(3).getPageContent(), FRAME_D);
    }
    
    public static void testFrameMap(TestBase instance) {
        ExamplePage page = instance.getInitialPage();
        Map<Object, FrameComponent> frameMap = page.getFrameMap();
        assertEquals(frameMap.size(), 4);
        assertEquals(frameMap.get(FRAME_A).getPageContent(), FRAME_A);
        assertEquals(frameMap.get(FRAME_B).getPageContent(), FRAME_B);
        assertEquals(frameMap.get(FRAME_C).getPageContent(), FRAME_C);
        assertEquals(frameMap.get(FRAME_D).getPageContent(), FRAME_D);
    }
    
    public static Runnable testShadowRootByLocator(final TestBase instance) {
        return new Runnable() {
            final ExamplePage page = instance.getInitialPage();

            @Override
            public void run() {
                ShadowRootComponent shadowRoot = page.getShadowRootByLocator();
                assertEquals(shadowRoot.getContent(), SHADOW_DOM_A);
            }
        };
    }
    
    public static Runnable testShadowRootByElement(final TestBase instance) {
        return new Runnable() {
            final ExamplePage page = instance.getInitialPage();

            @Override
            public void run() {
                ShadowRootComponent shadowRoot = page.getShadowRootByElement();
                assertEquals(shadowRoot.getContent(), SHADOW_DOM_B);
            }
        };
    }
    
    public static Runnable testShadowRootList(final TestBase instance) {
        return new Runnable() {
            final ExamplePage page = instance.getInitialPage();

            @Override
            public void run() {
                List<ShadowRootComponent> shadowRootList = page.getShadowRootList();
                assertEquals(shadowRootList.size(), 2);
                assertEquals(shadowRootList.get(0).getContent(), SHADOW_DOM_A);
                assertEquals(shadowRootList.get(1).getContent(), SHADOW_DOM_B);
            }
        };
    }
    
    public static Runnable testShadowRootMap(final TestBase instance) {
        return new Runnable() {
            final ExamplePage page = instance.getInitialPage();

            @Override
            public void run() {
                Map<Object, ShadowRootComponent> shadowRootMap = page.getShadowRootMap();
                assertEquals(shadowRootMap.size(), 2);
                assertEquals(shadowRootMap.get(SHADOW_DOM_A).getContent(), SHADOW_DOM_A);
                assertEquals(shadowRootMap.get(SHADOW_DOM_B).getContent(), SHADOW_DOM_B);
            }
        };
    }
    
    /**
     * This test verifies that stale elements are automatically refreshed
     * and that the search context chain gets refreshed efficiently.
     */
    public static void testRefresh(TestBase instance) {
        ExamplePage page = instance.getInitialPage();
        // get the table component
        TableComponent component = page.getTable();
        // verify table contents
        verifyTable(component);
        
        // get current refresh counts
        int pageRefreshCount = page.getRefreshCount();
        int tableRefreshCount = component.getRefreshCount();
        int headRefreshCount = component.getHeadRefreshCount();
        int[] bodyRefreshCounts = component.getBodyRefreshCounts();
        
        // verify no initial refresh requests
        assertEquals(pageRefreshCount, 0);
        assertEquals(tableRefreshCount, 0);
        assertEquals(headRefreshCount, 0);
        assertArrayEquals(bodyRefreshCounts, new int[] {0, 0, 0});
        
        // refresh page to force DOM rebuild
        page.getDriver().navigate().refresh();
        // verify table contents
        // NOTE: This necessitates refreshing stale element references
        verifyTable(component);
        
        // get current refresh counts
        pageRefreshCount = page.getRefreshCount();
        tableRefreshCount = component.getRefreshCount();
        headRefreshCount = component.getHeadRefreshCount();
        bodyRefreshCounts = component.getBodyRefreshCounts();
        
        // 1 page refresh request from its table context
        assertEquals(pageRefreshCount, 1);
        // 1 table refresh request from each of its four row contexts
        assertEquals(tableRefreshCount, 4);
        // 1 head row refresh request from one of its web element contexts
        assertEquals(headRefreshCount, 1);
        // 1 refresh request per body row from one of its web element contexts
        assertArrayEquals(bodyRefreshCounts, new int[] {1, 1, 1});
        
        // verify table contents again
        // NOTE: No additional refresh requests are expected
        verifyTable(component);
        
        // get current refresh counts
        pageRefreshCount = page.getRefreshCount();
        tableRefreshCount = component.getRefreshCount();
        headRefreshCount = component.getHeadRefreshCount();
        bodyRefreshCounts = component.getBodyRefreshCounts();
        
        // verify no additional refresh requests
        assertEquals(pageRefreshCount, 1);
        assertEquals(tableRefreshCount, 4);
        assertEquals(headRefreshCount, 1);
        assertArrayEquals(bodyRefreshCounts, new int[] {1, 1, 1});
    }
    
    public static void testCssOptional(TestBase instance) {
        ExamplePage page = instance.getInitialPage();
        assertTrue(page.hasCssOptional());
    }
    
    public static void testXpathOptional(TestBase instance) {
        ExamplePage page = instance.getInitialPage();
        assertTrue(page.hasXpathOptional());
    }
    
    public static void testBogusOptional(TestBase instance) {
        ExamplePage page = instance.getInitialPage();
        assertFalse(page.hasBogusOptional());
    }
    
}
