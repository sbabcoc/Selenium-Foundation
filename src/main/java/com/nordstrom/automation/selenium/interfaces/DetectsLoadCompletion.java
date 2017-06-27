package com.nordstrom.automation.selenium.interfaces;

import org.openqa.selenium.SearchContext;

import com.nordstrom.automation.selenium.support.Coordinator;

/**
 * Page classes that model pages with complex loading behavior implement this interface to provide scenario-specific
 * detection of page load completion. This is typically required for single-page applications or more conventional
 * multi-page applications that use dynamic load techniques (e.g. - AJAX).
 */
public interface DetectsLoadCompletion {
	
	/**
	 * Determine if the page has finished loading.
	 * 
	 * @return 'true' if the page has finished loading; otherwise 'false'
	 */
	public boolean isLoadComplete();
	
	/**
	 * Returns a 'wait' proxy that determines if the page has finished loading.
	 * 
	 * @return 'true' if the page has finished loading; otherwise 'false'
	 */
	public static Coordinator<Boolean> pageLoadIsComplete() {
		return new Coordinator<Boolean>() {

			@Override
			public Boolean apply(SearchContext context) {
				return Boolean.valueOf(((DetectsLoadCompletion) context).isLoadComplete());
			}
			
			@Override
			public String toString() {
				return "page to finish loading";
			}
		};
	}

}
