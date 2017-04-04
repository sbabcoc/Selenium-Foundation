package com.nordstrom.automation.selenium.model;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class Page extends ComponentContainer {
	
	private String windowHandle;

	public Page(WebDriver driver) {
		super(driver, null);
	}
	
	public Page(SearchContext context, Frame parent) {
		super(context, parent);
		windowHandle = driver.getWindowHandle();
	}
	
	@Override
	protected WebDriver switchToContext() {
		return driver.switchTo().window(windowHandle);
	}

}
