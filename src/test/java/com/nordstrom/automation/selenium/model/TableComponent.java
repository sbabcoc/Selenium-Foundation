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
