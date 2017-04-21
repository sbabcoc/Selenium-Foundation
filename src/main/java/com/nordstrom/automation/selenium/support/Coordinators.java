package com.nordstrom.automation.selenium.support;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

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

	/**
	 * Returns a 'wait' proxy that determines if the first element matched by the specified locator is visible
	 * 
	 * @param locator web element locator
	 * @return web element reference; 'null' if the indicated element is absent or hidden
	 */
	public static Coordinator<WebElement> visibilityOfElementLocated(final By locator) {
		return new Coordinator<WebElement>() {

			@Override
			public WebElement apply(SearchContext context) {
				try {
					return elementIfVisible(context.findElement(locator));
				} catch (StaleElementReferenceException e) {
					return null;
				}
			}

			@Override
			public String toString() {
				return "visibility of element located by " + locator;
			}
		};

	}
	
	/**
	 * Returns a 'wait' proxy that determines if any element matched by the specified locator is visible
	 * 
	 * @param locator web element locator
	 * @return web element reference; 'null' if no matching elements are visible
	 */
	public static Coordinator<WebElement> visibilityOfAnyElementLocated(final By locator) {
		return new Coordinator<WebElement>() {

			@Override
			public WebElement apply(SearchContext context) {
				try {
					List<WebElement> visible = context.findElements(locator);
					return (filterHidden(visible)) ? null : visible.get(0);
				} catch (StaleElementReferenceException e) {
					return null;
				}
			}

			@Override
			public String toString() {
				return "visibility of element located by " + locator;
			}
		};

	}
	
	/**
	 * Return a visibility-filtered element reference
	 * 
	 * @param element element whose visibility is in question
	 * @return specified element reference; 'null' if element is hidden
	 */
	private static WebElement elementIfVisible(WebElement element) {
		return element.isDisplayed() ? element : null;
	}
	
	/**
	 * Remove hidden elements from specified list
	 * 
	 * @param elements list of elements
	 * @return 'true' if no visible elements were found; otherwise 'false'
	 */
	public static boolean filterHidden(List<WebElement> elements) {
		Iterator<WebElement> iter = elements.iterator();
		while (iter.hasNext()) {
			if ( ! iter.next().isDisplayed()) iter.remove();
		}
		return elements.isEmpty();
	}
}
