package com.nordstrom.automation.selenium.examples;

import static com.nordstrom.automation.selenium.examples.TextEditApplication.newDocumentIsOpened;

import java.util.Set;

import org.openqa.selenium.By;

import com.nordstrom.automation.selenium.model.ComponentContainer;
import com.nordstrom.automation.selenium.model.PageComponent;

/**
 * This class is the model for the <b>TextEdit</b> document management panel.
 */
public class TextEditManagementPanel extends PageComponent {
	
	private TextEditApplication parent;

    /**
     * Constructor for page component by element locator
     * 
     * @param locator component context element locator
     * @param parent component parent container
     */
    public TextEditManagementPanel(By locator, ComponentContainer parent) {
		super(locator, parent);
		this.parent = (TextEditApplication) parent;
	}

	/**
     * This enumeration defines element locator constants.
     */
    protected enum Using implements ByEnum {
        /** 'New Document' image button element */
        NEW_DOCUMENT(By.xpath(".//XCUIElementTypeImage[@label='New Document']"));
        
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
     * Open a new <b>TextEdit</b> document.
     * 
     * @return {@link TextEditDocumentWindow} page component
     */
    public TextEditDocumentWindow openNewDocument() {
    	Set<String> initialNames = parent.getOpenDocumentNames();
        findElement(Using.NEW_DOCUMENT).click();
        getWait().until(componentIsHidden());
		return (TextEditDocumentWindow) parent.getWait().until(newDocumentIsOpened(initialNames));
    }
    
}
