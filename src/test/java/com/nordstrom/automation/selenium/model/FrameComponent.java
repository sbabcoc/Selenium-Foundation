package com.nordstrom.automation.selenium.model;

import org.openqa.selenium.By;

public class FrameComponent extends Frame {
	
	public FrameComponent(By locator, ComponentContainer parent) {
		super(locator, parent);
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

}
