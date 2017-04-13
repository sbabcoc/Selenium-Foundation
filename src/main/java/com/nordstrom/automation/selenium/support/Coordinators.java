package com.nordstrom.automation.selenium.support;

import java.util.Set;

import org.openqa.selenium.SearchContext;

import com.nordstrom.automation.selenium.core.WebDriverUtils;

public class Coordinators {
	
	private Coordinators() {
		throw new AssertionError("Coordinators is a static utility class that cannot be instantiated");
	}
	
	public static Coordinator<String> newWindowIsOpened(final Set<String> initialHandles) {
		return new Coordinator<String>() {

			@Override
			public String apply(SearchContext context) {
				Set<String> currentHandles = WebDriverUtils.getDriver(context).getWindowHandles();
				currentHandles.removeAll(initialHandles);
				if (currentHandles.isEmpty()) {
					return null;
				} else {
					return currentHandles.iterator().next();
				}
			}
			
			@Override
			public String toString() {
				return "new window to be opened";
			}
		};

	}
	
	public static Coordinator<Boolean> windowIsClosed(final String windowHandle) {
		return new Coordinator<Boolean>() {

			@Override
			public Boolean apply(SearchContext context) {
				Set<String> currentHandles = WebDriverUtils.getDriver(context).getWindowHandles();
				return Boolean.valueOf( ! currentHandles.contains(windowHandle));
			}
			
			@Override
			public String toString() {
				return "window with handle '" + windowHandle + "' to be closed";
			}
		};
	}

}
