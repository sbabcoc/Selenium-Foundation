package com.nordstrom.automation.selenium.examples;

import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import com.nordstrom.automation.selenium.model.ComponentContainer;
import com.nordstrom.automation.selenium.model.PageComponent;
import com.nordstrom.automation.selenium.model.RobustWebElement;

/**
 * This class is the model for example page table row component.
 */
public class TableRowComponent extends PageComponent {

    /**
     * Constructor for page component by element locator
     * 
     * @param locator component context element locator
     * @param parent component parent container
     */
    public TableRowComponent(By locator, ComponentContainer parent) {
        super(locator, parent);
    }
    
    /**
     * Constructor for page component by context element
     * 
     * @param element component context element
     * @param parent component parent
     */
    public TableRowComponent(RobustWebElement element, ComponentContainer parent) {
        super(element, parent);
    }
    
    /**
     * This enumeration defines element locator constants.
     */
    protected enum Using implements ByEnum {
        /** table row cell (header/body) */
        TBL_CELL(By.cssSelector("th,td"));
        
        private final By locator;
        
        Using(By locator) {
            this.locator = locator;
        }

        @Override
        public By locator() {
            return locator;
        }
    }
    
    private List<WebElement> cells;
    private int refreshCount;
    
    /**
     * Get table row content in tabular format.
     * 
     * @return table row content as list
     */
    public List<String> getContent() {
        List<WebElement> cells = getCells();
        return Arrays.asList(cells.get(0).getText(), cells.get(1).getText(), cells.get(2).getText());
    }
    
    /**
     * Get table row elements.
     * 
     * @return list of {@link WebElement} references
     */
    private List<WebElement> getCells() {
        if (cells == null) {
            cells = findElements(Using.TBL_CELL);
        }
        return cells;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public SearchContext refreshContext(long expiration) {
        refreshCount++;
        return super.refreshContext(expiration);
    }
    
    /**
     * Get table row refresh count.
     * 
     * @return table row refresh count
     */
    public int getRefreshCount() {
        return refreshCount;
    }
    
}
