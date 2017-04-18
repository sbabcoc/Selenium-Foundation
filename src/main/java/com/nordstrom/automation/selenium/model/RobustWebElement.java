package com.nordstrom.automation.selenium.model;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.FindsByCssSelector;
import org.openqa.selenium.internal.FindsByXPath;
import org.openqa.selenium.internal.WrapsDriver;
import org.openqa.selenium.internal.WrapsElement;

import com.nordstrom.automation.selenium.core.ByType;
import com.nordstrom.automation.selenium.core.JsUtility;
import com.nordstrom.automation.selenium.core.WebDriverUtils;
import com.nordstrom.automation.selenium.interfaces.WrapsContext;
import com.nordstrom.automation.selenium.support.Coordinator;
import com.nordstrom.automation.selenium.utility.UncheckedThrow;

public class RobustWebElement implements WebElement, WrapsElement, WrapsDriver, WrapsContext {
	
	private static String LOCATE_BY_CSS = JsUtility.getScriptResource("locateByCss.js");
	private static String LOCATE_BY_XPATH = JsUtility.getScriptResource("locateByXpath.js");
	
	private enum Strategy { LOCATOR, JS_XPATH, JS_CSS }
	
	private WebDriver driver;
	private WebElement wrapped;
	private WrapsContext context;
	private By locator;
	private int index;
	
	private String selector;
	private Strategy strategy = Strategy.LOCATOR;
	
	/**
	 * Basic robust web element constructor
	 * 
	 * @param context element search context
	 * @param locator element locator
	 */
	RobustWebElement(WrapsContext context, By locator) {
		this(null, context, locator, -1);
	}
	
	/**
	 * Constructor for wrapping an existing element reference 
	 * 
	 * @param element element reference to be wrapped
	 * @param context element search context
	 * @param locator element locator
	 */
	RobustWebElement(WebElement element, WrapsContext context, By locator) {
		this(element, context, locator, -1);
	}
	
	/**
	 * Main robust web element constructor
	 * 
	 * @param element element reference to be wrapped (may be 'null')
	 * @param context element search context
	 * @param locator element locator
	 * @param index element index
	 */
	RobustWebElement(WebElement element, WrapsContext context, By locator, int index) {
		
		// if specified element is already robust
		if (element instanceof RobustWebElement) {
			RobustWebElement robust = (RobustWebElement) element;
			element = robust.wrapped;
			context = robust.context;
			locator = robust.locator;
			index = robust.index;
		}
		
		this.wrapped = element;
		this.context = context;
		this.locator = locator;
		this.index = (index < 0) ? -1 : index;
		
		if (context == null) throw new IllegalArgumentException("Context cannot be null");
		if (locator == null) throw new IllegalArgumentException("Locator cannot be null");
		
		driver = WebDriverUtils.getDriver(context.getWrappedContext());
		boolean findsByCss = (driver instanceof FindsByCssSelector);
		boolean findsByXPath = (driver instanceof FindsByXPath);
		
		if (index > 0) {
			if (findsByXPath && ( ! (locator instanceof By.ByCssSelector))) {
				selector = ByType.xpathLocatorFor(locator) + "[" + (index + 1) + "]";
				strategy = Strategy.JS_XPATH;
				
				this.locator = By.xpath(this.selector);
				index = -1;
			} else if (findsByCss) {
				selector = ByType.cssLocatorFor(locator);
				if (selector != null) {
					strategy = Strategy.JS_CSS;
				}
			}
		}
		
		if (element == null) {
			acquireReference(this);
		}
	}
	
	@Override
	public <X> X getScreenshotAs(final OutputType<X> arg0) throws WebDriverException {
		try {
			return wrapped.getScreenshotAs(arg0);
		} catch (StaleElementReferenceException e) {
			return refreshReference(e).getScreenshotAs(arg0);
		}
	}

	@Override
	public void clear() {
		try {
			wrapped.clear();
		} catch (StaleElementReferenceException e) {
			refreshReference(e).clear();
		}
	}

	@Override
	public void click() {
		try {
			wrapped.click();
		} catch (StaleElementReferenceException e) {
			refreshReference(e).click();
		}
	}

	@Override
	public WebElement findElement(final By by) {
		try {
			return wrapped.findElement(by);
		} catch (StaleElementReferenceException e) {
			return refreshReference(e).findElement(by);
		}
	}

	@Override
	public List<WebElement> findElements(final By by) {
		try {
			return wrapped.findElements(by);
		} catch (StaleElementReferenceException e) {
			return refreshReference(e).findElements(by);
		}
	}

	@Override
	public String getAttribute(String name) {
		try {
			return wrapped.getAttribute(name);
		} catch (StaleElementReferenceException e) {
			return refreshReference(e).getAttribute(name);
		}
	}

	@Override
	public String getCssValue(String propertyName) {
		try {
			return wrapped.getCssValue(propertyName);
		} catch (StaleElementReferenceException e) {
			return refreshReference(e).getCssValue(propertyName);
		}
	}

	@Override
	public Point getLocation() {
		try {
			return wrapped.getLocation();
		} catch (StaleElementReferenceException e) {
			return refreshReference(e).getLocation();
		}
	}

	@Override
	public Rectangle getRect() {
		try {
			return wrapped.getRect();
		} catch (StaleElementReferenceException e) {
			return refreshReference(e).getRect();
		}
	}

	@Override
	public Dimension getSize() {
		try {
			return wrapped.getSize();
		} catch (StaleElementReferenceException e) {
			return refreshReference(e).getSize();
		}
	}

	@Override
	public String getTagName() {
		try {
			return wrapped.getTagName();
		} catch (StaleElementReferenceException e) {
			return refreshReference(e).getTagName();
		}
	}

	@Override
	public String getText() {
		try {
			return wrapped.getText();
		} catch (StaleElementReferenceException e) {
			return refreshReference(e).getText();
		}
	}

	@Override
	public boolean isDisplayed() {
		try {
			return wrapped.isDisplayed();
		} catch (StaleElementReferenceException e) {
			return refreshReference(e).isDisplayed();
		}
	}

	@Override
	public boolean isEnabled() {
		try {
			return wrapped.isEnabled();
		} catch (StaleElementReferenceException e) {
			return refreshReference(e).isEnabled();
		}
	}

	@Override
	public boolean isSelected() {
		try {
			return wrapped.isSelected();
		} catch (StaleElementReferenceException e) {
			return refreshReference(e).isSelected();
		}
	}

	@Override
	public void sendKeys(CharSequence... keysToSend) {
		try {
			wrapped.sendKeys(keysToSend);
		} catch (StaleElementReferenceException e) {
			refreshReference(e).sendKeys(keysToSend);
		}
	}

	@Override
	public void submit() {
		try {
			wrapped.submit();
		} catch (StaleElementReferenceException e) {
			refreshReference(e).submit();
		}
	}

	@Override
	public WebElement getWrappedElement() {
		return wrapped;
	}
	
	/**
	 * Refresh the wrapped element reference
	 * 
	 * @param e {@link StaleElementReferenceException} that necessitates reference refresh
	 * @return this robust web element with refreshed reference
	 */
	private WebElement refreshReference(StaleElementReferenceException e) {
		try {
			wrapped = ((ComponentContainer) context).getWait().until(referenceIsRefreshed(this));
			return this;
		} catch (Throwable t) {
			throw UncheckedThrow.throwUnchecked((e != null) ? e : t);
		}
	}
	
	/**
	 * Returns a 'wait' proxy that refreshes the wrapped reference of the specified robust element
	 * 
	 * @param element robust web element object
	 * @return wrapped element reference (refreshed)
	 */
	private static Coordinator<WebElement> referenceIsRefreshed(final RobustWebElement element) {
		return new Coordinator<WebElement>() {

			@Override
			public WebElement apply(SearchContext context) {
				try {
					return acquireReference(element);
				} catch (StaleElementReferenceException e) {
					((ComponentContainer) context).refreshContext();
					return acquireReference(element);
				}
			}

			@Override
			public String toString() {
				return "element reference to be refreshed";
			}
		};
		
	}
	
	/**
	 * Acquire the element reference that's wrapped by the specified robust element
	 * 
	 * @param element robust web element object
	 * @return wrapped element reference
	 */
	private static WebElement acquireReference(RobustWebElement element) {
		SearchContext context = element.context.getWrappedContext();
		
		switch (element.strategy) {
		case JS_CSS:
			element.wrapped = JsUtility.runAndReturn(
					element.driver, LOCATE_BY_CSS, WebElement.class, context, element.selector, element.index);
			break;
			
		case JS_XPATH:
			element.wrapped = JsUtility.runAndReturn(
					element.driver, LOCATE_BY_XPATH, WebElement.class, context, element.selector);
			break;
			
		case LOCATOR:
			if (element.index > 0) {
				element.wrapped = context.findElements(element.locator).get(element.index);
			} else {
				element.wrapped = context.findElement(element.locator);
			}
			break;
		}
		return element;
	}

	@Override
	public SearchContext getWrappedContext() {
		return wrapped;
	}

	@Override
	public SearchContext refreshContext() {
		return refreshReference(null);
	}

	@Override
	public WebDriver getWrappedDriver() {
		return WebDriverUtils.getDriver(wrapped);
	}
	
	/**
	 * Get the list of elements that match the specified locator in the indicated context
	 * 
	 * @param context element search context
	 * @param locator element locator
	 * @return list of robust elements in context that match the locator
	 */
	public static List<WebElement> getElements(ComponentContainer context, By locator) {
		List<WebElement> elements;
		try {
			context.switchTo();
			elements = context.getWrappedContext().findElements(locator);
		} catch (StaleElementReferenceException e) {
			elements = context.refreshContext().findElements(locator);
		}
		for (int index = 0; index < elements.size(); index++) {
			elements.set(index, new RobustWebElement(elements.get(index), context, locator, index));
		}
		return elements;
	}
	
	/**
	 * Get the first element that matches the specified locator in the indicated context
	 * 
	 * @param context element search context
	 * @param locator element locator
	 * @return robust element in context that matches the locator
	 */
	public static WebElement getElement(ComponentContainer context, By locator) {
		return getElement(context, locator, -1);
	}
	
	/**
	 * Get the item at the specified index in the list of elements matching the specified 
	 * locator in the indicated context
	 * 
	 * @param context element search context
	 * @param locator element locator
	 * @param index element index
	 * @return indexed robust element in context that matches the locator
	 */
	public static WebElement getElement(ComponentContainer context, By locator, int index) {
		return new RobustWebElement(null, context, locator, index);
	}

}
