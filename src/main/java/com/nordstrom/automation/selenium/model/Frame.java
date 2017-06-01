package com.nordstrom.automation.selenium.model;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.FindsByCssSelector;
import org.openqa.selenium.internal.FindsByXPath;

import com.nordstrom.automation.selenium.core.WebDriverUtils;

public class Frame extends Page {
	
	private FrameSelect frameSelect;
	private RobustWebElement element;
	private int index;
	private String nameOrId;
	private Class<?>[] argumentTypes;
	private Object[] arguments;
	
	private static final Class<?>[] ARG_TYPES_1 = {RobustWebElement.class, ComponentContainer.class};
	private static final Class<?>[] ARG_TYPES_2 = {Integer.TYPE, ComponentContainer.class};
	private static final Class<?>[] ARG_TYPES_3 = {String.class, ComponentContainer.class};
	
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
	}
	
	/**
	 * Constructor for frame by locator and index
	 * 
	 * @param locator frame element locator
	 * @param index frame element index
	 * @param parent frame parent
	 */
	public Frame(By locator, int index, ComponentContainer parent) {
		this(RobustWebElement.getElement(parent, locator, index), parent);
	}
	
	/**
	 * Constructor for frame by context element
	 * 
	 * @param element frame context element
	 * @param parent frame parent
	 */
	public Frame(RobustWebElement element, ComponentContainer parent) {
		super(parent.driver, parent);
		this.frameSelect = FrameSelect.ELEMENT;
		this.element = element;
		this.index = element.getIndex();
		
		argumentTypes = ARG_TYPES_1;
		arguments = new Object[] {element, parent};
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
		
		argumentTypes = ARG_TYPES_2;
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
		
		argumentTypes = ARG_TYPES_3;
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
	
	public static class FrameList<E extends Frame> extends ContainerList<E> {
		
		private static final By FRAME_BY_CSS = By.cssSelector("iframe,frame");
		private static final By FRAME_BY_XPATH = By.xpath(".//iframe|.//frame");
		
		FrameList(ComponentContainer parent, Class<E> containerType) {
			super(parent, containerType, getLocator(parent));
		}
		
		/**
		 * Get frame locator for the specified parent.
		 * 
		 * @param parent frame parent
		 * @return frame locator
		 */
		private static By getLocator(ComponentContainer parent) {
			WebDriver driver = WebDriverUtils.getDriver(parent);
			if (driver instanceof FindsByXPath) {
				return FRAME_BY_XPATH;
			} else if (driver instanceof FindsByCssSelector) {
				return FRAME_BY_CSS;
			}
			throw new UnsupportedOperationException("Driver must support either Xpath or CSS selectors");
		}

		@Override
		Class<?>[] getArgumentTypes() {
			return ARG_TYPES_1;
		}

		@Override
		Object[] getArguments(int index) {
			RobustWebElement element = (RobustWebElement) elementList.get(index);
			return new Object[] {element, parent};
		}
	}
	
	public static class ComponentMap<V extends PageComponent> extends ContainerMap<V> {

		ComponentMap(ComponentContainer parent, Class<V> containerType, By locator) {
			super(parent, containerType, locator);
		}

		@Override
		Class<?>[] getArgumentTypes() {
			return ARG_TYPES_1;
		}

		@Override
		Object[] getArguments(WebElement element) {
			return new Object[] {(RobustWebElement) element, parent};
		}
	}
}
