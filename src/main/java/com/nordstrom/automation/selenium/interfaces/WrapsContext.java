package com.nordstrom.automation.selenium.interfaces;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.internal.WrapsDriver;

public interface WrapsContext extends WrapsDriver {
	
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
