package com.nordstrom.automation.selenium.interfaces;

import org.openqa.selenium.SearchContext;

public interface WrapsContext {
	
	public SearchContext getWrappedContext();
	public SearchContext refreshContext();

}
