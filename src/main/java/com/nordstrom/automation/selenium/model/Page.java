package com.nordstrom.automation.selenium.model;

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
		// FIXME - Must be set by interceptor. This won't work for actions that spawn new windows.
		windowHandle = driver.getWindowHandle();
	}
	
	Page(WebDriver driver, ComponentContainer parent) {
		super(driver, parent);
	}
	
	public String getTitle() {
		return driver.getTitle();
	}
	
	@Override
	protected WebDriver switchToContext() {
		// if this is frame content
		if (windowHandle == null) {
			return driver;
		} else {
			return driver.switchTo().window(windowHandle);
		}
	}

}
