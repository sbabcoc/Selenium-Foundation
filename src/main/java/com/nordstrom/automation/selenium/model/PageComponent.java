package com.nordstrom.automation.selenium.model;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;

public class PageComponent extends ComponentContainer {
	
	private Class<?>[] argumentTypes;
	private Object[] arguments;
	
	private static final Class<?>[] ARG_TYPES_1 = {By.class, ComponentContainer.class};
	private static final Class<?>[] ARG_TYPES_2 = {By.class, Integer.TYPE, ComponentContainer.class};
	private static final Class<?>[] ARG_TYPES_3 = {RobustWebElement.class, ComponentContainer.class};
	
	/**
	 * Constructor for page component by element locator
	 * 
	 * @param locator component context element locator
	 * @param parent component parent container
	 */
	public PageComponent(By locator, ComponentContainer parent) {
		this(locator, -1, parent);
		
		argumentTypes = ARG_TYPES_1;
		arguments = new Object[] {locator, parent};
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
		
		argumentTypes = ARG_TYPES_2;
		arguments = new Object[] {locator, index, parent};
	}
	
	/**
	 * Get context element for this page component
	 * 
	 * @param locator element locator
	 * @param index element index (-1 = non-indexed)
	 * @param parent element search context
	 * @return page component context element reference
	 */
	private static RobustWebElement getContext(By locator, int index, ComponentContainer parent) {
		return (RobustWebElement) RobustWebElement.getElement(parent, locator, index);
	}
	
	/**
	 * Constructor for page component by context element
	 * 
	 * @param element component context element
	 * @param parent component parent
	 */
	public PageComponent(RobustWebElement element, ComponentContainer parent) {
		super(element, parent);
		
		argumentTypes = ARG_TYPES_3;
		arguments = new Object[] {element, parent};
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
		((RobustWebElement) context).refreshContext();
		return this;
	}

	@Override
	public Class<?>[] getArgumentTypes() {
		return argumentTypes;
	}

	@Override
	public Object[] getArguments() {
		return arguments;
	}
	
}
