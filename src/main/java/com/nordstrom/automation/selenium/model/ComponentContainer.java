package com.nordstrom.automation.selenium.model;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.WrapsDriver;
import org.openqa.selenium.internal.WrapsElement;

import com.nordstrom.automation.selenium.core.WebDriverUtils;

public abstract class ComponentContainer implements SearchContext, WrapsDriver, WrapsElement {
	
	public static final By SELF = By.xpath(".");
	
	public ComponentContainer(SearchContext context, ComponentContainer parent) {
		if (context != null) {
			this.context = context;
			this.driver = WebDriverUtils.getDriver(context);
		} else {
			throw new IllegalArgumentException("Context must be non-null");
		}
		
		if (parent != null) {
			this.parent = parent;
		} else if ( ! (this instanceof Page)) {
			throw new IllegalArgumentException("Only page objects can omit parent");
		}
	}

	protected WebDriver driver;
	protected SearchContext context;
	protected ComponentContainer parent;
	
	/**
	 * 
	 * 
	 * @return the driver
	 */
	public WebDriver getDriver() {
		return driver;
	}
	
	/**
	 * 
	 * @return
	 */
	public ComponentContainer getParent() {
		return parent;
	}
	
	/**
	 * 
	 * @return
	 */
	public List<ComponentContainer> getLineage() {
		List<ComponentContainer> lineage = new ArrayList<>();
		for (ComponentContainer generation = this; generation != null; generation = generation.getParent()) {
			lineage.add(0, generation);
		}
		return lineage;
	}
	
	/**
	 * 
	 * @return
	 */
	public WebDriver switchTo() {
		if (parent != null) parent.switchTo();
		return switchToContext();
	}
	
	/**
	 * 
	 * @return
	 */
	protected abstract WebDriver switchToContext();
	
	/**
	 * 
	 * @param selector
	 * @return
	 */
	@Override
	public List<WebElement> findElements(By selector) {
		return context.findElements(selector);
	}
	
	/**
	 * 
	 * @param selector
	 * @return
	 */
	@Override
	public WebElement findElement(By selector) {
		return context.findElement(selector);
	}
	
	@Override
	public WebDriver getWrappedDriver() {
		return driver;
	}

	@Override
	public WebElement getWrappedElement() {
		return context.findElement(SELF);
	}

}
