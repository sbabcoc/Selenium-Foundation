| [ModelTest.java](ModelTest.md) | [ExamplePage.java](ExamplePage.md) | [TableComponent.java](TableComponent.md) | [TableRowComponent.java](TableRowComponent.md) | [FrameComponent.java](FrameComponent.md) | **ShadowRootComponent** |

# Sample Code

###### ShadowRootComponent.java
```java
package com.nordstrom.automation.selenium.model;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;

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
    
    public String getContent() {
        return findElement(Using.HEADING).getText();
    }

    public static Object getKey(RobustWebElement context) {
        WebDriver driver = context.getWrappedDriver();
        Object key = driver.findElement(Using.HEADING.locator).getText();
        return key;
    }
}
```