package com.nordstrom.automation.selenium.model;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.UriBuilder;

import org.openqa.selenium.WebDriver;

import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.annotations.PageUrl;

public class Page extends ComponentContainer {
	
	private String windowHandle;
	private WindowState windowState;
	
	public enum WindowState {
		WILL_OPEN, 
		WILL_CLOSE, 
		DID_OPEN, 
		DID_CLOSE
	}
	
	/**
	 * Constructor for main document context
	 * 
	 * @param driver driver object
	 */
	public Page(WebDriver driver) {
		super(driver, null);
		// FIXME - Must be set by interceptor. This won't work for actions that spawn new windows.
		windowHandle = driver.getWindowHandle();
	}
	
	/**
	 * Constructor for frame-based document context<br>
	 * <br>
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
	
	/**
	 * Get page title
	 * 
	 * @return page title
	 */
	public String getTitle() {
		return driver.getTitle();
	}
	
	@Override
	protected WebDriver switchToContext() {
		return driver.switchTo().window(windowHandle);
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
	
}
