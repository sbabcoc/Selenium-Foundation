package com.nordstrom.automation.selenium.examples;

import static com.nordstrom.automation.selenium.utility.KeysPayloadBuilder.CMD;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import com.nordstrom.automation.selenium.core.JsUtility;
import com.nordstrom.automation.selenium.core.WebDriverUtils;
import com.nordstrom.automation.selenium.exceptions.NoDocumentAppearedTimeoutException;
import com.nordstrom.automation.selenium.model.Page;
import com.nordstrom.automation.selenium.model.PageComponent;
import com.nordstrom.automation.selenium.support.Coordinator;
import com.nordstrom.automation.selenium.utility.KeysPayloadBuilder;

/**
 * This class is the model for the <b>TextEdit</b> application.
 */
public class TextEditApplication extends Page {

    /**
     * Constructor for main document context
     * 
     * @param driver driver object
     */
	public TextEditApplication(WebDriver driver) {
		super(driver);
	}
	
	static {
		KeysPayloadBuilder.Builder builder = KeysPayloadBuilder.builder();
		CMD_N = builder.key("n", CMD).build();
		CMD_O = builder.key("o", CMD).build();
	}
	
	private static final Map<String, Object> CMD_N;
	private static final Map<String, Object> CMD_O;
	private static final String DOC_WINDOW_TEMPLATE = ".//XCUIElementTypeWindow[@title='%s']";
	
	private TextEditManagementPanel managementPanel;
	
    /**
     * This enumeration defines element locator constants.
     */
    protected enum Using implements ByEnum {
        /** <b>TextEdit</b> document management panel element */
        MANAGEMENT_PANEL(By.xpath(".//XCUIElementTypeWindow[@title='Open']")),
        /** <b>TextEdit</b> document window element */
        DOCUMENT_WINDOW(By.xpath(".//XCUIElementTypeWindow[@identifier='_NS:34']"));
    	
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
     * Open the <b>TextEdit</b> document management panel.
     * 
     * @return {@link TextEditManagementPanel} component
     */
    public TextEditManagementPanel openManagementPanel() {
    	if (managementPanel == null || managementPanel.isInvisible()) {
			JsUtility.run(driver, "macos: keys", CMD_O);
    		managementPanel = new TextEditManagementPanel(Using.MANAGEMENT_PANEL.locator, this);
    	}
    	return managementPanel;
    }
    
    /**
     * Open a new <b>TextEdit</b> document.
     * 
     * @return {@link TextEditDocumentWindow} component
     */
    public TextEditDocumentWindow openNewDocument() {
    	Set<String> initialNames = getOpenDocumentNames();
		JsUtility.run(driver, "macos: keys", CMD_N);
		return (TextEditDocumentWindow) getWait().until(newDocumentIsOpened(initialNames));
    }
    
    /**
     * Get the titles of all open <b>TextEdit</b> document windows.
     * 
     * @return set of document window names
     */
    public Set<String> getOpenDocumentNames() {
		return driver.findElements(Using.DOCUMENT_WINDOW.locator).stream()
				.map(e -> WebDriverUtils.getDomAttributeOf(e, "title")).collect(Collectors.toSet());
    }
    
    /**
     * Returns a 'wait' proxy that determines if a new document has opened.
     * 
     * @param initialNames initial set of document window names
     * @return document window component if visible; otherwise 'null'
     */
    public static Coordinator<PageComponent> newDocumentIsOpened(final Set<String> initialNames) {
        return new Coordinator<PageComponent>() {
            
            /**
             * {@inheritDoc}
             */
            @Override
            public PageComponent apply(final SearchContext context) {
            	if (!(context instanceof TextEditApplication)) {
					throw new IllegalArgumentException("[context] must be 'TextEditApplication'; got '"
							+ context.getClass().getSimpleName() + "'");
            	}
            	TextEditApplication application = TextEditApplication.class.cast(context);
                Set<String> currentNames = application.getOpenDocumentNames();
                currentNames.removeAll(initialNames);
                if (currentNames.isEmpty()) {
                    return null;
                } else {
                	String name = currentNames.iterator().next();
                	String xpath = String.format(DOC_WINDOW_TEMPLATE, name);
                	return new TextEditDocumentWindow(By.xpath(xpath), application);
                }
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString() {
                return "new document to be opened";
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public TimeoutException differentiateTimeout(TimeoutException e) {
                return new NoDocumentAppearedTimeoutException(e.getMessage(), e.getCause());
            }
        };
    }
    
}
