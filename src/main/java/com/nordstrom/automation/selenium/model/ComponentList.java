package com.nordstrom.automation.selenium.model;

import org.openqa.selenium.By;

public class ComponentList<E extends PageComponent> extends ContainerList<E> {

    ComponentList(ComponentContainer parent, Class<E> componentType, By locator) {
        super(parent, componentType, locator);
    }

}