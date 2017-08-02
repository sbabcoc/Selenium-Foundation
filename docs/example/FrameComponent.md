| [ModelTest.java](ModelTest.md) | [ExamplePage.java](ExamplePage.md) | [TableComponent.java](TableComponent.md) | [TableRowComponent.java](TableRowComponent.md) | **FrameComponent.java** |

# Sample Code

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
		RobustWebElement element = (RobustWebElement) context;
		WebDriver driver = element.getWrappedDriver().switchTo().frame(element);
		Object key = driver.findElement(Using.HEADING.selector).getText();
		switchToParentFrame(element);
		return key;
	}
}
```