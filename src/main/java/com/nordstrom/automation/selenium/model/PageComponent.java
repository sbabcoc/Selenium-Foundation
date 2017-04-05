package com.nordstrom.automation.selenium.model;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;

public class PageComponent extends ComponentContainer {

	/**
	 * Constructor for page component
	 * 
	 * @param context component search context
	 * @param parent component parent
	 */
	public PageComponent(SearchContext context, ComponentContainer parent) {
		super(context, parent);
	}

	@Override
	protected WebDriver switchToContext() {
		return driver;
	}

}
