package com.nordstrom.automation.selenium.model;

import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.nordstrom.automation.selenium.core.ByType.ByEnum;

public class ExamplePage extends Page {

	public ExamplePage(WebDriver driver) {
		super(driver);
	}
	
	private FrameComponent frameByElement;
	private FrameComponent frameByIndex;
	private FrameComponent frameById;
	private TableComponent table;
	
	protected static final String FRAME_A_ID = "frame-a";
	protected static final String FRAME_B_ID = "frame-b";
	protected static final String FRAME_C_ID = "frame-c";
	
	protected enum Using implements ByEnum {
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
		List<WebElement> paraList = findElements(Using.PARA.locator);
		return Arrays.asList(paraList.get(0).getText(), paraList.get(1).getText(), paraList.get(2).getText());
	}
	
	public TableComponent getTable() {
		if (table == null) {
			table = new TableComponent(Using.TABLE.locator, this);
		}
		return table;
	}
}
