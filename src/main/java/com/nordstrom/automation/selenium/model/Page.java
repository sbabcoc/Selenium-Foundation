package com.nordstrom.automation.selenium.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang3.ArrayUtils;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;

import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.annotations.PageUrl;
import com.nordstrom.common.base.UncheckedThrow;

public class Page extends ComponentContainer {
	
	private String windowHandle;
	private WindowState windowState;
	private Class<?>[] argumentTypes;
	private Object[] arguments;
	
	private static final Class<?>[] ARG_TYPES_1 = {WebDriver.class};
	private static final Class<?>[] ARG_TYPES_2 = {WebDriver.class, ComponentContainer.class};
	
	private static final String[] METHODS = {"setWindowHandle", "getWindowHandle", "setWindowState", "getWindowState",
			"openInitialPage", "getInitialUrl", "getPageUrl"};
	
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
		windowHandle = driver.getWindowHandle();
		
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
	 * Open the page defined by the {@link PageUrl} annotation of the specified page class.
	 * 
	 * @param <T> page class
	 * @param pageClass type of page object to instantiate
	 * @return new instance of the specified page class
	 */
	public <T extends Page> T openAnnotatedPage(Class<T> pageClass) {
		return openAnnotatedPage(pageClass, driver, SeleniumConfig.getConfig().getTargetUri());
	}
	
	/**
	 * Open the page defined by the {@link PageUrl} annotation of the specified page class.
	 * 
	 * @param <T> page class
	 * @param pageClass type of page object to instantiate
	 * @param driver driver object
	 * @param targetUri target URI
	 * @return new instance of the specified page class
	 */
	public static <T extends Page> T openAnnotatedPage(Class<T> pageClass, WebDriver driver, URI targetUri) {
		String pageUrl = getPageUrl(pageClass.getAnnotation(PageUrl.class), targetUri);
		driver.get(pageUrl);
		return newPage(pageClass, driver);
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
		String initialUrl = getInitialUrl(initialPage, targetUri);
		driver.get(initialUrl);
		return (T) newPage(initialPage.value(), driver);
	}
	
	/**
	 * Construct a new instance of the specified page class.
	 * 
	 * @param <T> page class
	 * @param pageClass type of page object to instantiate
	 * @param driver driver object
	 * @return new instance of the specified page class
	 */
	public static <T extends Page> T newPage(Class<T> pageClass, WebDriver driver) {
		try {
			Constructor<T> ctor = pageClass.getConstructor(WebDriver.class);
			return ctor.newInstance(driver);
		} catch (InvocationTargetException e) {
			throw UncheckedThrow.throwUnchecked(e.getCause());
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException e) {
			throw UncheckedThrow.throwUnchecked(e);
		} catch (NoSuchMethodException | InstantiationException e) {
			throw UncheckedThrow.throwUnchecked(e);
		}
	}
	
	/**
	 * Get the URL defined by the specified {@link InitialPage} annotation.
	 * 
	 * @param initialPage initial page annotation
	 * @param targetUri target URI
	 * @return defined initial URL as a string (may be 'null')
	 */
	public static String getInitialUrl(InitialPage initialPage, URI targetUri) {
		String url = getPageUrl(initialPage.pageUrl(), targetUri);
		if (url == null) {
			Class<? extends Page> pageClass = initialPage.value();
			url = getPageUrl(pageClass.getAnnotation(PageUrl.class), targetUri);
		}
		return url;
	}
	
	/**
	 * Get the URL defined by the specified {@link PageUrl} annotation.
	 * 
	 * @param pageUrl page URL annotation
	 * @param targetUri target URI
	 * @return defined page URL as a string (may be 'null')
	 */
	public static String getPageUrl(PageUrl pageUrl, URI targetUri) {
		if (pageUrl == null) return null;
		
		String scheme = pageUrl.scheme();
		String userInfo = pageUrl.userInfo();
		String host = pageUrl.host();
		String port = pageUrl.port();
		String path = pageUrl.value();
		String[] params = pageUrl.params();
		
		int len = Stream.of(scheme, userInfo, host, port, path, String.join("", params))
				.filter(s -> s != null && !s.isEmpty()).collect(Collectors.joining("")).length();
		
		if (len == 0) return null;

		UriBuilder builder = UriBuilder.fromUri(targetUri);
		
		if (scheme.length() > 0) builder.scheme(scheme);
		if (userInfo.length() > 0) builder.userInfo(userInfo);
		if (host.length() > 0) builder.host(host);
		if (port.length() > 0) builder.port(Integer.parseInt(port));
		if (path.length() > 0) builder.path(path);
		for (String param : params) {
			String[] bits = param.split("=");
			if (bits.length == 2) {
				builder.queryParam(bits[0], bits[1]);
			} else {
				throw new IllegalArgumentException("Unsupported format for declared parameter: " + param);
			}
		}
		
		return builder.build().toString();
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

}
