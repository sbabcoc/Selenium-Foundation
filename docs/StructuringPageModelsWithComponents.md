###### ExamplePage.java
```java
package com.nordstrom.automation.selenium.model;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

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

	protected enum Using implements ByEnum {
		HDR_ROW(By.cssSelector("tr[id*='-h']")),
		TBL_ROW(By.cssSelector("tr[id*='-r']"));
		
		private By locator;
		
		Using(By locator) {
			this.locator = locator;
		}

		@Override
		public By locator() {
			return locator;
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
	
	protected enum Using implements ByEnum {
		TBL_CELL(By.cssSelector("th,td"));
		
		private By locator;
		
		Using(By locator) {
			this.locator = locator;
		}

		@Override
		public By locator() {
			return locator;
		}
	}
	
	private List<WebElement> cells;

	public List<String> getContent() {
		List<WebElement> cells = getCells();
		return Arrays.asList(cells.get(0).getText(), cells.get(1).getText(), cells.get(2).getText());
	}
	
	private List<WebElement> getCells() {
		if (cells == null) {
			cells = findElements(Using.TBL_CELL);
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
	
	private enum Using implements ByEnum {
		HEADING(By.cssSelector("h1"));
		
		private By locator;
		
		Using(By locator) {
			this.locator = locator;
		}

		@Override
		public By locator() {
			return locator;
		}
	}
	
	public String getPageContent() {
		return findElement(Using.HEADING).getText();
	}

	public static Object getKey(SearchContext context) {
		return ((WebElement) context).getAttribute("id");
	}
}
```

# Page Component Search Contexts



# Driver Focus with Frame-Based Components

# Component Nesting and Aggregation

# Component Collections (Lists and Maps)