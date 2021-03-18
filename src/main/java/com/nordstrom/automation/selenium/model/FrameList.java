package com.nordstrom.automation.selenium.model;

import org.openqa.selenium.By;

/**
 * This class defines a list for <b>Selenium Foundation</b> frame objects.
 * <p>
 * <b>NOTE</b>: This class implements a read-only list; all methods that would alter the composition of the collection
 * (e.g. - {@link #add(Object)}) result in {@link UnsupportedOperationException}.
 *
 * @param <E> the class of frame objects collected by this list
 */
public class FrameList<E extends Frame> extends ContainerList<E> {
    
    /**
     * Constructor for frame list with parent, type, and locator
     * 
     * @param parent parent container
     * @param containerType container type
     * @param locator container context element locator
     */
    FrameList(final ComponentContainer parent, final Class<E> containerType, final By locator) {
        super(parent, containerType, locator);
    }
    
}
