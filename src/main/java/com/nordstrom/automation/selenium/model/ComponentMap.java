package com.nordstrom.automation.selenium.model;

import org.openqa.selenium.By;

public class ComponentMap<V extends PageComponent> extends ContainerMap<V> {

	ComponentMap(ComponentContainer parent, Class<V> containerType, By locator) {
		super(parent, containerType, locator);
	}

}