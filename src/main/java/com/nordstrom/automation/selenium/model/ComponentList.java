package com.nordstrom.automation.selenium.model;

import org.openqa.selenium.By;

/**
 * This class defines a list for <b>Selenium Foundation</b> page component objects.
 * <p>
 * <b>NOTE</b>: This class implements a read-only list; all methods that would alter the composition of the collection
 * (e.g. - {@link #add(Object)}) result in {@link UnsupportedOperationException}.
 *
 * @param <E> the class of page component objects collected by this list
 */
public class ComponentList<E extends PageComponent> extends ContainerList<E> {

    /**
     * Constructor for component list with parent, type, and locator
     * 
     * @param parent parent container
     * @param containerType container type
     * @param locator container context element locator
     */
    ComponentList(final ComponentContainer parent, final Class<E> componentType, final By locator) {
        super(parent, componentType, locator);
    }

}
