package com.nordstrom.automation.selenium.examples;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;

import com.nordstrom.automation.selenium.interfaces.DetectsLoadCompletion;
import com.nordstrom.automation.selenium.model.ComponentContainer;
import com.nordstrom.automation.selenium.model.Frame;
import com.nordstrom.automation.selenium.model.RobustWebElement;

/**
 * This class is the model for example page frame components.
 */
public class FrameComponent extends Frame implements DetectsLoadCompletion<FrameComponent> {
    
    /**
     * Constructor for frame by locator
     * 
     * @param locator frame element locator
     * @param parent frame parent
     */
    public FrameComponent(By locator, ComponentContainer parent) {
        super(locator, parent);
    }
    
    /**
     * Constructor for frame by locator and index
     * 
     * @param locator frame element locator
     * @param index frame element index
     * @param parent frame parent
     */
    public FrameComponent(By locator, int index, ComponentContainer parent) {
        super(locator, index, parent);
    }
    
    /**
     * Constructor for frame by context element
     * 
     * @param element frame context element
     * @param parent frame parent
     */
    public FrameComponent(RobustWebElement element, ComponentContainer parent) {
        super(element, parent);
    }
    
    /**
     * Constructor for frame by index
     * 
     * @param index (zero-based) frame index
     * @param parent frame parent
     */
    public FrameComponent(int index, ComponentContainer parent) {
        super(index, parent);
    }
    
    /**
     * Constructor for frame by name or ID
     * 
     * @param nameOrId the name of the frame window, the id of the &lt;frame&gt; or
     *            &lt;iframe&gt; element, or the (zero-based) frame index
     * @param parent frame parent
     */
    public FrameComponent(String nameOrId, ComponentContainer parent) {
        super(nameOrId, parent);
    }
    
    private enum Using implements ByEnum {
        HEADING(By.cssSelector("h1"));
        
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
        return findOptional(Using.HEADING).hasReference();
    }

    /**
     * Get text content of this frame component.
     * 
     * @return frame component text content
     */
    public String getPageContent() {
        return findElement(Using.HEADING).getText();
    }

    /**
     * Get the key that uniquely identifies the specified frame context.
     * 
     * @param context frame component search context
     * @return frame component key
     */
    public static Object getKey(SearchContext context) {
        RobustWebElement element = (RobustWebElement) context;
        WebDriver driver = switchTo(element);
        Object key = driver.findElement(Using.HEADING.locator).getText();
        switchToParentFrame(element);
        return key;
    }

}
