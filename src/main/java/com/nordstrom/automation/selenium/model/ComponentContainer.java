package com.nordstrom.automation.selenium.model;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.WrapsDriver;
import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.SeleniumConfig.SeleniumSettings;
import com.nordstrom.automation.selenium.core.WebDriverUtils;
import com.nordstrom.automation.selenium.interfaces.WrapsContext;
import com.nordstrom.automation.selenium.support.Coordinator;
import com.nordstrom.automation.selenium.support.SearchContextWait;

public abstract class ComponentContainer extends Enhanceable<ComponentContainer> implements SearchContext, WrapsDriver, WrapsContext {
	
	protected WebDriver driver;
	protected SearchContext context;
	protected ComponentContainer parent;
	protected Method vacater;
	protected SearchContextWait wait;
	private List<Class<?>> bypass;
	private List<String> methods;
	
	public static final By SELF = By.xpath(".");
	private static final Class<?>[] BYPASS = {Object.class, WrapsDriver.class, WrapsContext.class};
	private static final String[] METHODS = {"validateParent", "getDriver", "getContext", "getParent", "getParentPage", 
			"getWait", "switchTo", "switchToContext", "getVacater", "setVacater", "isVacated", "enhanceContainer",
			"bypassClassOf", "bypassMethod"};
	private static final Class<?>[] ARG_TYPES = {SearchContext.class, ComponentContainer.class};
	
	/**
	 * Constructor for component container
	 * 
	 * @param context container search context
	 * @param parent container parent (may be {@code null} for {@link Page} objects
	 */
	public ComponentContainer(SearchContext context, ComponentContainer parent) {
		if (context == null) throw new IllegalArgumentException("Context must be non-null");
		validateParent(parent);
		
		this.context = context;
		this.driver = WebDriverUtils.getDriver(context);
		this.parent = parent;
	}
	
	/**
	 * Validate the specified parent object
	 * 
	 * @param parent container parent
	 */
	protected void validateParent(ComponentContainer parent) {
		if (parent == null) throw new IllegalArgumentException("Parent must be non-null");
	}

	/**
	 * Get the driver associated with this container
	 * 
	 * @return container driver
	 */
	public WebDriver getDriver() {
		return driver;
	}
	
	/**
	 * Get the container search context
	 * 
	 * @return container search context
	 */
	public SearchContext getContext() {
		return context;
	}
	
	/**
	 * Get the parent of this container
	 * 
	 * @return parent container
	 */
	public ComponentContainer getParent() {
		return parent;
	}
	
	/**
	 * Get the parent page for this container
	 * 
	 * @return container parent page
	 */
	public Page getParentPage() {
		if (parent != null) return parent.getParentPage();
		return (Page) this;
	}
	
	/**
	 * Convenience method to get a search context wait object for this container
	 * 
	 * @return {@link SearchContextWait} object with timeout specified by {@link SeleniumSettings#WAIT_TIMEOUT}
	 */
	public SearchContextWait getWait() {
		if (wait == null) {
			SeleniumConfig config = SeleniumConfig.getConfig();
			long waitTimeout = config.getLong(SeleniumSettings.WAIT_TIMEOUT.key());
			wait = new SearchContextWait(this, waitTimeout);
		}
		return wait;
	}
	
	/**
	 * Switch focus to this container's search context.
	 * <p>
	 * <b>NOTE</b>: This method walks down the container lineage to the parent page object, then back up to this 
	 * container, focusing the driver on each container as it goes.
	 * 
	 * @return this container's context
	 */
	public SearchContext switchTo() {
		return getWait().until(contextIsSwitched(this));
	}
	
	/**
	 * Returns a 'wait' proxy that switches focus to the specified context
	 * 
	 * @param context search context on which to focus
	 * @return target search context
	 */
	static Coordinator<SearchContext> contextIsSwitched(final ComponentContainer context) {
		return new Coordinator<SearchContext>() {

			@Override
			public SearchContext apply(SearchContext ignore) {
				if (context.parent != null) context.parent.switchTo();
				
				try {
					return context.switchToContext();
				} catch (StaleElementReferenceException e) {
					return context.refreshContext();
				}
			}
			
			@Override
			public String toString() {
				return "context to be switched";
			}
		};
	}
	
	/**
	 * Switch focus to this container's search context.
	 * <p>
	 * <b>NOTE</b>: This method walks down the container lineage to the parent page object, then back up to this 
	 * container, focusing the driver on each container as it goes.
	 * 
	 * @return this container's context
	 */
	protected abstract SearchContext switchToContext();
	
	/**
	 * Get the method that caused this container to be vacated.
	 * 
	 * @return vacating method; 'null' if container is still valid
	 */
	Method getVacater() {
		if (vacater != null) {
			return vacater;
		} else if (parent != null) {
			return parent.getVacater();
		} else {
			return null;
		}
	}
	
	/**
	 * Set the method that caused this container to be vacated.
	 * 
	 * @param vacater vacating method
	 */
	void setVacater(Method vacater) {
		this.vacater = vacater;
		if (parent != null) parent.setVacater(vacater);
	}
	
	/**
	 * Determine if this container has been vacated.
	 * 
	 * @return 'true' if container has been vacated; otherwise 'false'
	 */
	boolean isVacated() {
		return (null != getVacater());
	}
	
	/**
	 * Find all elements within the current context using the given mechanism.
	 * 
	 * @param by the locating mechanism
	 * @return a list of all WebElements, or an empty list if nothing matches
	 */
	@Override
	public List<WebElement> findElements(By by) {
		return RobustWebElement.getElements(this, by);
	}
	
	/**
	 * Find the first WebElement using the given method.
	 * 
	 * @param by the locating mechanism
	 * @return the first matching element on the current context
	 */
	@Override
	public WebElement findElement(By by) {
		return RobustWebElement.getElement(this, by);
	}
	
	/**
	 * Get the driver object associated with this container.
	 * 
	 * @return container driver object
	 */
	@Override
	public WebDriver getWrappedDriver() {
		return driver;
	}
	
	/**
	 * Update the specified element with the indicated value
	 * 
	 * @param element target element (checkbox)
	 * @param value desired value
	 * @return 'true' if element value changed; otherwise 'false'
	 */
	public static boolean updateValue(WebElement element, boolean value) {
		String tagName = element.getTagName().toLowerCase();
		if ("input".equals(tagName)) {
			if ("checkbox".equals(element.getAttribute("type"))) {
				boolean exist = element.isSelected();
				if (exist == value) {
					return false;
				} else {
					element.click();
					return true;
				}
			}
		}
		return updateValue(element, Boolean.valueOf(value).toString());
	}
	
	/**
	 * Update the specified element with the indicated value
	 * 
	 * @param element target element (input, select)
	 * @param value desired value
	 * @return 'true' if element value changed; otherwise 'false'
	 */
	public static boolean updateValue(WebElement element, String value) {
		String tagName = element.getTagName().toLowerCase();
		if ("input".equals(tagName)) {
			if ("checkbox".equals(element.getAttribute("type"))) {
				return updateValue(element, Boolean.parseBoolean(value));
			} else {
				String exist = element.getAttribute("value");
				if (exist == null) {
					if (value == null) {
						return false;
					}
				}
				if (value == null) {
					element.clear();
					return true;
				} else if (exist.equals(value)) {
					return false;
				} else {
					element.sendKeys(value);
					return true;
				}
			}
		} else if ("select".equals(tagName)) {
			
		}
		return false;
	}
	
	/**
	 * Scroll the specified element into view
	 * 
	 * @param element target element
	 * @return the specified element
	 */
	public static WebElement scrollIntoView(WebElement element) {
		WebDriverUtils.getExecutor(element).executeScript("arguments[0].scrollIntoView(true);", element);
		return element;
	}

	@Override
	Class<?>[] getArgumentTypes() {
		return ARG_TYPES;
	}

	@Override
	Object[] getArguments() {
		return new Object[] {context, parent};
	}

	@Override
	List<Class<?>> getBypassClasses() {
		if (bypass == null) {
			bypass = super.getBypassClasses();
			Collections.addAll(bypass, bypassClasses());
		}
		return bypass;
	}
	
	/**
	 * Returns an array of classes whose methods should not be intercepted
	 * 
	 * @return array of bypass classes
	 */
	Class<?>[] bypassClasses() {
		return BYPASS;
	}
	
	@Override
	List<String> getBypassMethods() {
		if (methods == null) {
			methods = super.getBypassMethods();
			Collections.addAll(methods, bypassMethods());
		}
		return methods;
	}
	
	/**
	 * Returns an array of names for methods that should not be intercepted
	 * 
	 * @return array of bypass method names
	 */
	String[] bypassMethods() {
		return METHODS;
	}
	
}
