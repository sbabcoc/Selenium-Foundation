package com.nordstrom.automation.selenium.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.WrapsDriver;
import org.openqa.selenium.internal.WrapsElement;

import com.google.common.base.Throwables;
import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.SeleniumConfig.SeleniumSettings;
import com.nordstrom.automation.selenium.core.WebDriverUtils;
import com.nordstrom.automation.selenium.interfaces.WrapsContext;
import com.nordstrom.automation.selenium.support.Coordinator;
import com.nordstrom.automation.selenium.support.SearchContextWait;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

public abstract class ComponentContainer implements SearchContext, WrapsDriver, WrapsElement, WrapsContext, CallbackFilter {
	
	protected WebDriver driver;
	protected SearchContext context;
	protected ComponentContainer parent;
	protected Method vacater;
	protected SearchContextWait wait;
	
	public static final By SELF = By.xpath(".");
	protected static final List<Class<?>> BYPASS;
	protected static final List<String> METHODS;
	private static final Class<?>[] ARG_TYPES = {SearchContext.class, ComponentContainer.class};
	
	static {
		BYPASS = Arrays.asList(Object.class, WrapsDriver.class, WrapsElement.class, WrapsContext.class, CallbackFilter.class);
		METHODS = Arrays.asList("validateParent", "getDriver", "getContext", "getParent", "getParentPage", "getWait",
				"switchTo", "switchToContext", "getVacater", "setVacater", "isVacated", "newChild", "enhanceContainer",
				"bypassClassOf", "bypassMethod");
	}
	
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
	 * Switch driver to this container's search context.
	 * <p>
	 * <b>NOTE</b>: This method walks down the container lineage to the parent page object, then back up to this 
	 * container, focusing the driver on each container as it goes.
	 * 
	 * @return driver focused on this container's context
	 */
	public ComponentContainer switchTo() {
		return (ComponentContainer) getWait().until(contextIsSwitched(this));
	}
	
	static Coordinator<SearchContext> contextIsSwitched(final ComponentContainer context) {
		return new Coordinator<SearchContext>() {

			@Override
			public SearchContext apply(SearchContext ignore) {
				if (((ComponentContainer) context).parent != null) ((ComponentContainer) context).parent.switchTo();
				
				try {
					return ((ComponentContainer) context).switchToContext();
				} catch (StaleElementReferenceException e) {
					return ((ComponentContainer) context).refreshContext();
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
	 * <b>NOTE</b>: This protected method is used to focus the driver on this container's context. This is the worker 
	 * for the {@link #switchTo} method, and it must be called in proper sequence to work properly.
	 * 
	 * @return this component container
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
	 * Create a container object of the specified class and context as a child of the target object
	 * 
	 * @param <T> type of child object to instantiate
	 * @param childClass class of child object to create
	 * @param context container search context
	 * @return new object of the specified type, with the current container as parent
	 */
	public <T extends ComponentContainer> T newChild(Class<T> childClass, SearchContext context) {
		return newChild(childClass, context, this);
	}
	
	/**
	 * Create a container object of the specified class and context as a child of the specified parent
	 * 
	 * @param <T> type of child object to instantiate
	 * @param childClass class of child object to create
	 * @param context container search context
	 * @param parent parent of the new container object
	 * @return new object of the specified type, with the specified container as parent
	 */
	public static <T extends ComponentContainer> T newChild(Class<T> childClass, SearchContext context, ComponentContainer parent) {
		T child = null;
		try {
			Constructor<T> ctor = childClass.getConstructor(SearchContext.class, ComponentContainer.class);
			child = ctor.newInstance(context, parent);
		} catch (InvocationTargetException e) {
			Throwables.propagate(e.getCause());
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException e) {
			Throwables.propagate(e);
		} catch (NoSuchMethodException | InstantiationException e) {
			// never thrown because generic type is bounded
		}
		return child;
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
	 * Get the context element for this container.
	 * 
	 * @return container context element
	 */
	@Override
	public WebElement getWrappedElement() {
		return context.findElement(SELF);
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

	/**
	 * Create an enhanced instance of the specified container.
	 * 
	 * @param <T> container type
	 * @param container container object to be enhanced
	 * @return enhanced container object
	 */
	@SuppressWarnings("unchecked")
	public <T extends ComponentContainer> T enhanceContainer(T container) {
		Class<? extends ComponentContainer> type = container.getClass();
		if (Enhancer.isEnhanced(type)) return container;
		
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(type);
		enhancer.setCallbacks(new Callback[] {ContainerMethodInterceptor.INSTANCE, NoOp.INSTANCE});
		enhancer.setCallbackFilter(this);
		return (T) enhancer.create(ARG_TYPES, new Object[] {container.context, container.parent});
	}
	
	/**
	 * Map a method to a callback type.
	 * 
	 * @param method the intercepted method
	 * @return a callback type, as enumerated by the {@link Enhancer#setCallbacks} invocation in
	 *         {@link #enhanceContainer}
	 */
	@Override
	public int accept(Method method) {
		if (bypassClassOf(method)) {
			return 1;
		} else if (bypassMethod(method)) {
			return 1;
		} else {
			return 0;
		}
	}
	
	/**
	 * Determine if the specified method is declared in a class that should be entirely bypassed.
	 * 
	 * @param method method in question
	 * @return 'true' if specified method is declared in bypassed class; otherwise 'false'
	 */
	protected boolean bypassClassOf(Method method) {
		for (Class<?> clazz : BYPASS) {
			for (Method member : clazz.getMethods()) {
				if (member.getName().endsWith(method.getName())) {
					if (Arrays.equals(member.getGenericParameterTypes(), method.getParameterTypes())) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Determine if the specified method should not be intercepted.
	 * 
	 * @param method method in question
	 * @return 'true' if specified method should be bypassed; otherwise 'false'
	 */
	protected boolean bypassMethod(Method method) {
		return METHODS.contains(method.getName());
	}
	
}
