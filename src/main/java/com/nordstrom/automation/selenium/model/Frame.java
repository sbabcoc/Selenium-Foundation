package com.nordstrom.automation.selenium.model;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;

/**
 * Extend this class when modeling a browser frame element.
 * <p>
 * This class defines five constructors:
 * <ol>
 *     <li>Create {@link #Frame(By, ComponentContainer) frame by locator}.</li>
 *     <li>Create {@link #Frame(By, int, ComponentContainer) frame by locator and index}.</li>
 *     <li>Create {@link #Frame(RobustWebElement, ComponentContainer) frame by context element}.</li>
 *     <li>Create {@link #Frame(int, ComponentContainer) frame by index}.</li>
 *     <li>Create {@link #Frame(String, ComponentContainer) frame by name or ID}.</li>
 * </ol>
 * Your frame class can implement any of these constructors, but #3 ({@code frame by context element}) is required if
 * you wish to collect multiple instances in a {@link FrameList} or {@link FrameMap}. Also note that you must override
 * {@link #hashCode()} and {@link #equals(Object)} if you add significant fields.
 */
public class Frame extends Page {

    private FrameSelect frameSelect;
    private RobustWebElement element;
    private int index;
    private String nameOrId;
    
    private static final Class<?>[] ARG_TYPES_1 = {By.class, ComponentContainer.class};
    private static final Class<?>[] ARG_TYPES_2 = {By.class, Integer.TYPE, ComponentContainer.class};
    private static final Class<?>[] ARG_TYPES_4 = {Integer.TYPE, ComponentContainer.class};
    private static final Class<?>[] ARG_TYPES_5 = {String.class, ComponentContainer.class};
    
    private enum FrameSelect {
        ELEMENT,
        INDEX, 
        NAME_OR_ID 
    }
    
    /**
     * Constructor for frame by locator
     * 
     * @param locator frame element locator
     * @param parent frame parent
     */
    public Frame(final By locator, final ComponentContainer parent) {
        this(locator, -1, parent);
        
        argumentTypes = ARG_TYPES_1;
        arguments = new Object[] {locator, parent};
    }
    
    /**
     * Constructor for frame by locator and index
     * 
     * @param locator frame element locator
     * @param index frame element index
     * @param parent frame parent
     */
    public Frame(final By locator, final int index, final ComponentContainer parent) {
        this((RobustWebElement) RobustElementFactory.getElement(parent, locator, index), parent);
        
        argumentTypes = ARG_TYPES_2;
        arguments = new Object[] {locator, index, parent};
    }
    
    /**
     * Constructor for frame by context element
     * 
     * @param element frame context element
     * @param parent frame parent
     */
    public Frame(final RobustWebElement element, final ComponentContainer parent) {
        super(parent.driver, parent);
        this.frameSelect = FrameSelect.ELEMENT;
        this.element = element;
        this.index = element.getIndex();
        
        argumentTypes = ComponentContainer.getCollectibleArgs();
        arguments = new Object[] {element, parent};
    }
    
    /**
     * Constructor for frame by index
     * 
     * @param index (zero-based) frame index
     * @param parent frame parent
     */
    public Frame(final int index, final ComponentContainer parent) {
        super(parent.driver, parent);
        this.frameSelect = FrameSelect.INDEX;
        this.index = index;
        
        argumentTypes = ARG_TYPES_4;
        arguments = new Object[] {index, parent};
    }
    
    /**
     * Constructor for frame by name or ID
     * 
     * @param nameOrId the name of the frame window, the id of the &lt;frame&gt; or
     *            &lt;iframe&gt; element, or the (zero-based) frame index
     * @param parent frame parent
     */
    public Frame(final String nameOrId, final ComponentContainer parent) {
        super(parent.driver, parent);
        this.frameSelect = FrameSelect.NAME_OR_ID;
        this.nameOrId = nameOrId;
        
        argumentTypes = ARG_TYPES_5;
        arguments = new Object[] {nameOrId, parent};
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected SearchContext switchToContext() {
        switch (frameSelect) {
            case ELEMENT:
                driver.switchTo().frame(element);
                break;

            case INDEX:
                driver.switchTo().frame(index);
                break;

            case NAME_OR_ID:
                driver.switchTo().frame(nameOrId);
                break;
        }
        return this;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public SearchContext refreshContext(final long expiration) {
        if (frameSelect == FrameSelect.ELEMENT) {
            element.refreshContext(expiration);
        } else {
            parent.refreshContext(expiration);
        }
        return switchToContext();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public long acquiredAt() {
        if (frameSelect == FrameSelect.ELEMENT) {
            return element.acquiredAt();
        } else {
            return parent.acquiredAt();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?>[] getArgumentTypes() {
        return argumentTypes;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] getArguments() {
        return arguments;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + frameSelect.hashCode();
        result = prime * result + ((element == null) ? 0 : element.hashCode());
        result = prime * result + index;
        result = prime * result + ((nameOrId == null) ? 0 : nameOrId.hashCode());
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
        Frame other = (Frame) obj;
        if (frameSelect != other.frameSelect)
            return false;
        if (element == null) {
            if (other.element != null)
                return false;
        } else if (!element.equals(other.element))
            return false;
        if (index != other.index)
            return false;
        if (nameOrId == null) {
            if (other.nameOrId != null)
                return false;
        } else if (!nameOrId.equals(other.nameOrId))
            return false;
        return true;
    }
}
