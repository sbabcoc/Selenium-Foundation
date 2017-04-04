package com.nordstrom.automation.selenium.model;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;

public class Page extends ComponentContainer {
	
	private String windowHandle;
	private WindowState windowState;
	
	public enum WindowState {
		WILL_OPEN, 
		WILL_CLOSE, 
		DID_OPEN, 
		DID_CLOSE
	}

	public Page(WebDriver driver) {
		super(driver, null);
	}
	
	public Page(SearchContext context, Frame parent) {
		super(context, parent);
		// FIXME - Must be set by interceptor. This won't work for actions that spawn new windows.
		windowHandle = driver.getWindowHandle();
	}
	
	@Override
	protected WebDriver switchToContext() {
		return driver.switchTo().window(windowHandle);
	}

}
