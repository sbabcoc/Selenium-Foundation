package com.nordstrom.automation.selenium.model;

import org.openqa.selenium.By;

public class ComponentList<E extends PageComponent> extends ContainerList<E> {

	ComponentList(ComponentContainer parent, Class<E> componentType, By locator) {
		super(parent, componentType, locator);
	}

	@Override
	Class<?>[] getArgumentTypes() {
		return PageComponent.ARG_TYPES_3;
	}

	@Override
	Object[] getArguments(int index) {
		RobustWebElement element = (RobustWebElement) elementList.get(index);
		return new Object[] {element, parent};
	}
}