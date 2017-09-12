package com.nordstrom.automation.selenium.model;

import org.openqa.selenium.By;

/**
 * This class defines a map for <b>Selenium Foundation</b> frame objects.
 * <p>
 * <b>NOTE</b>: This class implements a read-only map; all methods that would alter the composition of the collection
 * (e.g. - {@link #put(Object, Object)}) result in {@link UnsupportedOperationException}.
 *
 * @param <V> the class of frame objects collected by this map
 */
public class FrameMap<V extends Frame> extends ContainerMap<V> {

    FrameMap(ComponentContainer parent, Class<V> containerType, By locator) {
        super(parent, containerType, locator);
    }

}
