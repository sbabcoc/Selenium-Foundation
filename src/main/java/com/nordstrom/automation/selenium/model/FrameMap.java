package com.nordstrom.automation.selenium.model;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class FrameMap<V extends Frame> extends ContainerMap<V> {

	FrameMap(ComponentContainer parent, Class<V> containerType, By locator) {
		super(parent, containerType, locator);
	}

	@Override
	Class<?>[] getArgumentTypes() {
		return Frame.ARG_TYPES_3;
	}

	@Override
	Object[] getArguments(WebElement element) {
		return new Object[] {(RobustWebElement) element, parent};
	}
}