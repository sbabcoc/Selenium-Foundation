package com.nordstrom.automation.selenium.model;

import org.openqa.selenium.By;

public class FrameList<E extends Frame> extends ContainerList<E> {
	
	FrameList(ComponentContainer parent, Class<E> containerType, By locator) {
		super(parent, containerType, locator);
	}
	
	@Override
	Class<?>[] getArgumentTypes() {
		return Frame.ARG_TYPES_3;
	}

	@Override
	Object[] getArguments(int index) {
		RobustWebElement element = (RobustWebElement) elementList.get(index);
		return new Object[] {element, parent};
	}
}