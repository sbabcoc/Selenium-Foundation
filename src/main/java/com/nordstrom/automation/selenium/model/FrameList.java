package com.nordstrom.automation.selenium.model;

import org.openqa.selenium.By;

/**
 * This class defines a list for <b>Selenium Foundation</b> frame objects.
 *
 * @param <E> the class of frame objects collected by this list
 */
public class FrameList<E extends Frame> extends ContainerList<E> {
    
    FrameList(ComponentContainer parent, Class<E> containerType, By locator) {
        super(parent, containerType, locator);
    }
    
}
