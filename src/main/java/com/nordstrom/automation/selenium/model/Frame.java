package com.nordstrom.automation.selenium.model;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class Frame extends Page {
	
	private FrameSelect frameSelect;
	private WebElement element;
	private int index;
	private String nameOrId;
	
	private enum FrameSelect {
		INDEX, 
		NAME_OR_ID, 
		ELEMENT
	}
	
	/**
	 * Constructor for frame by element
	 * 
	 * @param element
	 * @param parent frame parent
	 */
	public Frame(WebElement element, ComponentContainer parent) {
		super(parent.driver.switchTo().frame(element), parent);
		this.frameSelect = FrameSelect.ELEMENT;
		this.element = element;
	}
	
	/**
	 * Constructor for frame by index
	 * 
	 * @param index (zero-based) index
	 * @param parent frame parent
	 */
	public Frame(int index, ComponentContainer parent) {
		super(parent.driver.switchTo().frame(index), parent);
		this.frameSelect = FrameSelect.INDEX;
		this.index = index;
	}
	
	/**
	 * Constructor for frame by name or ID
	 * 
	 * @param nameOrId the name of the frame window, the id of the &lt;frame&gt; or &lt;iframe&gt; element, 
	 * or the (zero-based) index
	 * @param parent frame parent
	 */
	public Frame(String nameOrId, ComponentContainer parent) {
		super(parent.driver.switchTo().frame(nameOrId), parent);
		this.frameSelect = FrameSelect.NAME_OR_ID;
		this.nameOrId = nameOrId;
	}

	@Override
	protected WebDriver switchToContext() {
		switch (frameSelect) {
		case ELEMENT:
			driver.switchTo().frame(element);
			break;
			
		case INDEX:
			driver.switchTo().frame(index);
			break;
			
		case NAME_OR_ID:
			driver.switchTo().frame(nameOrId);
			break;
		}
		return driver;
	}

}
