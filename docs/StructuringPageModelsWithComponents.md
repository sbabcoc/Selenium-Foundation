# Introduction

By applying the page-model pattern, you can produce a cohesive, behavior-based API that centralizes the nuts and bolts of interacting with the target application in your page classes. When the application under test changes (and it will), your tests will typically be unaffected; all of the updates will be isolated to the page classes.

However, modeling an application based solely on its pages produces a very flat model. It's quite common for a web application page to contain groups of elements that are logically associated (e.g. - billing address on an order information page). It's also common to encounter pages with multiple occurrences of an element grouping (e.g. - item tiles on a search results page). Factoring these grouping out into **page components** can greatly enrich your models, presenting a conceptual framework that automation developers will recognize.

If your target application uses frames to structure its content, you will be amazed at the ease with which your models interact with them. With automatic driver targeting, **Selenium Foundation** entirely removes explicit context switching from your implementation, allowing you to focus on functionality instead. More on this later.

###### ExamplePage.java
```java
package com.nordstrom.automation.selenium.model;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class ExamplePage extends Page {

	public ExamplePage(WebDriver driver) {
		super(driver);
	}
	
	private TableComponent table;
	private Map<Object, FrameComponent> frameMap;

	protected enum Using implements ByEnum {
		FRAME(By.cssSelector("iframe[id^='frame-']")),
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
	
	public Map<Object, FrameComponent> getFrameMap() {
		if (frameMap == null) {
			frameMap = newFrameMap(FrameComponent.class, Using.FRAME.locator);
		}
		return frameMap;
	}
}
```

In the preceding example page class, extracted from the **Selenium Foundation** unit tests, we see that subsets of page functionality have been factored out into two components - <span style="color: rgb(0, 0, 255);">TableComponent</span> and <span style="color: rgb(0, 0, 255);">FrameComponent</span>. The application page modeled by this class contains one table and three frames, which are represented by the model as a table component and a mapped collection of frame components. Definitions and descriptions of these components appear below.

# Page Component Search Contexts

In the **Selenium WebDriver** API, a search context is defined by the range of elements that will be examined when searching for a specified locator. For a page object, the search context is the entire page. For a page component, the search context is all of the elements within the bounds of the component's container element. For a frame, the search context is all of the elements within the bounds of the frame element.

The search context of a page component encompasses a subset of the elements of the context(s) in which it is contained. In other words, elements within the bounds of the component can be found by searches in encompassing contexts. However, the same cannot be said for frames, because...

## Frames define distinct search contexts

The preceding descriptions of search contexts omits one important detail - frames define distinct search contexts. Elements within the bounds of a frame **cannot** be found by searches in encompassing contexts. In the browser, frames are handled as separate documents. From a conceptual standpoint, a frame **IS-A** page, and the <span style="color: rgb(0, 0, 255);">Frame</span> class of **Selenium Foundation** models this concept by extending the <span style="color: rgb(0, 0, 255);">Page</span> class.

The search context of a frame is completely isolated, and the driver target needs to be switched to this context to interact with it. Without **Selenium Foundation**, the task of frame driver targeting can be frustrating and confusing. Once you've modeled a frame as a <span style="color: rgb(0, 0, 255);">Frame</span> object, **Selenium Foundation** handles driver targeting for you automatically. More on this below.

## A word about XPath locators

XPath locators differ from all other locator types in that they can traverse outside the scope of the target search context. For page and frame containers, this is purely academic, as their search contexts automatically encompass the entire document. For page component, however, you need to make sure you write your XPath expressions so they only evaluate element within the bounds of the page component search context.



# Driver Focus with Frame-Based Components

# Component Nesting and Aggregation

###### TableComponent.java
```java
package com.nordstrom.automation.selenium.model;

import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;

public class TableComponent extends PageComponent {

	public TableComponent(By locator, ComponentContainer parent) {
		super(locator, parent);
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
}
```

###### TableRowComponent.java
```java
package com.nordstrom.automation.selenium.model;

import java.util.Arrays;
import java.util.List;
import org.openqa.selenium.By;
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

# Component Collections (Lists and Maps)

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
	
	public FrameComponent(RobustWebElement element, ComponentContainer parent) {
		super(element, parent);
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

## Lazy initialization

