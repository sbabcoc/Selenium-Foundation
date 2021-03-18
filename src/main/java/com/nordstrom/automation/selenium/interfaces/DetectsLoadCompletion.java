package com.nordstrom.automation.selenium.interfaces;

import org.openqa.selenium.SearchContext;

import com.nordstrom.automation.selenium.model.ComponentContainer;

/**
 * Page classes that model pages with complex loading behavior implement this interface to provide scenario-specific
 * detection of page load completion. This is typically required for single-page applications or more conventional
 * multi-page applications that use dynamic load techniques (e.g. - AJAX).
 */
public interface DetectsLoadCompletion {
    
    /**
     * Determine if the page has finished loading.
     * 
     * @return 'true' if the page has finished loading; otherwise 'false'
     */
    boolean isLoadComplete();
    
    /**
     * Get the container search context
     * <p>
     * <b>NOTE</b>: This method is lifted from the {@link ComponentContainer} class.
     * 
     * @return container search context
     */
    SearchContext getContext();
    
}
