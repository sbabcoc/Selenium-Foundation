package com.nordstrom.automation.selenium.model;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * This is the abstract base class for all of the container list classes defined by <b>Selenium Foundation</b>.
 * <p>
 * <b>NOTE</b>: This class implements a read-only list; all methods that would alter the composition of the collection
 * (e.g. - {@link #add(Object)}) result in {@link UnsupportOperationException}.
 *
 * @param <E> the class of container objects collected by this list
 */
abstract class ContainerList<E extends ComponentContainer> extends AbstractList<E> {

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = super.hashCode();
        result = PRIME * result + parent.hashCode();
        result = PRIME * result + containerType.hashCode();
        result = PRIME * result + locator.hashCode();
        result = PRIME * result + elements.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        ContainerList<?> other = (ContainerList<?>) obj;
        if (!parent.equals(other.parent))
            return false;
        if (!containerType.equals(other.containerType))
            return false;
        if (!locator.equals(other.locator))
            return false;
        if (!elements.equals(other.elements))
            return false;
        return true;
    }

    protected ComponentContainer parent;
    protected Class<E> containerType;
    protected By locator;
    
    protected List<WebElement> elements;
    protected List<E> containers;
    
    /**
     * Constructor for container list with parent, type, and locator
     * 
     * @param parent parent container
     * @param containerType container type
     * @param locator container context element locator
     */
    ContainerList(ComponentContainer parent, Class<E> containerType, By locator) {
        Objects.requireNonNull(parent, "[parent] must be non-null");
        Objects.requireNonNull(containerType, "[containerType] must be non-null");
        Objects.requireNonNull(locator, "[locator] must be non-null");
        
        ComponentContainer.verifyCollectible(containerType);
        
        this.parent = parent;
        this.containerType = containerType;
        this.locator = locator;
        
        elements = parent.findElements(locator);
        containers = new ArrayList<>(elements.size());
        for (int i = 0; i < elements.size(); i++) {
            containers.add(null);
        }
    }
    
    @Override
    public int size() {
        return containers.size();
    }
    
    @Override
    public E get(int index) {
        E container = containers.get(index);
        if (container == null) {
            container = ComponentContainer.newContainer(containerType, getArgumentTypes(), getArguments(index));
            container = container.enhanceContainer(container);
            containers.set(index, container);
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
        RobustWebElement element = (RobustWebElement) elements.get(index);
        return new Object[] {element, parent};
    }
}
