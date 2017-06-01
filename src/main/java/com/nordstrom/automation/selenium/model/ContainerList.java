package com.nordstrom.automation.selenium.model;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

abstract class ContainerList<E extends ComponentContainer> extends AbstractList<E> {

	protected ComponentContainer parent;
	protected Class<E> containerType;
	protected By locator;
	
	protected List<WebElement> elementList;
	protected List<E> containerList;
	protected List<E> immutableView;
	
	ContainerList(ComponentContainer parent, Class<E> containerType, By locator) {
		if (parent == null) throw new IllegalArgumentException("Parent must be non-null");
		if (containerType == null) throw new IllegalArgumentException("Container type must be non-null");
		if (locator == null) throw new IllegalArgumentException("Locator must be non-null");
		
		this.parent = parent;
		this.containerType = containerType;
		this.locator = locator;
		
		elementList = parent.findElements(locator);
		containerList = new ArrayList<>(elementList.size());
		for (int i = 0; i < elementList.size(); i++) {
			containerList.add(null);
		}
	}
	
	@Override
	public int size() {
		return containerList.size();
	}
	
	@Override
	public E get(int index) {
		E container = containerList.get(index);
		if (container == null) {
			container = ComponentContainer.newContainer(containerType, getArgumentTypes(), getArguments(index));
			containerList.set(index, container);
		}
		return container;
	}
	
	/**
	 * 
	 * @return
	 */
	abstract Class<?>[] getArgumentTypes();
	
	/**
	 * 
	 * @param index
	 * @return
	 */
	abstract Object[] getArguments(int index);
}