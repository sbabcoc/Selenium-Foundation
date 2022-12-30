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

public class ShadowRootComponent extends ShadowRoot {

    public ShadowRootComponent(final By locator, final ComponentContainer parent) {
        super(locator, parent);
    }
    
    public ShadowRootComponent(final By locator, final int index, final ComponentContainer parent) {
        super(locator, index, parent);
    }
    
    public ShadowRootComponent(final RobustWebElement element, final ComponentContainer parent) {
        super(element, parent);
    }
    
    private enum Using implements ByEnum {
        HEADING(By.cssSelector("h1")),
        PARA(By.cssSelector("p[id^='para-']")),
        INPUT(By.cssSelector("input[id^='input-field-']")),
        CHECK(By.cssSelector("input[id^='checkbox-']"));
        
        private By locator;
        
        Using(By locator) {
            this.locator = locator;
        }

        @Override
        public By locator() {
            return locator;
        }
    }
    
    public String getHeading() {
        return findElement(Using.HEADING).getText();
    }
    
    public List<String> getParagraphs() {
        List<WebElement> paraList = findElements(Using.PARA);
        return Arrays.asList(paraList.get(0).getText(), paraList.get(1).getText(), paraList.get(2).getText());
    }
    
    public String getInputLocator() {
        return ByType.cssLocatorFor(Using.INPUT);
    }
    
    public boolean setInputValue(String value) {
        return updateValue(findElement(Using.INPUT), value);
    }
    
    public String getInputValue() {
        return findElement(Using.INPUT).getAttribute("value");
    }
    
    public String getCheckLocator() {
        return ByType.cssLocatorFor(Using.CHECK);
    }
    
    public boolean isBoxChecked() {
        return findElement(Using.CHECK).isSelected();
    }
    
    public boolean setCheckValue(boolean value) {
        return updateValue(findElement(Using.CHECK), value);
    }
    
    public static Object getKey(final SearchContext context) {
        return getShadowRoot(context).findElement(Using.HEADING.locator).getText();
    }

}
