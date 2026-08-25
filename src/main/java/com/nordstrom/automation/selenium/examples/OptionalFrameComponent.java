package com.nordstrom.automation.selenium.examples;

import org.openqa.selenium.By;

import com.nordstrom.automation.selenium.model.ComponentContainer;
import com.nordstrom.automation.selenium.model.Frame;
import com.nordstrom.automation.selenium.model.RobustWebElement;

/**
 * This class is the model for the example page's optional frame - a frame whose own context element is
 * obtained via {@code findOptional} and may or may not exist at any given time.
 * <p>
 * Deliberately does <b>not</b> implement {@code DetectsLoadCompletion}, unlike {@link FrameComponent}: that
 * interface triggers an automatic, construction-time load-completion check in
 * {@code ContainerMethodInterceptor} - checked against the actual runtime object returned by a method, not
 * just its declared type, so a component meant to be constructed lazily around a reference that may not exist
 * yet must not implement it at all. "Detects load completion" isn't a meaningful concept for something that
 * might not be there in the first place.
 */
public class OptionalFrameComponent extends Frame {

    /**
     * Constructor for frame by context element
     * 
     * @param element frame context element
     * @param parent frame parent
     */
    public OptionalFrameComponent(RobustWebElement element, ComponentContainer parent) {
        super(element, parent);
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
    
    /**
     * Get text content of this frame component.
     * 
     * @return frame component text content
     */
    public String getPageContent() {
        return findElement(Using.HEADING).getText();
    }

}
