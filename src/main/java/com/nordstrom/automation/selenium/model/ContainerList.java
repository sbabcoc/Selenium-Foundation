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
 * (e.g. - {@link #add(Object)}) result in {@link UnsupportedOperationException}.
 *
 * @param <E> the class of container objects collected by this list
 */
abstract class ContainerList<E extends ComponentContainer> extends AbstractList<E> {

    /** parent for containers */
    protected ComponentContainer parent;
    /** type of containers */
    protected Class<E> containerType;
    /** context element locator */
    protected By locator;
    
    /** list of context elements */
    protected List<WebElement> elements;
    /** list of containers */
    protected List<E> containers;
    
    /**
     * Constructor for container list with parent, type, and locator
     * 
     * @param parent parent container
     * @param containerType container type
     * @param locator container context element locator
     */
    ContainerList(final ComponentContainer parent, final Class<E> containerType, final By locator) {
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
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return containers.size();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public E get(final int index) {
        E container = containers.get(index);
        if (container == null) {
            container = ComponentContainer.newContainer(containerType, getArgumentTypes(), getArguments(index));
            container = container.enhanceContainer(container);
            containers.set(index, container);
            
            // if load completion logic is spec'd, wait for it
            ContainerMethodInterceptor.waitForLoadCompletion(container);
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
    Object[] getArguments(final int index) {
        RobustWebElement element = (RobustWebElement) elements.get(index);
        return new Object[] {element, parent};
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + parent.hashCode();
        result = prime * result + containerType.hashCode();
        result = prime * result + locator.hashCode();
        result = prime * result + elements.hashCode();
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
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
}
