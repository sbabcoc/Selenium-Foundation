package com.nordstrom.automation.selenium.examples;

import org.openqa.selenium.By;

import com.nordstrom.automation.selenium.core.WebDriverUtils;
import com.nordstrom.automation.selenium.model.ComponentContainer;
import com.nordstrom.automation.selenium.model.PageComponent;

/**
 * This class is the model for a <b>TextEdit</b> document window.
 */
public class TextEditDocumentWindow extends PageComponent {

    /**
     * Constructor for page component by element locator
     * 
     * @param locator component context element locator
     * @param parent component parent container
     */
	public TextEditDocumentWindow(By locator, ComponentContainer parent) {
		super(locator, parent);
	}

	/**
     * This enumeration defines element locator constants.
     */
    protected enum Using implements ByEnum {
    	/** document text view element */
        TEXT_VIEW(By.className("XCUIElementTypeTextView")),
    	/** document <b>Close</b> button element */
        CLOSE_BUTTON(By.xpath(".//XCUIElementTypeButton[@identifier='_XCUI:CloseWindow']")),
    	/** document <b>Save</b> sheet element */
        SAVE_SHEET(By.xpath(".//XCUIElementTypeSheet[@label='save']"));
        
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
     * Get the title of this <b>TextEdit</b> document window.
     * 
     * @return document window title
     */
    public String getDocumentTitle() {
    	return WebDriverUtils.getDomAttributeOf(getWrappedElement(), "title");
    }
    
    /**
     * Populate text view of this <b>TextEdit</b> document with the specified string.
     * 
     * @param keys string to type into document text view
     */
    public void modifyDocument(String keys) {
        findElement(Using.TEXT_VIEW).sendKeys(keys);
    }
    
    /**
     * Get content of the text view of this <b>TextEdit</b> document.
     * 
     * @return document text view content
     */
    public String getDocumentContent() {
        return findElement(Using.TEXT_VIEW).getText();
    }
    
    /**
     * Close this <b>TextEdit</b> document without saving.
     */
    public void closeDocumentWithoutSaving() {
    	findElement(Using.CLOSE_BUTTON).click();
    	new TextEditDocumentSaveSheet(Using.SAVE_SHEET.locator, this).deleteDocument();
    	getWait().until(componentIsHidden());
    }
    
}
