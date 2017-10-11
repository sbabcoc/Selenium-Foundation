package com.nordstrom.automation.selenium.interfaces;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.internal.WrapsDriver;

/**
 * Classes implement this interface to provide access to their underlying native Selenium
 * {@link SearchContext}. This interface also defines methods related to automatic recovery from 
 * {@link org.openqa.selenium.StaleElementReferenceException StaleElementReferenceException}
 * failures.
 */
public interface WrapsContext extends WrapsDriver {
    
    /**
     * Switch the driver to the context that underlies this object.
     * 
     * @return this object's underlying search context
     */
    SearchContext switchTo();
    
    /**
     * Get the underlying search context for this object.
     * 
     * @return object search context
     */
    SearchContext getWrappedContext();
    
    /**
     * Refresh the underlying search context for this object.
     * 
     * @param expiration expiration time of context chain
     * @return object search context
     */
    SearchContext refreshContext(long expiration);
    
    /**
     * Determine when the underlying search context for this object was acquired.
     * 
     * @return search context acquisition time (from {@link System#currentTimeMillis()})
     */
    long acquiredAt();
    
}
