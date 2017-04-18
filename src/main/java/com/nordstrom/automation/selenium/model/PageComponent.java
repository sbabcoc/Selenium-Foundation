package com.nordstrom.automation.selenium.model;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

public class PageComponent extends ComponentContainer {
	
	/**
	 * Constructor for page component by element locator
	 * 
	 * @param locator component context element locator
	 * @param parent component parent container
	 */
	public PageComponent(By locator, ComponentContainer parent) {
		this(locator, -1, parent);
	}
	
	/**
	 * Constructor for page component by element locator and index
	 * 
	 * @param locator component context element locator
	 * @param index component context index (-1 = non-indexed)
	 * @param parent component parent container
	 */
	public PageComponent(By locator, int index, ComponentContainer parent) {
		this(getContext(locator, index, parent), parent);
	}
	
	/**
	 * Get context element for this page component
	 * 
	 * @param locator element locator
	 * @param index element index (-1 = non-indexed)
	 * @param parent element search context
	 * @return page component context element reference
	 */
	private static WebElement getContext(By locator, int index, ComponentContainer parent) {
		return RobustWebElement.getElement(parent, locator, index);
	}
	
	/**
	 * Constructor for page component
	 * 
	 * @param context component search context (must be {@link RobustWebElement}
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
	
	@Override
	public SearchContext getWrappedContext() {
		return context;
	}

	@Override
	public SearchContext refreshContext() {
		parent.refreshContext();
		return this;
	}

}
