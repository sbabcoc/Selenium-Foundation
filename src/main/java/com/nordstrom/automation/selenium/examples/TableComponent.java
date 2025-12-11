package com.nordstrom.automation.selenium.examples;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import com.nordstrom.automation.selenium.core.WebDriverUtils;
import com.nordstrom.automation.selenium.interfaces.DetectsLoadCompletion;
import com.nordstrom.automation.selenium.model.ComponentContainer;
import com.nordstrom.automation.selenium.model.PageComponent;
import com.nordstrom.automation.selenium.model.RobustWebElement;

/**
 * This class is the model for example page table component.
 */
public class TableComponent extends PageComponent implements DetectsLoadCompletion<TableComponent> {

    /**
     * Constructor for page component by element locator
     * 
     * @param locator component context element locator
     * @param parent component parent container
     */
    public TableComponent(By locator, ComponentContainer parent) {
        super(locator, parent);
    }
    
    /**
     * Constructor for page component by context element
     * 
     * @param element component context element
     * @param parent component parent
     */
    public TableComponent(RobustWebElement element, ComponentContainer parent) {
        super(element, parent);
    }
    
    private TableRowComponent tableHdr;
    private List<TableRowComponent> tableRows;
    private int refreshCount;
    
    /**
     * This enumeration defines element locator constants.
     */
    protected enum Using implements ByEnum {
        /** table header row */
        HDR_ROW(By.cssSelector("tr[id*='-h']")),
        /** table body row */
        TBL_ROW(By.cssSelector("tr[id*='-r']"));
        
        private final By locator;
        
        Using(By locator) {
            this.locator = locator;
        }

        @Override
        public By locator() {
            return locator;
        }
    }
    
    @Override
    public boolean isLoadComplete() {
        return findOptional(Using.HDR_ROW).hasReference();
    }

    /**
     * Get headings of this table component.
     * 
     * @return list of component headings
     */
    public List<String> getHeadings() {
        return getTableHdr().getContent();
    }
    
    /**
     * Get table content in tabular format.
     * 
     * @return table content as list of lists (rows / columns)
     */
    public List<List<String>> getContent() {
        List<List<String>> result = new ArrayList<>();
        for (TableRowComponent row : getTableRows()) {
            result.add(row.getContent());
        }
        return result;
    }
    
    /**
     * Get table header row component.
     * 
     * @return {@link TableRowComponent} for table header
     */
    private TableRowComponent getTableHdr() {
        if (tableHdr == null) {
            tableHdr = new TableRowComponent(Using.HDR_ROW.locator, this);
        }
        return tableHdr;
    }
    
    /**
     * Get list of table body row components.
     * 
     * @return list of {@link TableRowComponent} for table body
     */
    private List<TableRowComponent> getTableRows() {
        if (tableRows == null) {
            tableRows = newComponentList(TableRowComponent.class, Using.TBL_ROW.locator);
        }
        return tableRows;
    }
    
    /**
     * Get the key that uniquely identifies the specified table context.
     * 
     * @param context table component search context
     * @return table component key
     */
    public static Object getKey(SearchContext context) {
        return WebDriverUtils.getDomAttributeOf((WebElement) context, "id");
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
     * Get table refresh count.
     * 
     * @return table refresh count
     */
    public int getRefreshCount() {
        return refreshCount;
    }
    
    /**
     * Get table header refresh count.
     * 
     * @return table header refresh count
     */
    public int getHeadRefreshCount() {
        return getTableHdr().getRefreshCount();
    }
    
    /**
     * Get table body refresh counts.
     * 
     * @return array of table body refresh counts
     */
    public int[] getBodyRefreshCounts() {
        List<TableRowComponent> tableRows = getTableRows();
        int[] counts = new int[tableRows.size()];
        for (int i = 0; i < tableRows.size(); i++) {
            counts[i] = tableRows.get(i).getRefreshCount();
        }
        return counts;
    }
    
}
