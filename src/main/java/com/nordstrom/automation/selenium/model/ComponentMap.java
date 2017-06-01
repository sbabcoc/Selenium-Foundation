package com.nordstrom.automation.selenium.model;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class ComponentMap<V extends PageComponent> extends ContainerMap<V> {

	ComponentMap(ComponentContainer parent, Class<V> containerType, By locator) {
		super(parent, containerType, locator);
	}

	@Override
	Class<?>[] getArgumentTypes() {
		return PageComponent.ARG_TYPES_3;
	}

	@Override
	Object[] getArguments(WebElement element) {
		return new Object[] {(RobustWebElement) element, parent};
	}
}