package com.nordstrom.automation.selenium.model;

import org.openqa.selenium.By;

public class FrameMap<V extends Frame> extends ContainerMap<V> {

	FrameMap(ComponentContainer parent, Class<V> containerType, By locator) {
		super(parent, containerType, locator);
	}

}