package com.nordstrom.automation.selenium.model;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

abstract class ContainerList<E extends ComponentContainer> extends AbstractList<E> {

    protected ComponentContainer parent;
    protected Class<E> containerType;
    protected By locator;
    
    protected List<WebElement> elementList;
    protected List<E> containerList;
    protected List<E> immutableView;
    
    ContainerList(ComponentContainer parent, Class<E> containerType, By locator) {
        Objects.requireNonNull(parent, "[parent] must be non-null");
        Objects.requireNonNull(containerType, "[containerType] must be non-null");
        Objects.requireNonNull(locator, "[locator] must be non-null");
        
        ComponentContainer.verifyCollectible(containerType);
        
        this.parent = parent;
        this.containerType = containerType;
        this.locator = locator;
        
        elementList = parent.findElements(locator);
        containerList = new ArrayList<>(elementList.size());
        for (int i = 0; i < elementList.size(); i++) {
            containerList.add(null);
        }
    }
    
    @Override
    public int size() {
        return containerList.size();
    }
    
    @Override
    public E get(int index) {
        E container = containerList.get(index);
        if (container == null) {
            container = ComponentContainer.newContainer(containerType, getArgumentTypes(), getArguments(index));
            container = container.enhanceContainer(container);
            containerList.set(index, container);
        }
        return container;
    }
    
    /**
     * Get array of constructor argument types.
     * 
     * @return array of constructor argument types
     */
    Class<?>[] getArgumentTypes() {
        return ComponentContainer.getCollectibleArgs();
    }
    
    /**
     * Get array of constructor argument values for the specified index.
     * 
     * @param index container list index
     * @return array of constructor argument values
     */
    Object[] getArguments(int index) {
        RobustWebElement element = (RobustWebElement) elementList.get(index);
        return new Object[] {element, parent};
    }
}