| **ModelTest.java** | [ExamplePage.java](ExamplePage.md) | [TableComponent.java](TableComponent.md) | [TableRowComponent.java](TableRowComponent.md) | [FrameComponent.java](FrameComponent.md) | [ShadowRootComponent.java](ShadowRootComponent.md) |

# Sample Code

###### ModelTest.java
```java
package com.nordstrom.automation.selenium.model;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.testng.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.testng.SkipException;
import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.exceptions.ShadowRootContextException;
import com.nordstrom.automation.selenium.support.TestNgBase;

@InitialPage(ExamplePage.class)
public class ModelTest extends TestNgBase {
    
    private static final String TITLE = "Example Page";
    private static final String[] PARAS = {"This is paragraph one.", "This is paragraph two.", "This is paragraph three."};
    private static final String[] HEADINGS = {"Firstname", "Lastname", "Age"};
    private static final String[][] CONTENT = {{"Jill", "Smith", "50"}, {"Eve", "Jackson", "94"}, {"John", "Doe", "80"}};
    private static final String FRAME_A = "Frame A";
    private static final String FRAME_B = "Frame B";
    private static final String FRAME_C = "Frame C";
    private static final String FRAME_D = "Frame D";
    private static final String TABLE_ID = "t1";
    private static final String SHADOW_DOM_A = "Shadow DOM A";
    private static final String SHADOW_DOM_B = "Shadow DOM B";
    
    @Test
    public void testBasicPage() {
        ExamplePage page = getPage();
        assertEquals(page.getTitle(), TITLE);
    }
    
    @Test
    public void testParagraphs() {
        ExamplePage page = getPage();
        List<String> paraList = page.getParagraphs();
        assertEquals(paraList.size(), 3);
        assertArrayEquals(paraList.toArray(), PARAS);
    }
    
    @Test
    public void testTable() {
        ExamplePage page = getPage();
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
    
    @Test
    public void testFrameByLocator() {
        ExamplePage page = getPage();
        FrameComponent component = page.getFrameByLocator();
        assertEquals(component.getPageContent(), FRAME_A);
    }
    
    @Test
    public void testFrameByElement() {
        ExamplePage page = getPage();
        FrameComponent component = page.getFrameByElement();
        assertEquals(component.getPageContent(), FRAME_B);
    }
    
    @Test
    public void testFrameByIndex() {
        ExamplePage page = getPage();
        FrameComponent component = page.getFrameByIndex();
        assertEquals(component.getPageContent(), FRAME_C);
    }
    
    @Test
    public void testFrameById() {
        ExamplePage page = getPage();
        FrameComponent component = page.getFrameById();
        assertEquals(component.getPageContent(), FRAME_D);
    }
    
    @Test
    public void testComponentList() {
        ExamplePage page = getPage();
        List<TableComponent> componentList = page.getTableList();
        verifyTable(componentList.get(0));
    }
    
    @Test
    public void testComponentMap() {
        ExamplePage page = getPage();
        Map<Object, TableComponent> componentMap = page.getTableMap();
        verifyTable(componentMap.get(TABLE_ID));
    }
    
    @Test
    public void testFrameList() {
        ExamplePage page = getPage();
        List<FrameComponent> frameList = page.getFrameList();
        assertEquals(frameList.size(), 4);
        assertEquals(frameList.get(0).getPageContent(), FRAME_A);
        assertEquals(frameList.get(1).getPageContent(), FRAME_B);
        assertEquals(frameList.get(2).getPageContent(), FRAME_C);
        assertEquals(frameList.get(3).getPageContent(), FRAME_D);
    }
    
    @Test
    public void testFrameMap() {
        ExamplePage page = getPage();
        Map<Object, FrameComponent> frameMap = page.getFrameMap();
        assertEquals(frameMap.size(), 4);
        assertEquals(frameMap.get(FRAME_A).getPageContent(), FRAME_A);
        assertEquals(frameMap.get(FRAME_B).getPageContent(), FRAME_B);
        assertEquals(frameMap.get(FRAME_C).getPageContent(), FRAME_C);
        assertEquals(frameMap.get(FRAME_D).getPageContent(), FRAME_D);
    }
    
    @Test
    public void testShadowRootByLocator() {
        ExamplePage page = getPage();
        try {
            ShadowRootComponent shadowRoot = page.getShadowRootByLocator();
            assertEquals(shadowRoot.getContent(), SHADOW_DOM_A);
        } catch (ShadowRootContextException e) {
            throw new SkipException(e.getMessage(), e);
        }
    }
    
    @Test
    public void testShadowRootByElement() {
        ExamplePage page = getPage();
        try {
            ShadowRootComponent shadowRoot = page.getShadowRootByElement();
            assertEquals(shadowRoot.getContent(), SHADOW_DOM_B);
        } catch (ShadowRootContextException e) {
            throw new SkipException(e.getMessage(), e);
        }
    }
    
    private ExamplePage getPage() {
        return (ExamplePage) getInitialPage();
    }
}
```