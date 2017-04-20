package com.nordstrom.automation.selenium.model;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;

public class Frame extends Page {
	
	private FrameSelect frameSelect;
	private RobustWebElement element;
	private int index;
	private String nameOrId;
	private Class<?>[] argumentTypes;
	private Object[] arguments;
	
	private static final Class<?>[] ARG_TYPES_1 = {By.class, ComponentContainer.class};
	private static final Class<?>[] ARG_TYPES_2 = {By.class, Integer.TYPE, ComponentContainer.class};
	private static final Class<?>[] ARG_TYPES_3 = {Integer.TYPE, ComponentContainer.class};
	private static final Class<?>[] ARG_TYPES_4 = {String.class, ComponentContainer.class};
	
	private enum FrameSelect {
		ELEMENT,
		INDEX, 
		NAME_OR_ID 
	}
	
	/**
	 * Constructor for frame by locator
	 * 
	 * @param locator frame element locator
	 * @param parent frame parent
	 */
	public Frame(By locator, ComponentContainer parent) {
		this(locator, -1, parent);
		
		argumentTypes = ARG_TYPES_1;
		arguments = new Object[] {locator, parent};
	}
	
	/**
	 * Constructor for frame by locator and index
	 * 
	 * @param locator frame element locator
	 * @param index frame element index
	 * @param parent frame parent
	 */
	public Frame(By locator, int index, ComponentContainer parent) {
		super(parent.driver, parent);
		this.frameSelect = FrameSelect.ELEMENT;
		this.index = index;
		
		this.element = new RobustWebElement(null, parent, locator, index);
		
		argumentTypes = ARG_TYPES_2;
		arguments = new Object[] {locator, index, parent};
	}
	
	/**
	 * Constructor for frame by index
	 * 
	 * @param index (zero-based) frame index
	 * @param parent frame parent
	 */
	public Frame(int index, ComponentContainer parent) {
		super(parent.driver, parent);
		this.frameSelect = FrameSelect.INDEX;
		this.index = index;
		
		argumentTypes = ARG_TYPES_3;
		arguments = new Object[] {index, parent};
	}
	
	/**
	 * Constructor for frame by name or ID
	 * 
	 * @param nameOrId the name of the frame window, the id of the &lt;frame&gt; or
	 *            &lt;iframe&gt; element, or the (zero-based) frame index
	 * @param parent frame parent
	 */
	public Frame(String nameOrId, ComponentContainer parent) {
		super(parent.driver, parent);
		this.frameSelect = FrameSelect.NAME_OR_ID;
		this.nameOrId = nameOrId;
		
		argumentTypes = ARG_TYPES_4;
		arguments = new Object[] {nameOrId, parent};
	}

	@Override
	protected SearchContext switchToContext() {
		switch (frameSelect) {
		case ELEMENT:
			driver.switchTo().frame(element.getWrappedElement());
			break;
			
		case INDEX:
			driver.switchTo().frame(index);
			break;
			
		case NAME_OR_ID:
			driver.switchTo().frame(nameOrId);
			break;
		}
		return this;
	}
	
	@Override
	public SearchContext refreshContext() {
		if (frameSelect == FrameSelect.ELEMENT) {
			element.refreshContext();
		} else {
			parent.refreshContext();
		}
		return switchToContext();
	}

	@Override
	public Class<?>[] getArgumentTypes() {
		return argumentTypes;
	}

	@Override
	public Object[] getArguments() {
		return arguments;
	}
	
}
