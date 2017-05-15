package com.nordstrom.automation.selenium.interfaces;

import org.openqa.selenium.SearchContext;

import com.nordstrom.automation.selenium.support.Coordinator;

public interface DetectsLoadCompletion {
	
	public boolean isLoadComplete();
	
	/**
	 * Returns a 'wait' proxy that determines if the page has finished loading.
	 * 
	 * @return 'true' if the page has finished loading; otherwise 'false'
	 */
	static Coordinator<Boolean> pageLoadIsComplete() {
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
