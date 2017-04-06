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
	
	/**
	 * Constructor for main document context
	 * 
	 * @param driver driver object
	 */
	public Page(WebDriver driver) {
		super(driver, null);
		// FIXME - Must be set by interceptor. This won't work for actions that spawn new windows.
		windowHandle = driver.getWindowHandle();
	}
	
	/**
	 * Constructor for frame-based document context<br>
	 * <br>
	 * <b>NOTE</b>: This package-private constructor is reserved for the {@link Frame} class
	 * 
	 * @param driver driver object
	 * @param parent page parent
	 */
	Page(WebDriver driver, ComponentContainer parent) {
		super(driver, parent);
	}
	
	@Override
	protected void validateParent(ComponentContainer parent) {
		// Page objects can omit parent 
	}
	
	/**
	 * Get page title
	 * 
	 * @return page title
	 */
	public String getTitle() {
		return driver.getTitle();
	}
	
	@Override
	protected WebDriver switchToContext() {
		return driver.switchTo().window(windowHandle);
	}

}
