package com.nordstrom.automation.selenium.model;

import org.openqa.selenium.By;

/**
 * This class defines a list for <b>Selenium Foundation</b> page component objects.
 *
 * @param <E> the class of page component objects collected by this list
 */
public class ComponentList<E extends PageComponent> extends ContainerList<E> {

    ComponentList(ComponentContainer parent, Class<E> componentType, By locator) {
        super(parent, componentType, locator);
    }

}
