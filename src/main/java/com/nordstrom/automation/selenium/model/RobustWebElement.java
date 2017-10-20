package com.nordstrom.automation.selenium.model;

import org.openqa.selenium.WebElement;

/**
 * This interface declares the public API for "robust" web elements, adding the reference-refreshing methods of the
 * {@link ReferenceFetcher} interface to the standard {@link WebElement} interface.
 */
public interface RobustWebElement extends WebElement, ReferenceFetcher {

}
