package com.nordstrom.automation.selenium.model;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

public class PageComponent extends ComponentContainer {

	private By locator;
	private int index;
	
	public PageComponent(By locator, ComponentContainer parent) {
		this(locator, -1, parent);
	}
	
	public PageComponent(By locator, int index, ComponentContainer parent) {
		this(getContext(locator, index, parent), parent);
		this.locator = locator;
		this.index = index;
	}
	
	private static WebElement getContext(By locator, int index, ComponentContainer parent) {
		if (index > 0) {
			return parent.findElements(locator).get(index);
		} else {
			return parent.findElement(locator);
		}
	}
	
	/**
	 * Constructor for page component
	 * 
	 * @param context component search context
	 * @param parent component parent
	 */
	private PageComponent(SearchContext context, ComponentContainer parent) {
		super(context, parent);
	}

	@Override
	protected SearchContext switchToContext() {
		return this;
	}

	@Override
	public SearchContext getWrappedContext() {
		return getWrappedElement();
	}

	@Override
	public SearchContext refreshContext() {
		parent.refreshContext();
		getContext(locator, index, parent);
		return this;
	}

}
