package com.nordstrom.automation.selenium.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.nordstrom.automation.selenium.interfaces.ContainerResolver;

/**
 * This annotation enables you to specify a resolver class for pages and components that vary based on browser window
 * dimensions (responsive layout) or UI/UX variations (A/B testing). This feature can also select between old and new
 * versions of site-wide components (e.g. - common navigation) during the transition between versions when some pages
 * have been updated but others haven't.
 * 
 *  @see ContainerResolver
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Resolver {
    /**
     * Get the value class of the container resolver.
     * 
     * @return container resolver class
     */
    @SuppressWarnings("rawtypes")
    Class<? extends ContainerResolver> value();
}
