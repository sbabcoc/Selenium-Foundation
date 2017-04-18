package com.nordstrom.automation.selenium.model;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

public class Frame extends Page {
	
	private FrameSelect frameSelect;
	private By locator;
	private RobustWebElement element;
	private int index;
	private String nameOrId;
	
	private static final Class<?>[] ELEMENT_ARG_TYPES = {By.class, Integer.TYPE, ComponentContainer.class};
	private static final Class<?>[] INDEX_ARG_TYPES = {Integer.TYPE, ComponentContainer.class};
	private static final Class<?>[] NAME_OR_ID_ARG_TYPES = {String.class, ComponentContainer.class};
	
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
		super(parent.driver, parent);
		this.frameSelect = FrameSelect.ELEMENT;
		this.locator = locator;
		this.index = index;
		
		this.element = new RobustWebElement(null, parent, locator, index);
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
	@SuppressWarnings("unchecked")
	public <T extends ComponentContainer> T enhanceContainer(T container) {
		Class<? extends ComponentContainer> type = container.getClass();
		if (Enhancer.isEnhanced(type)) return container;
		
		Frame frame = (Frame) container;
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(type);
		enhancer.setCallbacks(new Callback[] {ContainerMethodInterceptor.INSTANCE, NoOp.INSTANCE});
		enhancer.setCallbackFilter(this);
		
		switch (frame.frameSelect) {
		case ELEMENT:
			return (T) enhancer.create(ELEMENT_ARG_TYPES, new Object[] {frame.locator, frame.index, frame.parent});
			
		case INDEX:
			return (T) enhancer.create(INDEX_ARG_TYPES, new Object[] {frame.index, frame.parent});
			
		case NAME_OR_ID:
			return (T) enhancer.create(NAME_OR_ID_ARG_TYPES, new Object[] {frame.nameOrId, frame.parent});
		}
		throw new AssertionError("This is unreachable");
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

}
