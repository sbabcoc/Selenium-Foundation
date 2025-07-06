package com.nordstrom.automation.selenium.interfaces;

import com.nordstrom.automation.selenium.annotations.Resolver;
import com.nordstrom.automation.selenium.exceptions.UnresolvedContainerTypeException;
import com.nordstrom.automation.selenium.model.ComponentContainer;

/**
 * Implementations of this interface are used to resolve concrete {@link ComponentContainer} types based on current DOM
 * state. This can be used to handle multiple versions of a page or component in which an equivalent set of features is
 * presented in multiple ways based on browser window dimensions (responsive layout) or UI/UX variations (A/B testing).
 * This feature can also select between old and new versions of site-wide components (e.g. - common navigation) during
 * the transition between versions when some pages have been updated but others haven't.
 * 
 * @param <T> context container type
 * @see Resolver
 */
@FunctionalInterface
public interface ContainerResolver<T extends ComponentContainer> {
    /**
     * Resolve this container to its context-specific type.
     * 
     * @param container context container object
     * @return context-specific container object
     * @throws UnresolvedContainerTypeException if concrete type cannot be resolved
     */
    T resolve(T container);
}
