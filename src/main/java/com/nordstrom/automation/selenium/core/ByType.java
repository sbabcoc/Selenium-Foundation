package com.nordstrom.automation.selenium.core;

import org.openqa.selenium.By;

public class ByType {
	
	private ByType() {
		throw new AssertionError("ByType is a static utility class that cannot be instantiated");
	}

	public interface ByEnum {
		By locator();
	}
	
	public static String cssLocatorFor(By locator) {
		
		String val = valueOf(locator);
		
		if (locator instanceof By.ByClassName) {
			return "." + val;
		} else if (locator instanceof By.ByCssSelector) {
			return val;
		} else if (locator instanceof By.ById) {
			return "#" + val;
		} else if (locator instanceof By.ByLinkText) {
			// unsupported
		} else if (locator instanceof By.ByName) {
			return "[name=" + val + "]";
		} else if (locator instanceof By.ByPartialLinkText) {
			// unsupported
		} else if (locator instanceof By.ByTagName) {
			return val;
		} else if (locator instanceof By.ByXPath) {
			// unsupported
		}
		
		return null;
	}
	
	public static String xpathLocatorFor(By locator) {
		
		String val = valueOf(locator);
		
		if (locator instanceof By.ByClassName) {
			return "./*[contains(concat(' ',@class,' '),' " + val + " ')]";
		} else if (locator instanceof By.ByCssSelector) {
			// unsupported
		} else if (locator instanceof By.ById) {
			return "./*[@id='" + val + "']";
		} else if (locator instanceof By.ByLinkText) {
			return "./a[.='" + val + "']";
		} else if (locator instanceof By.ByName) {
			return "./*[@name='" + val + "']";
		} else if (locator instanceof By.ByPartialLinkText) {
			return "./a[text()[contains(.,'" + val + "')]]";
		} else if (locator instanceof By.ByTagName) {
			return "./" + val;
		} else if (locator instanceof By.ByXPath) {
			return val;
		}
		
		return val;
	}
	
	private static String valueOf(By locator) {
		String str = locator.toString();
		int i = str.indexOf(':');
		return str.substring(i + 1).trim();
	}
}
