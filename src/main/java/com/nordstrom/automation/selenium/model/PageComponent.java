package com.nordstrom.automation.selenium.model;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.WrapsElement;

import com.nordstrom.automation.selenium.support.Coordinator;

public class PageComponent extends ComponentContainer implements WrapsElement {
	
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
	
	@Override
	public WebElement getWrappedElement() {
		return (WebElement) context;
	}
	
	/**
	 * Determine if this component is visible
	 * <p>
	 * <b>NOTE</b>: The default implementation of this method uses the {@link WebElement#isDisplayed()} method of the 
	 * component context element to determine visibility. If the component context element is always hidden, override 
	 * this method with your scenario-specific implementation. 
	 * 
	 * @return 'true' if component is visible; otherwise 'false'
	 */
	public boolean isDisplayed() {
		RobustWebElement element = (RobustWebElement) context;
		return (element.hasReference()) ? element.isDisplayed() : false;
	}
	
	/**
	 * Returns a 'wait' proxy that determines if this page component is visible
	 * 
	 * @return page component if visible; otherwise 'null'
	 */
	public static Coordinator<PageComponent> componentIsVisible() {
		return new Coordinator<PageComponent>() {

			@Override
			public PageComponent apply(SearchContext context) {
				PageComponent component = verifyContext(context);
				return (component.isDisplayed()) ? component : null;
			}
			
			@Override
			public String toString() {
				return "page component to be visible";
			}
		};
	}
	
	/**
	 * Returns a 'wait' proxy that determines if this page component is hidden
	 * 
	 * @return page component if hidden; otherwise 'null'
	 */
	public static Coordinator<PageComponent> componentIsHidden() {
		return new Coordinator<PageComponent>() {

			@Override
			public PageComponent apply(SearchContext context) {
				PageComponent component = verifyContext(context);
				return (component.isDisplayed()) ? null : component;
			}
			
			@Override
			public String toString() {
				return "page component to be hidden";
			}
		};
	}
	
	/**
	 * Determine if the specified search context is a page component
	 * 
	 * @param context search context in question
	 * @return search context as page component (throws an exception otherwise)
	 * @throws UnsupportedOperationException if specified search context isn't a page component
	 */
	private static PageComponent verifyContext(SearchContext context) {
		if (context instanceof PageComponent) return (PageComponent) context;
		throw new UnsupportedOperationException("Wait object search context is not a page component");
	}

}
