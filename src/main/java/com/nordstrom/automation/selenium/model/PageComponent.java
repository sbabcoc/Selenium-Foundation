package com.nordstrom.automation.selenium.model;

import java.util.Arrays;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.WrapsElement;

import com.nordstrom.automation.selenium.exceptions.ComponentStillDisplayedTimeoutException;
import com.nordstrom.automation.selenium.exceptions.ComponentStillInvisibleTimeoutException;
import com.nordstrom.automation.selenium.support.Coordinator;

/**
 * Extend this class when modeling a browser page component.
 * <p>
 * This class defines three constructors:
 * <ol>
 *     <li>Create {@link #PageComponent(By, ComponentContainer) page component by locator}.</li>
 *     <li>Create {@link #PageComponent(By, int, ComponentContainer) page component by locator and index}.</li>
 *     <li>Create {@link #PageComponent(RobustWebElement, ComponentContainer) page component by context element}.</li>
 * </ol>
 * Your page component class can implement any of these constructors, but #3 ({@code page component by context
 * element}) is required if you wish to collect multiple instances in a {@link ComponentList} or {@link ComponentMap}.
 * Also note that you must override {@link #hashCode()} and {@link #equals(Object)} if you add significant fields.
 */
public class PageComponent extends ComponentContainer implements WrapsElement {

    private Class<?>[] argumentTypes;
    private Object[] arguments;
    
    private static final Class<?>[] ARG_TYPES_1 = {By.class, ComponentContainer.class};
    private static final Class<?>[] ARG_TYPES_2 = {By.class, Integer.TYPE, ComponentContainer.class};
    
    /**
     * Constructor for page component by element locator
     * 
     * @param locator component context element locator
     * @param parent component parent container
     */
    public PageComponent(final By locator, final ComponentContainer parent) {
        this(locator, -1, parent);
        
        argumentTypes = ARG_TYPES_1;
        arguments = new Object[] {locator, parent};
    }
    
    /**
     * Constructor for page component by element locator and index
     * 
     * @param locator component context element locator
     * @param index component context index (-1 = non-indexed)
     * @param parent component parent container
     */
    public PageComponent(final By locator, final int index, final ComponentContainer parent) {
        this((RobustWebElement) RobustElementFactory.getElement(parent, locator, index), parent);
        
        argumentTypes = ARG_TYPES_2;
        arguments = new Object[] {locator, index, parent};
    }
    
    /**
     * Constructor for page component by context element
     * 
     * @param element component context element
     * @param parent component parent
     */
    public PageComponent(final RobustWebElement element, final ComponentContainer parent) {
        super(element, parent);
        
        argumentTypes = ComponentContainer.getCollectibleArgs();
        arguments = new Object[] {element, parent};
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected SearchContext switchToContext() {
        return this;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public SearchContext getWrappedContext() {
        return ((RobustWebElement) context).getWrappedContext();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public SearchContext refreshContext(final long expiration) {
        // if this context is past the expiration
        if (expiration >= acquiredAt()) {
            // refresh context ancestry
            parent.refreshContext(expiration);
            // refresh context element
            ((RobustWebElement) context).refreshContext(expiration);
        }
        return this;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public long acquiredAt() {
        return ((RobustWebElement) context).acquiredAt();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?>[] getArgumentTypes() {
        return Arrays.copyOf(argumentTypes, argumentTypes.length);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] getArguments() {
        return Arrays.copyOf(arguments, arguments.length);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public WebElement getWrappedElement() {
        return (WebElement) context;
    }
    
    /**
     * Get the viewport element for this component.
     * <p>
     * <b>NOTE</b>: The default implementation of this method returns the component's context element. If the context
     * element is always hidden, override this method to return the component's largest visible container element.
     * 
     * @return page component viewport element
     */
    public RobustWebElement getViewport() {
        return (RobustWebElement) context;
    }
    
    /**
     * Determine if this component is visible
     * 
     * @return 'true' if component is visible; otherwise 'false'
     */
    public boolean isDisplayed() {
        RobustWebElement element = getViewport();
        if (element.hasReference()) {
            return element.isDisplayed();
        }
        return false;
    }
    
    /**
     * Determine if this component is absent or hidden
     * 
     * @return 'true' if component is absent or hidden; otherwise 'false'
     */
    public boolean isInvisible() {
        RobustWebElement element = getViewport();
        if (element.hasReference()) {
            try {
                return ! element.getWrappedElement().isDisplayed();
            } catch (StaleElementReferenceException e) {
                getLogger().warn("Container element no longer exists");
            }
        }
        return true;
    }
    
    /**
     * Returns a 'wait' proxy that determines if this page component is visible
     * 
     * @return page component if visible; otherwise 'null'
     */
    public static Coordinator<PageComponent> componentIsVisible() {
        return new Coordinator<PageComponent>() {
            
            /**
             * {@inheritDoc}
             */
            @Override
            @SuppressWarnings("squid:S1774")
            public PageComponent apply(final SearchContext context) {
                PageComponent component = verifyContext(context);
                return (component.isDisplayed()) ? component : null;
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString() {
                return "page component to be visible";
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public TimeoutException differentiateTimeout(TimeoutException e) {
                return new ComponentStillInvisibleTimeoutException(e.getMessage(), e.getCause());
            }
        };
    }
    
    /**
     * Returns a 'wait' proxy that determines if this page component is hidden
     * 
     * @return page component if hidden; otherwise 'null'
     */
    public static Coordinator<PageComponent> componentIsHidden() {
        return new Coordinator<PageComponent>() {
            
            /**
             * {@inheritDoc}
             */
            @Override
            @SuppressWarnings("squid:S1774")
            public PageComponent apply(final SearchContext context) {
                PageComponent component = verifyContext(context);
                return (component.isInvisible()) ? component : null;
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString() {
                return "page component to be absent or hidden";
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public TimeoutException differentiateTimeout(TimeoutException e) {
                return new ComponentStillDisplayedTimeoutException(e.getMessage(), e.getCause());
            }
        };
    }
    
    /**
     * Determine if the specified search context is a page component
     * 
     * @param context search context in question
     * @return search context as page component (throws an exception otherwise)
     * @throws UnsupportedOperationException The specified search context isn't a page component
     */
    protected static PageComponent verifyContext(final SearchContext context) {
        if (context instanceof PageComponent) {
            return (PageComponent) context;
        }
        
        throw new UnsupportedOperationException("Wait object search context is not a page component");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Arrays.hashCode(argumentTypes);
        result = prime * result + Arrays.hashCode(arguments);
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({"squid:S1142", "squid:S1126"})
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        PageComponent other = (PageComponent) obj;
        if (!Arrays.equals(argumentTypes, other.argumentTypes))
            return false;
        if (!Arrays.equals(arguments, other.arguments))
            return false;
        return true;
    }
}
