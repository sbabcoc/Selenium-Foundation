package com.nordstrom.automation.selenium.model;

import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

public class Frame extends Page {
	
	private FrameSelect frameSelect;
	private WebElement element;
	private int index;
	private String nameOrId;
	
	private static final Class<?>[] ELEMENT_ARG_TYPES = {WebElement.class, ComponentContainer.class};
	private static final Class<?>[] INDEX_ARG_TYPES = {Integer.TYPE, ComponentContainer.class};
	private static final Class<?>[] NAME_OR_ID_ARG_TYPES = {String.class, ComponentContainer.class};
	
	private enum FrameSelect {
		INDEX, 
		NAME_OR_ID, 
		ELEMENT
	}
	
	/**
	 * Constructor for frame by element
	 * 
	 * @param element frame container element
	 * @param parent frame parent
	 */
	public Frame(WebElement element, ComponentContainer parent) {
		super(parent.driver, parent);
		this.frameSelect = FrameSelect.ELEMENT;
		this.element = element;
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
	
	@Override
	@SuppressWarnings("unchecked")
	public <T extends ComponentContainer> T enhanceContainer(T container) {
		Class<? extends ComponentContainer> type = container.getClass();
		if (Enhancer.isEnhanced(type)) return container;
		
		Frame frame = (Frame) container;
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(type);
		enhancer.setCallbacks(new Callback[] {ContainerMethodInterceptor.INSTANCE, NoOp.INSTANCE});
		enhancer.setCallbackFilter(this);
		
		T enhanced = null;
		frame.parent.switchTo();
		switch (frame.frameSelect) {
		case ELEMENT:
			enhanced = (T) enhancer.create(ELEMENT_ARG_TYPES, new Object[] {frame.element, frame.parent});
			break;
			
		case INDEX:
			enhanced = (T) enhancer.create(INDEX_ARG_TYPES, new Object[] {frame.index, frame.parent});
			break;
			
		case NAME_OR_ID:
			enhanced = (T) enhancer.create(NAME_OR_ID_ARG_TYPES, new Object[] {frame.nameOrId, frame.parent});
			break;
		}
		return enhanced;
	}

}
