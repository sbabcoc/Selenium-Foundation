package com.nordstrom.automation.selenium.core;

import static com.nordstrom.automation.selenium.examples.ExamplePage.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;

import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.examples.ExamplePage;
import com.nordstrom.automation.selenium.examples.FormComponent;
import com.nordstrom.automation.selenium.examples.FrameComponent;
import com.nordstrom.automation.selenium.examples.ShadowRootComponent;
import com.nordstrom.automation.selenium.examples.TableComponent;
import com.nordstrom.automation.selenium.exceptions.ElementReferenceRefreshFailureException;
import com.nordstrom.automation.selenium.model.Enhanced;
import com.nordstrom.automation.selenium.model.RobustWebElement;

@InitialPage(ExamplePage.class)
public class ModelTestCore {

    public static void testBasicPage(TestBase instance) {
        ExamplePage page = instance.getInitialPage();
        assertEquals(page.getTitle(), TITLE);
        assertTrue(page instanceof Enhanced);
    }
    
    public static void updateTextInputSameValue(TestBase instance) {
        ExamplePage page = instance.getInitialPage();
        assertFalse(page.setInputValue("Nordstrom"));
        assertEquals(page.getInputValue(), "Nordstrom");
    }

    public static void updateTextInputNewValue(TestBase instance) {
        ExamplePage page = instance.getInitialPage();
        assertTrue(page.setInputValue("HauteLook"));
        assertEquals("HauteLook", page.getInputValue());
    }

    public static void updateTextInputBoolValue(TestBase instance) {
        ExamplePage page = instance.getInitialPage();
        assertTrue(page.setInputValue(true));
        assertEquals("true", page.getInputValue());
    }

    public static void updateTextInputNullValue(TestBase instance) {
        ExamplePage page = instance.getInitialPage();
        assertTrue(page.setInputValue(null));
        assertEquals("", page.getInputValue());
    }

    public static void updateCheckboxSameValue(TestBase instance) {
        ExamplePage page = instance.getInitialPage();
        assertFalse(page.setCheckValue(false));
        assertFalse(page.isBoxChecked());
    }

    public static void updateCheckboxNewValue(TestBase instance) {
        ExamplePage page = instance.getInitialPage();
        assertTrue(page.setCheckValue(true));
        assertTrue(page.isBoxChecked());
    }

    public static void updateCheckboxStringValue(TestBase instance) {
        ExamplePage page = instance.getInitialPage();
        assertTrue(page.setCheckValue("true"));
        assertTrue(page.isBoxChecked());
    }

    public static void updateCheckboxNullValue(TestBase instance) {
        ExamplePage page = instance.getInitialPage();
        assertFalse(page.setCheckValue(null));
        assertFalse(page.isBoxChecked());
    }

    public static void testParagraphs(TestBase instance) {
        ExamplePage page = instance.getInitialPage();
        List<String> paraList = page.getParagraphs();
        assertEquals(paraList.size(), 4);
        assertArrayEquals(PARAS, paraList.toArray());
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
        assertArrayEquals(HEADINGS, component.getHeadings().toArray());
        List<List<String>> content = component.getContent();
        assertEquals(content.size(), 3);
        assertArrayEquals(CONTENT[0], content.get(0).toArray());
        assertArrayEquals(CONTENT[1], content.get(1).toArray());
        assertArrayEquals(CONTENT[2], content.get(2).toArray());
        assertTrue(component instanceof Enhanced);
    }
    
    public static void testFrameByLocator(TestBase instance) {
        ExamplePage page = instance.getInitialPage();
        FrameComponent component = page.getFrameByLocator();
        assertEquals(FRAME_A, component.getPageContent());
        assertTrue(component instanceof Enhanced);
    }
    
    public static void testFrameByElement(TestBase instance) {
        ExamplePage page = instance.getInitialPage();
        FrameComponent component = page.getFrameByElement();
        assertEquals(FRAME_B, component.getPageContent());
        assertTrue(component instanceof Enhanced);
    }
    
    public static void testFrameByIndex(TestBase instance) {
        ExamplePage page = instance.getInitialPage();
        FrameComponent component = page.getFrameByIndex();
        assertEquals(FRAME_C, component.getPageContent());
        assertTrue(component instanceof Enhanced);
    }
    
    public static void testFrameById(TestBase instance) {
        ExamplePage page = instance.getInitialPage();
        FrameComponent component = page.getFrameById();
        assertEquals(FRAME_D, component.getPageContent());
        assertTrue(component instanceof Enhanced);
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
        assertEquals(4, frameList.size());
        assertEquals(FRAME_A, frameList.get(0).getPageContent());
        assertTrue(frameList.get(0) instanceof Enhanced);
        assertEquals(FRAME_B, frameList.get(1).getPageContent());
        assertTrue(frameList.get(1) instanceof Enhanced);
        assertEquals(FRAME_C, frameList.get(2).getPageContent());
        assertTrue(frameList.get(2) instanceof Enhanced);
        assertEquals(FRAME_D, frameList.get(3).getPageContent());
        assertTrue(frameList.get(3) instanceof Enhanced);
    }
    
    public static void testFrameMap(TestBase instance) {
        ExamplePage page = instance.getInitialPage();
        Map<Object, FrameComponent> frameMap = page.getFrameMap();
        assertEquals(4, frameMap.size());
        assertEquals(FRAME_A, frameMap.get(FRAME_A).getPageContent());
        assertTrue(frameMap.get(FRAME_A) instanceof Enhanced);
        assertEquals(FRAME_B, frameMap.get(FRAME_B).getPageContent());
        assertTrue(frameMap.get(FRAME_B) instanceof Enhanced);
        assertEquals(FRAME_C, frameMap.get(FRAME_C).getPageContent());
        assertTrue(frameMap.get(FRAME_C) instanceof Enhanced);
        assertEquals(FRAME_D, frameMap.get(FRAME_D).getPageContent());
        assertTrue(frameMap.get(FRAME_D) instanceof Enhanced);
    }
    
    public static Runnable testShadowRootByLocator(final TestBase instance) {
        return new Runnable() {
            final ExamplePage page = instance.getInitialPage();

            @Override
            public void run() {
                ShadowRootComponent shadowRoot = page.getShadowRootByLocator();
                assertEquals(SHADOW_DOM_A, shadowRoot.getHeading());
                assertTrue(shadowRoot instanceof Enhanced);
            }
        };
    }
    
    public static Runnable testShadowRootByElement(final TestBase instance) {
        return new Runnable() {
            final ExamplePage page = instance.getInitialPage();

            @Override
            public void run() {
                ShadowRootComponent shadowRoot = page.getShadowRootByElement();
                assertEquals(SHADOW_DOM_B, shadowRoot.getHeading());
                assertTrue(shadowRoot instanceof Enhanced);
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
                assertEquals(SHADOW_DOM_A, shadowRootList.get(0).getHeading());
                assertTrue(shadowRootList.get(0) instanceof Enhanced);
                assertEquals(SHADOW_DOM_B, shadowRootList.get(1).getHeading());
                assertTrue(shadowRootList.get(1) instanceof Enhanced);
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
                assertEquals(SHADOW_DOM_A, shadowRootMap.get(SHADOW_DOM_A).getHeading());
                assertTrue(shadowRootMap.get(SHADOW_DOM_A) instanceof Enhanced);
                assertEquals(SHADOW_DOM_B, shadowRootMap.get(SHADOW_DOM_B).getHeading());
                assertTrue(shadowRootMap.get(SHADOW_DOM_B) instanceof Enhanced);
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
        assertEquals(0, pageRefreshCount);
        assertEquals(0, tableRefreshCount);
        assertEquals(0, headRefreshCount);
        assertArrayEquals(new int[] {0, 0, 0}, bodyRefreshCounts);
        
        // refresh page to force DOM rebuild
        page.getWrappedDriver().navigate().refresh();
        // verify table contents
        // NOTE: This necessitates refreshing stale element references
        verifyTable(component);
        
        // get current refresh counts
        pageRefreshCount = page.getRefreshCount();
        tableRefreshCount = component.getRefreshCount();
        headRefreshCount = component.getHeadRefreshCount();
        bodyRefreshCounts = component.getBodyRefreshCounts();
        
        // 1 page refresh request from its table context
        assertEquals(1, pageRefreshCount);
        // 1 table refresh request from each of its four row contexts
        assertEquals(4, tableRefreshCount);
        // 1 head row refresh request from one of its web element contexts
        assertEquals(1, headRefreshCount);
        // 1 refresh request per body row from one of its web element contexts
        assertArrayEquals(new int[] {1, 1, 1}, bodyRefreshCounts);
        
        // verify table contents again
        // NOTE: No additional refresh requests are expected
        verifyTable(component);
        
        // get current refresh counts
        pageRefreshCount = page.getRefreshCount();
        tableRefreshCount = component.getRefreshCount();
        headRefreshCount = component.getHeadRefreshCount();
        bodyRefreshCounts = component.getBodyRefreshCounts();
        
        // verify no additional refresh requests
        assertEquals(1, pageRefreshCount);
        assertEquals(4, tableRefreshCount);
        assertEquals(1, headRefreshCount);
        assertArrayEquals(new int[] {1, 1, 1}, bodyRefreshCounts);
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

    public static void testOptionalBehavior(TestBase instance) {
        ExamplePage page = instance.getInitialPage();
        FormComponent form = page.getForm();
        RobustWebElement optional = form.getOptional();
        assertFalse("Optional node should initially be absent", optional.hasReference());
        assertTrue("Failed appending optional node", form.toggleOptionalNode());
        assertTrue("Failed finding appended optional node", optional.hasReference());
        assertEquals("Optional node context mismatch", "I'm optional", optional.getText());
        assertFalse("Failed removing optional node", form.toggleOptionalNode());

        try {
            optional.getTagName();
            fail("No exception thrown for removed node");
        } catch (StaleElementReferenceException e) {
            assertFalse("Failed clearing reference for optional node", optional.hasReference());
            assertNotEquals("Refresh should not have been attempted for optional node",
                    ElementReferenceRefreshFailureException.class, e.getClass());
        }

        assertTrue("Failed appending optional node", form.toggleOptionalNode());
        assertEquals("Optional node context mismatch", "I'm optional", optional.getText());
        
        int count = page.getRefreshCount();
        page.getWrappedDriver().navigate().refresh();
        assertTrue("Failed appending optional node", form.toggleOptionalNode());
        assertEquals("Optional node context mismatch", "I'm optional", optional.getText());
        assertEquals("Page refresh count not incremented", count + 1, page.getRefreshCount());
    }
    
    public static void testReferenceRefreshFailure(TestBase instance) {
        ExamplePage page = instance.getInitialPage();
        FormComponent form = page.getForm();
        assertTrue("Failed appending optional node", form.toggleOptionalNode());
        WebElement required = form.getRequired();
        assertEquals("Optional node context mismatch", "I'm optional", required.getText());
        assertFalse("Failed removing optional node", form.toggleOptionalNode());
        required.getTagName();
        fail("No exception thrown for removed node");
    }
    
    public static Runnable testShadowParagraphs(final TestBase instance) {
        return new Runnable() {
            final ExamplePage page = instance.getInitialPage();

            @Override
            public void run() {
                ShadowRootComponent shadowRoot = page.getShadowRootByLocator();
                List<String> paraList = shadowRoot.getParagraphs();
                assertEquals(3, paraList.size());
                
                String[] expect = new String[3];
                String heading = shadowRoot.getHeading();
                String marker = String.format("[%s] ", heading.substring(11));
                for (int i = 0; i < 3; i++) {
                    expect[i] = marker + PARAS[i];
                }
                assertArrayEquals(expect, paraList.toArray());
            }
        };
    }
    
}
