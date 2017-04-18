package com.nordstrom.automation.selenium.model;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

public class PageComponent extends ComponentContainer {

	public PageComponent(By locator, ComponentContainer parent) {
		this(locator, -1, parent);
	}
	
	public PageComponent(By locator, int index, ComponentContainer parent) {
		this(getContext(locator, index, parent), parent);
	}
	
	private static WebElement getContext(By locator, int index, ComponentContainer parent) {
		return RobustWebElement.getElement(parent, locator, index);
	}
	
	/**
	 * Constructor for page component
	 * 
	 * @param context component search context
	 * @param parent component parent
	 */
	public PageComponent(SearchContext context, ComponentContainer parent) {
		super(context, parent);
		if ( ! (context instanceof RobustWebElement)) throw new IllegalArgumentException("Context must be a RobustWebElement"); 
	}

	@Override
	protected SearchContext switchToContext() {
		return this;
	}

	public SearchContext getWrappedContext() {
		return context;
	}

	@Override
	public SearchContext refreshContext() {
		parent.refreshContext();
		return this;
	}

}
