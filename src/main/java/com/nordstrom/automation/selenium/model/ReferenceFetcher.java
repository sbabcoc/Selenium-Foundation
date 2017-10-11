package com.nordstrom.automation.selenium.model;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.WrapsElement;

import com.nordstrom.automation.selenium.interfaces.WrapsContext;
import com.nordstrom.automation.selenium.model.RobustElementFactory.RobustElementWrapper;

public interface ReferenceFetcher extends SearchContext, WrapsElement, WrapsContext, Enhanced {
    
    /**
     * Get a wrapped reference to the first element matching the specified locator.
     * <p>
     * <b>NOTE</b>: Use {@link ReferenceFetcher#hasReference()} to determine if a valid reference was acquired.
     * 
     * @param by the locating mechanism
     * @return robust web element
     */
    WebElement findOptional(By by);

    /**
     * Determine if this robust element wraps a valid reference.
     * 
     * @return 'true' if reference was acquired; otherwise 'false'
     */
    boolean hasReference();

    /**
     * Get the search context for this element.
     * 
     * @return element search context
     */
    WrapsContext getContext();
    
    /**
     * Get the locator for this element.
     * 
     * @return element locator
     */
    By getLocator();
    
    /**
     * Get the element index.
     * <p>
     * <b>NOTE</b>: {@link RobustElementFactory#CARDINAL CARDINAL} = 1st matched reference;
     *              {@link RobustElementFactory#OPTIONAL OPTIONAL} = an optional reference
     * 
     * @return element index (see NOTE)
     */
    int getIndex();
    
    /**
     * Refresh the wrapped element reference.
     * 
     * @param refreshTrigger {@link StaleElementReferenceException} that necessitates reference refresh
     * @return this robust element wrapper with refreshed reference
     */
    RobustElementWrapper refreshReference(final StaleElementReferenceException refreshTrigger);
}
