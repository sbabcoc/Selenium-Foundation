package com.nordstrom.automation.selenium.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class TableComponent extends PageComponent {

	public TableComponent(By locator, ComponentContainer parent) {
		super(locator, parent);
	}
	
	protected enum Using {
		HDR_ROW(By.cssSelector("tr[id*='-h']")),
		TBL_ROW(By.cssSelector("tr[id*='-r']")),
		HDR_CELL(By.cssSelector("th")),
		TBL_CELL(By.cssSelector("td"));
		
		private By selector;
		
		Using(By selector) {
			this.selector = selector;
		}
	}
	
	public List<String> getHeadings() {
		WebElement headerRow = findElement(Using.HDR_ROW.selector);
		List<WebElement> headerCells = headerRow.findElements(Using.HDR_CELL.selector);
		return Arrays.asList(headerCells.get(0).getText(), headerCells.get(1).getText(), headerCells.get(2).getText());
	}
	
	public List<List<String>> getContent() {
		List<List<String>> content = new ArrayList<>();
		List<WebElement> tableRows = findElements(Using.TBL_ROW.selector);
		for (WebElement thisRow : tableRows) {
			List<WebElement> rowCells = thisRow.findElements(Using.TBL_CELL.selector);
			content.add(Arrays.asList(rowCells.get(0).getText(), rowCells.get(1).getText(), rowCells.get(2).getText()));
		}
		return content;
	}

}
