package com.nordstrom.automation.selenium.examples;

import org.openqa.selenium.By;
import com.nordstrom.automation.selenium.model.ComponentContainer;
import com.nordstrom.automation.selenium.model.PageComponent;

/**
 * This class is the model for the <b>TextEdit</b> document 'save' sheet.
 */
public class TextEditDocumentSaveSheet extends PageComponent {

    /**
     * Constructor for page component by element locator
     * 
     * @param locator component context element locator
     * @param parent component parent container
     */
	public TextEditDocumentSaveSheet(By locator, ComponentContainer parent) {
		super(locator, parent);
	}

	/**
     * This enumeration defines element locator constants.
     */
    protected enum Using implements ByEnum {
    	/** 'save' sheet <b>Delete</b> button element */
        DELETE_BUTTON(By.xpath(".//XCUIElementTypeButton[@title='Delete']"));
        
        private final By locator;
        
        Using(By locator) {
            this.locator = locator;
        }

        @Override
        public By locator() {
            return locator;
        }
    }
    
    /**
     * Delete the document associated with this 'save' sheet.
     */
    public void deleteDocument() {
    	findElement(Using.DELETE_BUTTON).click();
    	getWait().until(componentIsHidden());
    }
    
}
