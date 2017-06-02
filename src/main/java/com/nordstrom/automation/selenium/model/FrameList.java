package com.nordstrom.automation.selenium.model;

import org.openqa.selenium.By;

public class FrameList<E extends Frame> extends ContainerList<E> {
	
	FrameList(ComponentContainer parent, Class<E> containerType, By locator) {
		super(parent, containerType, locator);
	}
	
}