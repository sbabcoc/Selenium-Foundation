package com.nordstrom.automation.selenium.support;

import java.util.Set;

import org.openqa.selenium.SearchContext;

import com.nordstrom.automation.selenium.core.WebDriverUtils;

public class Coordinators {
	
	private Coordinators() {
		throw new AssertionError("Coordinators is a static utility class that cannot be instantiated");
	}
	
	/**
	 * Returns a 'wait' proxy that determines if a new window has opened
	 * 
	 * @param initialHandles initial set of window handles
	 * @return new window handle; 'null' if no new window found
	 */
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
	
	/**
	 * Returns a 'wait' proxy that determines if the specified window has closed
	 * 
	 * @param windowHandle handle of window that's expected to close
	 * @return 'true' if the specified window has closed; otherwise 'false'
	 */
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
