package com.nordstrom.automation.selenium.model;

import org.openqa.selenium.By;

/**
 * This class defines a map for <b>Selenium Foundation</b> page component objects.
 *
 * @param <E> the class of page component objects collected by this map
 */
public class ComponentMap<V extends PageComponent> extends ContainerMap<V> {

    ComponentMap(ComponentContainer parent, Class<V> containerType, By locator) {
        super(parent, containerType, locator);
    }

}
