package com.nordstrom.automation.selenium.model;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.UriBuilder;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;

import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.annotations.PageUrl;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

public class Page extends ComponentContainer {
	
	private String windowHandle;
	private WindowState windowState;
	protected static final List<String> METHODS;
	private static final Class<?>[] ARG_TYPES = {WebDriver.class};
	
	static {
		METHODS = Arrays.asList("setWindowHandle", "getWindowHandle", "setWindowState", "getWindowState",
				"openInitialPage", "getInitialUrl", "getPageUrl");
	}
	
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
	 */
	public void setWindowState(WindowState windowState) {
		this.windowState = windowState;
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
	 * Open the page defined by the specified {@link InitialPage} annotation
	 * 
	 * @param initialPage initial page annotation
	 * @param driver driver object
	 * @return page object defined by the specified annotation
	 */
	public static Page openInitialPage(InitialPage initialPage, WebDriver driver) {
		String initialUrl = getInitialUrl(initialPage);
		driver.get(initialUrl);
		return ComponentContainer.newChild(initialPage.value(), driver, null);
	}
	
	/**
	 * Get the URL defined by the specified {@link InitialPage} annotation
	 * 
	 * @param initialPage initial page annotation
	 * @return defined initial URL as a string (may be 'null')
	 */
	public static String getInitialUrl(InitialPage initialPage) {
		String url = getPageUrl(initialPage.pageUrl());
		if (url == null) {
			Class<? extends Page> pageClass = initialPage.value();
			url = getPageUrl(pageClass.getAnnotation(PageUrl.class));
		}
		return url;
	}
	
	/**
	 * Get the URL defined by the specified {@link PageUrl} annotation
	 * 
	 * @param pageUrl page URL annotation
	 * @return defined page URL as a string (may be 'null')
	 */
	public static String getPageUrl(PageUrl pageUrl) {
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

		SeleniumConfig config = SeleniumConfig.getConfig();
		UriBuilder builder = UriBuilder.fromUri(config.getTargetUri());
		
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
	@SuppressWarnings("unchecked")
	public <T extends ComponentContainer> T enhanceContainer(T container) {
		Class<? extends ComponentContainer> type = container.getClass();
		if (Enhancer.isEnhanced(type)) return container;
		
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(type);
		enhancer.setCallbacks(new Callback[] {ContainerMethodInterceptor.INSTANCE, NoOp.INSTANCE});
		enhancer.setCallbackFilter(this);
		return (T) enhancer.create(ARG_TYPES, new Object[] {container.driver});
	}
	
	@Override
	protected boolean bypassMethod(Method method) {
		return super.bypassMethod(method) || METHODS.contains(method.getName());
	}

	@Override
	public SearchContext getWrappedContext() {
		return getWrappedDriver();
	}

	@Override
	public SearchContext refreshContext() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
