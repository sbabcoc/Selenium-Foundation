# Sample Code

The following 

###### ModelTest.java <a name="model-test"></a>
```java
package com.nordstrom.automation.selenium.model;

import static org.testng.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.listeners.DriverManager;
import com.nordstrom.automation.testng.ExecutionFlowController;
import com.nordstrom.automation.testng.ListenerChain;
import com.nordstrom.automation.testng.ListenerChainable;

@InitialPage(ExamplePage.class)
@Listeners({ListenerChain.class})
public class ModelTest implements ListenerChainable {
	
	private static final String TITLE = "Example Page";
	private static final String[] PARAS = {"This is paragraph one.", "This is paragraph two.", "This is paragraph three."};
	private static final String[] HEADINGS = {"Firstname", "Lastname", "Age"};
	private static final String[][] CONTENT = {{"Jill", "Smith", "50"}, {"Eve", "Jackson", "94"}, {"John", "Doe", "80"}};
	private static final String FRAME_A = "Frame A";
	private static final String FRAME_A_ID = "frame-a";
	private static final String FRAME_B = "Frame B";
	private static final String FRAME_B_ID = "frame-b";
	private static final String FRAME_C = "Frame C";
	private static final String FRAME_C_ID = "frame-c";
	private static final String TABLE_ID = "t1";
	
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
		assertEquals(paraList.toArray(), PARAS);
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
		assertEquals(component.getHeadings().toArray(), HEADINGS);
		List<List<String>> content = component.getContent();
		assertEquals(content.size(), 3);
		assertEquals(content.get(0).toArray(), CONTENT[0]);
		assertEquals(content.get(1).toArray(), CONTENT[1]);
		assertEquals(content.get(2).toArray(), CONTENT[2]);
	}
	
	@Test
	public void testFrameByElement() {
		ExamplePage page = getPage();
		FrameComponent component = page.getFrameByElement();
		assertEquals(component.getPageContent(), FRAME_A);
	}

	@Test
	public void testFrameByIndex() {
		ExamplePage page = getPage();
		FrameComponent component = page.getFrameByIndex();
		assertEquals(component.getPageContent(), FRAME_B);
	}

	@Test
	public void testFrameById() {
		ExamplePage page = getPage();
		FrameComponent component = page.getFrameById();
		assertEquals(component.getPageContent(), FRAME_C);
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
		assertEquals(frameList.size(), 3);
		assertEquals(frameList.get(0).getPageContent(), FRAME_A);
		assertEquals(frameList.get(1).getPageContent(), FRAME_B);
		assertEquals(frameList.get(2).getPageContent(), FRAME_C);
	}

	@Test
	public void testFrameMap() {
		ExamplePage page = getPage();
		Map<Object, FrameComponent> frameMap = page.getFrameMap();
		assertEquals(frameMap.size(), 3);
		assertEquals(frameMap.get(FRAME_A_ID).getPageContent(), FRAME_A);
		assertEquals(frameMap.get(FRAME_B_ID).getPageContent(), FRAME_B);
		assertEquals(frameMap.get(FRAME_C_ID).getPageContent(), FRAME_C);
	}

	private ExamplePage getPage() {
		return (ExamplePage) DriverManager.getInitialPage();
	}
	
	@Override
	public void attachListeners(ListenerChain listenerChain) {
		listenerChain.around(DriverManager.class).around(ExecutionFlowController.class);
	}
}
```

###### ExamplePage.java <a name="example-page"></a>
```java
package com.nordstrom.automation.selenium.model;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.nordstrom.automation.selenium.annotations.PageUrl;

@PageUrl("ExamplePage.html")
public class ExamplePage extends Page {

	public ExamplePage(WebDriver driver) {
		super(driver);
	}
	
	private FrameComponent frameByElement;
	private FrameComponent frameByIndex;
	private FrameComponent frameById;
	private TableComponent table;
	private List<TableComponent> tableList;
	private Map<Object, TableComponent> tableMap;
	private List<FrameComponent> frameList;
	private Map<Object, FrameComponent> frameMap;

	protected static final String FRAME_A_ID = "frame-a";
	protected static final String FRAME_B_ID = "frame-b";
	protected static final String FRAME_C_ID = "frame-c";
	
	protected enum Using implements ByEnum {
		FRAME(By.cssSelector("iframe[id^='frame-']")),
		FRAME_A(By.cssSelector("iframe#frame-a")),
		FRAME_B(By.cssSelector("iframe#frame-b")),
		FRAME_C(By.cssSelector("iframe#frame-c")),
		PARA(By.cssSelector("p[id^='para-']")),
		TABLE(By.cssSelector("table#t1"));
		
		private By locator;
		
		Using(By locator) {
			this.locator = locator;
		}

		@Override
		public By locator() {
			return locator;
		}
	}
	
	public FrameComponent getFrameByElement() {
		if (frameByElement == null) {
			frameByElement = new FrameComponent(Using.FRAME_A.locator, this);
		}
		return frameByElement;
	}
	
	public FrameComponent getFrameByIndex() {
		if (frameByIndex == null) {
			frameByIndex = new FrameComponent(1, this);
		}
		return frameByIndex;
	}
	
	public FrameComponent getFrameById() {
		if (frameById == null) {
			frameById = new FrameComponent(FRAME_C_ID, this);
		}
		return frameById;
	}
	
	public List<String> getParagraphs() {
		List<WebElement> paraList = findElements(Using.PARA);
		return Arrays.asList(paraList.get(0).getText(), paraList.get(1).getText(), paraList.get(2).getText());
	}
	
	public TableComponent getTable() {
		if (table == null) {
			table = new TableComponent(Using.TABLE.locator, this);
		}
		return table;
	}
	
	public List<TableComponent> getTableList() {
		if (tableList == null) {
			tableList = newComponentList(TableComponent.class, Using.TABLE.locator);
		}
		return tableList;
	}
	
	public Map<Object, TableComponent> getTableMap() {
		if (tableMap == null) {
			tableMap = newComponentMap(TableComponent.class, Using.TABLE.locator);
		}
		return tableMap;
	}
	
	public List<FrameComponent> getFrameList() {
		if (frameList == null) {
			frameList = newFrameList(FrameComponent.class, Using.FRAME.locator);
		}
		return frameList;
	}
	
	public Map<Object, FrameComponent> getFrameMap() {
		if (frameMap == null) {
			frameMap = newFrameMap(FrameComponent.class, Using.FRAME.locator);
		}
		return frameMap;
	}
}
```

###### TableComponent.java
```java
package com.nordstrom.automation.selenium.model;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

public class TableComponent extends PageComponent {

	public TableComponent(By locator, ComponentContainer parent) {
		super(locator, parent);
	}
	
	public TableComponent(RobustWebElement element, ComponentContainer parent) {
		super(element, parent);
	}
	
	private TableRowComponent tableHdr;
	private List<TableRowComponent> tableRows;

	protected enum Using {
		HDR_ROW(By.cssSelector("tr[id*='-h']")),
		TBL_ROW(By.cssSelector("tr[id*='-r']"));
		
		private By locator;
		
		Using(By locator) {
			this.locator = locator;
		}
	}
	
	public List<String> getHeadings() {
		return getTableHdr().getContent();
	}
	
	public List<List<String>> getContent() {
		List<List<String>> result = new ArrayList<>();
		for (TableRowComponent row : getTableRows()) {
			result.add(row.getContent());
		}
		return result;
	}
	
	private TableRowComponent getTableHdr() {
		if (tableHdr == null) {
			tableHdr = new TableRowComponent(Using.HDR_ROW.locator, this);
		}
		return tableHdr;
	}
	
	private List<TableRowComponent> getTableRows() {
		if (tableRows == null) {
			tableRows = new ComponentList<>(this, TableRowComponent.class, Using.TBL_ROW.locator);
		}
		return tableRows;
	}
	
	public static Object getKey(SearchContext context) {
		return ((WebElement) context).getAttribute("id");
	}
}
```

###### TableRowComponent.java
```java
package com.nordstrom.automation.selenium.model;

import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

public class TableRowComponent extends PageComponent {

	public TableRowComponent(By locator, ComponentContainer parent) {
		super(locator, parent);
	}
	
	public TableRowComponent(RobustWebElement element, ComponentContainer parent) {
		super(element, parent);
	}
	
	protected enum Using {
		TBL_CELL(By.cssSelector("th,td"));
		
		private By selector;
		
		Using(By selector) {
			this.selector = selector;
		}
	}
	
	private List<WebElement> cells;

	public List<String> getContent() {
		List<WebElement> cells = getCells();
		return Arrays.asList(cells.get(0).getText(), cells.get(1).getText(), cells.get(2).getText());
	}
	
	private List<WebElement> getCells() {
		if (cells == null) {
			cells = findElements(Using.TBL_CELL.selector);
		}
		return cells;
	}
}
```

###### FrameComponent.java
```java
package com.nordstrom.automation.selenium.model;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

public class FrameComponent extends Frame {
	
	public FrameComponent(By locator, ComponentContainer parent) {
		super(locator, parent);
	}
	
	public FrameComponent(By locator, int index, ComponentContainer parent) {
		super(locator, index, parent);
	}
	
	public FrameComponent(RobustWebElement element, ComponentContainer parent) {
		super(element, parent);
	}
	
	public FrameComponent(int index, ComponentContainer parent) {
		super(index, parent);
	}
	
	public FrameComponent(String nameOrId, ComponentContainer parent) {
		super(nameOrId, parent);
	}
	
	private enum Using {
		HEADING(By.cssSelector("h1"));
		
		private By selector;
		
		Using(By selector) {
			this.selector = selector;
		}
	}
	
	public String getPageContent() {
		return findElement(Using.HEADING.selector).getText();
	}

	public static Object getKey(SearchContext context) {
		return ((WebElement) context).getAttribute("id");
	}
}
```
