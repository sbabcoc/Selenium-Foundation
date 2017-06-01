package com.nordstrom.automation.selenium.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nordstrom.automation.selenium.SeleniumConfig.SeleniumSettings;
import com.nordstrom.automation.selenium.SeleniumConfig.WaitType;
import com.nordstrom.automation.selenium.core.WebDriverUtils;
import com.nordstrom.automation.selenium.interfaces.WrapsContext;
import com.nordstrom.automation.selenium.support.Coordinator;
import com.nordstrom.automation.selenium.support.SearchContextWait;
import com.nordstrom.common.base.UncheckedThrow;

public abstract class ComponentContainer extends Enhanceable<ComponentContainer> implements SearchContext, WrapsContext {
	
	protected WebDriver driver;
	protected SearchContext context;
	protected ComponentContainer parent;
	protected Method vacater;
	protected SearchContextWait wait;
	private List<Class<?>> bypass;
	private List<String> methods;
	
	public static final By SELF = By.xpath(".");
	private static final Class<?>[] BYPASS = {Object.class, WrapsContext.class};
	private static final String[] METHODS = {"validateParent", "getDriver", "getContext", "getParent", "getParentPage", 
			"getWait", "switchTo", "switchToContext", "getVacater", "setVacater", "isVacated", "enhanceContainer",
			"bypassClassOf", "bypassMethod", "getLogger", "hashCode", "equals", "getArgumentTypes", "getArguments"};
	private static final Class<?>[] ARG_TYPES = {SearchContext.class, ComponentContainer.class};
	private final Logger logger;
	
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
		
		Class<?> clazz = getClass();		
		logger = LoggerFactory.getLogger((this instanceof Enhanced) ? clazz.getSuperclass() : clazz);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(getArgumentTypes());
		result = prime * result + Arrays.hashCode(getArguments());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PageComponent other = (PageComponent) obj;
		if (!Arrays.equals(getArgumentTypes(), other.getArgumentTypes()))
			return false;
		if (!Arrays.equals(getArguments(), other.getArguments()))
			return false;
		return true;
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
			wait = WaitType.WAIT.getWait(this);
		}
		return wait;
	}
	
	/**
	 * Convenience method to get a search context wait object of the specified type for this container
	 * 
	 * @param waitType wait type being requested
	 * @return {@link SearchContextWait} object of the specified type for this container
	 */
	public SearchContextWait getWait(WaitType waitType) {
		return waitType.getWait(this);
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
	 * Get a wrapped reference to the first element matching the specified locator.
	 * <p>
	 * <b>NOTE</b>: Use {@link RobustWebElement#hasReference()} to determine if a valid reference was acquired.
	 * 
	 * @param by the locating mechanism
	 * @return robust web element
	 */
	public RobustWebElement findOptional(By by) {
		return RobustWebElement.getElement(this, by, RobustWebElement.OPTIONAL);
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
				if (!valueEquals(element, value)) {
					if (value == null) {
						element.clear();
					} else {
						element.sendKeys(value);
					}
					return true;
				}
			}
		} else if ("select".equals(tagName)) {
			if (!valueEquals(element, value)) {
				new Select(element).selectByValue(value);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Determine if the specified element has the desired value.
	 * 
	 * @param element target element (input, select)
	 * @param value desired value
	 * @return 'true' if element has the desired value; otherwise 'false'
	 */
	private static boolean valueEquals(WebElement element, String value) {
		String exist = element.getAttribute("value");
		if (exist == null) {
			if (value == null) {
				return true;
			}
		}
		return (exist.equals(value));
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
	protected List<Class<?>> getBypassClasses() {
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
	protected List<String> getBypassMethods() {
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
	
	/**
	 * Get the logger for this container
	 * 
	 * @return logger object
	 */
	protected Logger getLogger() {
		return logger;
	}
	
	/**
	 * Get {@link Method} object for the static {@code getKey(SearchContext)} method declared by the specified container type.
	 * 
	 * @param containerType target container type
	 * @return method object for getKey(SearchContext) 
	 */
	static <T extends ComponentContainer> Method getKeyMethod(Class<T> containerType) {
		try {
			Method method = containerType.getMethod("getKey", SearchContext.class);
			if (Modifier.isStatic(method.getModifiers())) return method;
		} catch (NoSuchMethodException e) { }
    	throw new UnsupportedOperationException("Container class must declare static 'getKey(SearchContext)' method");
	}
	
	/**
	 * Instantiate a new container of the specified type with the supplied arguments.
	 * 
	 * @param containerType type of container to instantiate
	 * @param argumentTypes array of constructor argument types
	 * @param arguments array of constructor argument values
	 * @return new container of the specified type
	 */
	static <T extends ComponentContainer> T newContainer(Class<T> containerType, Class<?>[] argumentTypes, Object[] arguments) {
		try {
			Constructor<T> ctor = containerType.getConstructor(argumentTypes);
			T container = ctor.newInstance(arguments);
			return container.enhanceContainer(container);
		} catch (NoSuchMethodException | SecurityException | InstantiationException |
				IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw UncheckedThrow.throwUnchecked(e);
		}
	}
	
}
