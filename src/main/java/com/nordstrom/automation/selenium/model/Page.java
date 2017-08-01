package com.nordstrom.automation.selenium.model;

import java.net.URI;
import org.apache.commons.lang3.ArrayUtils;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;

import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.annotations.PageUrl;
import com.nordstrom.automation.selenium.exceptions.InitialPageUnspecifiedException;

public class Page extends ComponentContainer {
	
	private String windowHandle;
	private Page spawningPage;
	private WindowState windowState;
	protected Class<?>[] argumentTypes;
	protected Object[] arguments;
	
	private static final Class<?>[] ARG_TYPES_1 = {WebDriver.class};
	private static final Class<?>[] ARG_TYPES_2 = {WebDriver.class, ComponentContainer.class};
	
	private static final String[] METHODS = {"setWindowHandle", "getWindowHandle", "setSpawningPage", "getSpawningPage",
			"setWindowState", "getWindowState", "openInitialPage", "getInitialUrl", "getPageUrl"};
	
	public enum WindowState {
		WILL_OPEN, 
		WILL_CLOSE
	}
	
	/**
	 * Constructor for main document context
	 * 
	 * @param driver driver object
	 */
	public Page(WebDriver driver) {
		super(driver, null);
		
		argumentTypes = ARG_TYPES_1;
		arguments = new Object[] {driver};
	}
	
	/**
	 * Constructor for frame-based document context
	 * <p>
	 * <b>NOTE</b>: This package-private constructor is reserved for the {@link Frame} class
	 * 
	 * @param driver driver object
	 * @param parent page parent
	 */
	Page(WebDriver driver, ComponentContainer parent) {
		super(driver, parent);
		
		argumentTypes = ARG_TYPES_2;
		arguments = new Object[] {driver, parent};
	}
	
	@Override
	protected void validateParent(ComponentContainer parent) {
		// Page objects can omit parent 
	}
	
	@Override
	protected SearchContext switchToContext() {
		driver.switchTo().window(windowHandle);
		return this;
	}
	
	/**
	 * Set the window handle associated with this page object.
	 * 
	 * @param windowHandle page object window handle
	 */
	public void setWindowHandle(String windowHandle) {
		this.windowHandle = windowHandle;
	}
	
	/**
	 * Get the window handle associated with this page object.
	 * 
	 * @return page object window handle
	 */
	public String getWindowHandle() {
		return windowHandle;
	}
	
	/**
	 * Set the page from which this page was spawned.
	 * 
	 * @param pageObj page from which this page was spawned
	 */
	public void setSpawningPage(Page pageObj) {
		this.spawningPage = pageObj;
	}
	
	/**
	 * Get the page from which this page was spawned.
	 * 
	 * @return page from which this page was spawned
	 */
	public Page getSpawningPage() {
		return spawningPage;
	}
	
	/**
	 * Set the window state of this page object.
	 * 
	 * @param windowState page object {@link WindowState}
	 * @return this {@link Page} object
	 */
	public Page setWindowState(WindowState windowState) {
		this.windowState = windowState;
		return this;
	}
	
	/**
	 * Get the window state of this page object.
	 * 
	 * @return page object {@link WindowState}
	 */
	public WindowState getWindowState() {
		return windowState;
	}
	
	/**
	 * Get the title for this page object.
	 * 
	 * @return page object title
	 */
	public String getTitle() {
		return driver.getTitle();
	}
	
	/**
	 * Open the page defined by the specified {@link InitialPage} annotation.
	 * 
	 * @param <T> page class
	 * @param initialPage initial page annotation
	 * @param driver driver object
	 * @param targetUri target URI
	 * @return page object defined by the specified annotation
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Page> T openInitialPage(InitialPage initialPage, WebDriver driver, URI targetUri) {
		String url = getInitialUrl(initialPage, targetUri);
		if (url == null) throw new InitialPageUnspecifiedException("No initial page has been specified");
		
		driver.get(url);
		return newContainer((Class<T>) initialPage.value(), ARG_TYPES_1, new Object[] {driver});
	}
	
	/**
	 * Get the URL defined by the specified {@link InitialPage} annotation.
	 * 
	 * @param initialPage initial page annotation
	 * @param targetUri target URI
	 * @return defined initial URL as a string (may be 'null')
	 */
	private static String getInitialUrl(InitialPage initialPage, URI targetUri) {
		String url = getPageUrl(initialPage.pageUrl(), targetUri);
		if (url == null) {
			Class<? extends Page> pageClass = initialPage.value();
			url = getPageUrl(pageClass.getAnnotation(PageUrl.class), targetUri);
		}
		return url;
	}
	
	/**
	 * Get a string representing the current URL that the browser is looking at.
	 * 
	 * @return The URL of the page currently loaded in the browser
	 */
	public String getCurrentUrl() {
		return driver.getCurrentUrl();
	}
	
	@Override
	public SearchContext getWrappedContext() {
		return getWrappedDriver();
	}

	@Override
	public SearchContext refreshContext(Long expiration) {
		return this;
	}

	@Override
	public Long acquiredAt() {
		return System.currentTimeMillis();
	}

	@Override
	public Class<?>[] getArgumentTypes() {
		return argumentTypes;
	}

	@Override
	public Object[] getArguments() {
		return arguments;
	}
	
	@Override
	String[] bypassMethods() {
		return ArrayUtils.addAll(super.bypassMethods(), METHODS);
	}
	
	/**
	 * Create an enhanced instance of the specified container.
	 * 
	 * @param <C> container type
	 * @param container container object to be enhanced
	 * @return enhanced container object
	 */
	@Override
	public <C extends ComponentContainer> C enhanceContainer(C container) {
		if (container instanceof Enhanced) return container;
		C enhanced = super.enhanceContainer(container);
		((Page) enhanced).setWindowHandle(((Page) container).getWindowHandle());
		((Page) enhanced).setSpawningPage(((Page) container).getSpawningPage());
		return enhanced;
	}
	
}
