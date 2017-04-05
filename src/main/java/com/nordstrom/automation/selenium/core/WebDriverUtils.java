package com.nordstrom.automation.selenium.core;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.internal.WrapsDriver;

public class WebDriverUtils {
	
	private WebDriverUtils() {
		throw new UnsupportedOperationException("WebDriverUtils is a static utility class that cannot be instantiated");
	}
	
	/**
	 * Get the driver associated with the specified search context
	 * 
	 * @param context search context
	 * @return search context driver
	 */
	public static WebDriver getDriver(SearchContext context) {
		if (context instanceof WebDriver) {
			return (WebDriver) context;
		} else if (context instanceof WrapsDriver) {
			return ((WrapsDriver) context).getWrappedDriver();
		} else {
			throw new UnsupportedOperationException("Unable to extract the driver from the specified context");
		}
	}

}
