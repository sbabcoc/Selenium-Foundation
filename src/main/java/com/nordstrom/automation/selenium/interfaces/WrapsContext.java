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
	 * @param acquiredAt acquisition time of descendant context
	 * @return object search context
	 */
	public SearchContext refreshContext(Long acquiredAt);
	
	/**
	 * Determine when the underlying search context for this object was acquired
	 * 
	 * @return search context acquisition time (from {@link System#currentTimeMillis()})
	 */
	public Long acquiredAt();
	
}
