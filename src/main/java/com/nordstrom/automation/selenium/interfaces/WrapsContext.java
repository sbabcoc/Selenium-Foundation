package com.nordstrom.automation.selenium.interfaces;

import org.openqa.selenium.SearchContext;

public interface WrapsContext {
	
	/**
	 * Get the underlying search context for this object
	 * 
	 * @return object search context
	 */
	public SearchContext getWrappedContext();
	
	/**
	 * Refresh the underlying search context for this object
	 * 
	 * @return object search context
	 */
	public SearchContext refreshContext();

}
