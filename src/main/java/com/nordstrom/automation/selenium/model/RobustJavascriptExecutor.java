package com.nordstrom.automation.selenium.model;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.internal.WrapsDriver;

public class RobustJavascriptExecutor implements JavascriptExecutor, WrapsDriver {
	
	private JavascriptExecutor executor;
	
	public RobustJavascriptExecutor(WebDriver driver) {
		if (driver instanceof JavascriptExecutor) {
			executor = (JavascriptExecutor) driver;
		} else {
			throw new UnsupportedOperationException("The specified driver is unable to execute JavaScript");
		}
	}

	@Override
	public Object executeAsyncScript(String script, Object... args) {
		Object result = null;
		try {
			result = executor.executeAsyncScript(script, args);
		} catch (StaleElementReferenceException e) {
			if (refreshReferences(e, args)) {
				executeAsyncScript(script, args);
			} else {
				throw e;
			}
		}
		return result;
	}

	@Override
	public Object executeScript(String script, Object... args) {
		Object result = null;
		try {
			result = executor.executeScript(script, args);
		} catch (StaleElementReferenceException e) {
			if (refreshReferences(e, args)) {
				executeScript(script, args);
			} else {
				throw e;
			}
		}
		return result;
	}

	@Override
	public WebDriver getWrappedDriver() {
		return (WebDriver) executor;
	}
	
	private static boolean refreshReferences(StaleElementReferenceException e, Object... args) {
		boolean didRefresh = false;
		
		for (int i = 0; i < args.length; i++) {
			if (args[i] instanceof RobustWebElement) {
				((RobustWebElement) args[i]).refreshReference(e);
				didRefresh = true;
			}
		}
		
		return didRefresh;
	}
	
}
