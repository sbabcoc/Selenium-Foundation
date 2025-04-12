package com.nordstrom.automation.selenium.examples;

import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import com.nordstrom.automation.selenium.core.ByType;
import com.nordstrom.automation.selenium.model.ComponentContainer;
import com.nordstrom.automation.selenium.model.RobustWebElement;
import com.nordstrom.automation.selenium.model.ShadowRoot;

/**
 * This class is the model for example page shadow root components.
 */
public class ShadowRootComponent extends ShadowRoot {

    /**
     * Constructor for shadow root by element locator
     * 
     * @param locator component context element locator
     * @param parent component parent container
     */
    public ShadowRootComponent(final By locator, final ComponentContainer parent) {
        super(locator, parent);
    }
    
    /**
     * Constructor for shadow root by element locator and index
     * 
     * @param locator component context element locator
     * @param index component context index (-1 = non-indexed)
     * @param parent component parent container
     */
    public ShadowRootComponent(final By locator, final int index, final ComponentContainer parent) {
        super(locator, index, parent);
    }
    
    /**
     * Constructor for shadow root by context element
     * 
     * @param element component context element
     * @param parent component parent
     */
    public ShadowRootComponent(final RobustWebElement element, final ComponentContainer parent) {
        super(element, parent);
    }
    
    private enum Using implements ByEnum {
        HEADING(By.cssSelector("h1")),
        PARA(By.cssSelector("p[id^='para-']")),
        INPUT(By.cssSelector("input[id^='input-field-']")),
        CHECK(By.cssSelector("input[id^='checkbox-']"));
        
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
     * Get heading of this shadow root component.
     * 
     * @return component heading text
     */
    public String getHeading() {
        return findElement(Using.HEADING).getText();
    }
    
    /**
     * Get text content of the paragraphs collection.
     * 
     * @return list of paragraph strings
     */
    public List<String> getParagraphs() {
        List<WebElement> paraList = findElements(Using.PARA);
        return Arrays.asList(paraList.get(0).getText(), paraList.get(1).getText(), paraList.get(2).getText());
    }
    
    /**
     * Get CSS locator string for the form input field.
     * 
     * @return input field locator string
     */
    public String getInputLocator() {
        return ByType.cssLocatorFor(Using.INPUT);
    }
    
    /**
     * Set value of the form input field to specified string.
     * 
     * @param value input value string
     * @return {@code true} if field value changed; otherwise {@code false}
     */
    public boolean setInputValue(String value) {
        return updateValue(findElement(Using.INPUT), value);
    }
    
    /**
     * Get current value of the form input field.
     * 
     * @return input field value
     */
    public String getInputValue() {
        return findElement(Using.INPUT).getAttribute("value");
    }
    
    /**
     * Get CSS locator string for the form check box.
     * 
     * @return check box locator string
     */
    public String getCheckLocator() {
        return ByType.cssLocatorFor(Using.CHECK);
    }
    
    /**
     * Determine if the form check box is checked.
     * 
     * @return {@code true} if box is checked; otherwise {@code false}
     */
    public boolean isBoxChecked() {
        return findElement(Using.CHECK).isSelected();
    }
    
    /**
     * Set value of the form check box to specified boolean.
     * 
     * @param value {@code true} to set 'checked' state; {@code false} to clear it
     * @return {@code true} if 'checked' state changed; otherwise {@code false}
     */
    public boolean setCheckValue(boolean value) {
        return updateValue(findElement(Using.CHECK), value);
    }
    
    /**
     * Get the key that uniquely identifies the specified shadow root context.
     * 
     * @param context shadow root component search context
     * @return shadow root component key
     */
    public static Object getKey(final SearchContext context) {
        return getShadowRoot(context).findElement(Using.HEADING.locator).getText();
    }

}
