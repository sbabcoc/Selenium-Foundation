package com.nordstrom.automation.selenium.support;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.nordstrom.automation.selenium.core.WebDriverUtils;

public class SearchContextWait extends WebDriverWait {
	
	private final SearchContext context;
	
	public SearchContextWait(SearchContext context, long timeOutInSeconds) {
		super(WebDriverUtils.getDriver(context), timeOutInSeconds);
		this.context = context;
	}

	public SearchContextWait(SearchContext context, long timeOutInSeconds, long sleepInMillis) {
		super(WebDriverUtils.getDriver(context), timeOutInSeconds, sleepInMillis);
		this.context = context;
	}
}
