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
	
	protected enum Using {
		TBL_CELL(By.cssSelector("th,td"));
		
		private By selector;
		
		Using(By selector) {
			this.selector = selector;
		}
	}
	
	List<WebElement> cells;
	
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
