package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.WebDriverException;

import com.nordstrom.automation.selenium.annotations.PageUrl;
import com.nordstrom.automation.selenium.model.Page;

public class LandingPageMismatchException extends WebDriverException {

	private static final long serialVersionUID = -2375529252543798864L;

	public LandingPageMismatchException(Class<? extends Page> pageClass, String url) {
		super(getMessage(pageClass, url));
	}
	
	public LandingPageMismatchException(String message, Class<? extends Page> pageClass, String actual, String expect) {
		super(getMessage(message, pageClass, actual, expect));
	}
	
	private static String getMessage(Class<? extends Page> pageClass, String url) {
		return "Landing page for '" + pageClass.getSimpleName() + "' doesn't match specified pattern.\nactual: "
				+ url + "\npattern: " + pageClass.getAnnotation(PageUrl.class).pattern();
	}
	
	private static String getMessage(String message, Class<? extends Page> pageClass, String actual, String expect) {
		return "Landing page for '" + pageClass.getSimpleName() + "' doesn't match specified properties: " + message
				+ ".\nactual: " + actual + "\nexpected: " + expect;
	}
}
