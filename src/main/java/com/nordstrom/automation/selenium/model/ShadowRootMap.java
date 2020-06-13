package com.nordstrom.automation.selenium.model;

import org.openqa.selenium.By;

/**
 * This class defines a map for <b>Selenium Foundation</b> shadow root objects.
 * <p>
 * <b>NOTE</b>: This class implements a read-only map; all methods that would alter the composition of the collection
 * (e.g. - {@link #put(Object, Object)}) result in {@link UnsupportedOperationException}.
 *
 * @param <V> the class of shadow root objects collected by this map
 */
public class ShadowRootMap<V extends ShadowRoot> extends ContainerMap<V> {

    /**
     * Constructor for shadow root map with parent, type, and locator
     * 
     * @param parent parent container
     * @param containerType container type
     * @param locator container context element locator
     */
    ShadowRootMap(final ComponentContainer parent, final Class<V> containerType, final By locator) {
        super(parent, containerType, locator);
    }

}
