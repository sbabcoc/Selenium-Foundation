# Introduction

When modeling a web application, it's common to encounter scenarios where the presentation of a page or component varies from one run to the next. For example:

* You're modeling a site that uses responsive layout and have configured separate runs to verify site behavior in each of the supported breakpoint dimensions.
* The UI/UX team is running an A/B test to evaluate multiple design ideas to determine which one increases engagement and conversion rates.
* You're maintaining an automation suite through a design transition where a new version of a site-wide component (e.g. - header navigation) has been installed on some pages, but other pages are still using the old version.

To enable seamless handling of these sorts of scenarios, **Selenium Foundation** provides a `container resolution` feature that automatically selects the version of a page or component model that corresponds to the current presentation in the browser.

## Defining a Resolvable Container Set

The container resolution feature of **Selenium Foundation** manages set of classes that implement a common interface. The base class defines the interface and specifies a resolver that inspects the DOM and selects the subclass that corresponds to what it finds. The project unit test collection includes a simple example for reference.

### Part A: Generic Base Class

###### TabPage.java
```java
package com.nordstrom.automation.selenium.examples;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.nordstrom.automation.selenium.annotations.Resolver;
import com.nordstrom.automation.selenium.interfaces.DetectsLoadCompletion;
import com.nordstrom.automation.selenium.model.Page;

/**
 * This class is the abstract model for the tab pages opened by the "Open A/B Tab" button on the 'Example' page.
 * The {@link Resolver} annotation specifies the container resolver that selects the concrete subclass model for
 * the specific page that gets opened ({@link TabPageA} or {@link TabPageB}).
 */
@Resolver(TabPageResolver.class)
public class TabPage extends Page implements DetectsLoadCompletion {
    
    /**
     * Constructor for tab page view context.
     * 
     * @param driver driver object
     */
    public TabPage(WebDriver driver) {
        super(driver);
    }
    
    /**
     * This enumeration defines element locator constants.
     */
    enum Using implements ByEnum {
        /** page heading locator */
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
     * Get content of tab page.
     * 
     * @return tab page content
     */
    public String getPageContent() {
        return findElement(Using.HEADING).getText();
    }

    /**
     * Verify content of tab page.
     * 
     * @return {@code true} if verification succeeds; otherwise {@code false}
     */
    public boolean verifyContent() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLoadComplete() {
        return verifyContent();
    }
}
```

The preceding sample code defines the common interface for two similar pages that get selected at random and opened in a new window when the **`Open A/B Tab`** button is clicked. The interface of this consists of two methods: `getPageContent` and `verifyContent`. To ensure proper page load synchronization, this class implements the **`DetectsLoadCompletion`** interface. Note that the `isLoadComplete` method invokes a method that throws **UnsupportedOperationException**, which ensures that no one tries to use instances of this generic base class.

### Part B: Container Resolver

##### TabPageResolver.java
```java
package com.nordstrom.automation.selenium.examples;

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import com.nordstrom.automation.selenium.AbstractSeleniumConfig.WaitType;
import com.nordstrom.automation.selenium.exceptions.UnresolvedContainerTypeException;
import com.nordstrom.automation.selenium.interfaces.ContainerResolver;
import com.nordstrom.automation.selenium.support.Coordinators;

/**
 * This class implements the container resolver for the tab pages opened by the "Open A/B Tab" button on the 'Example'
 * page.
 */
public class TabPageResolver implements ContainerResolver<TabPage> {
    @Override
    public TabPage resolve(TabPage container) {
        try {
            WebElement element = container.getWait(WaitType.PAGE_LOAD)
                    .until(Coordinators.presenceOfElementLocated(TabPage.Using.HEADING.locator()));
            String pageContent = element.getText();
            if (pageContent.equals(TabPageA.EXPECT_CONTENT)) {
                return new TabPageA(container.getWrappedDriver());
            }
            if (pageContent.equals(TabPageB.EXPECT_CONTENT)) {
                return new TabPageB(container.getWrappedDriver());
            }
            throw new UnresolvedContainerTypeException("Unsupported tab page heading: " + pageContent);
        } catch (TimeoutException e) {
            throw new UnresolvedContainerTypeException("Tab page heading not found");
        }
    }
}
```

The preceding sample code defines the container resolver that determines which concrete subclass model to use based on the value of the tab page heading. Note that this code acquires the header element through a "wait" proxy, because no load completion checks have been performed. The implementation of each container resolver will be scenario-specific and won't necessarily require synchronized access to DOM elements, but this example illustrates a typical approach.

## Part C: Concrete Subclass Model

##### TabPageA.java
```java
package com.nordstrom.automation.selenium.examples;

import org.openqa.selenium.WebDriver;

import com.nordstrom.automation.selenium.annotations.PageUrl;

/**
 * This class is the model for the first concrete tab page subclass opened by the "Open A/B Tab" button on the
 * 'Example' page.
 */
@PageUrl("/grid/admin/FrameA_Servlet")
public class TabPageA extends TabPage {
    
    /** expected content */
    public static String EXPECT_CONTENT = "Frame A";

    /**
     * Constructor for tab page view context.
     * 
     * @param driver driver object
     */
    public TabPageA(WebDriver driver) {
        super(driver);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean verifyContent() {
        return getPageContent().equals(EXPECT_CONTENT);
    }
}
```

The preceding sample code defined the first of two concrete tab page subclasses. In this contrived example, the only difference between the two expected variations is the text of the page heading. In real-world scenarios, the implementations of component interface methods may be entirely different, depending on the precise nature of the variations being modeled.

## Page/Component Load Synchronization

As noted above, container resolvers are responsible for ensuring that the target DOM is sufficiently stable to provide reliable subclass resolution. Once the concrete subclass has been resolved, the standard synchronization mechanisms are invoked to ensure that the expected landing page is loaded and that the defined load completion criteria are satisfied.

The sample code in the previous section shows implementation related to these features:

* The `@PageUrl` annotation is used for landing page verification.
* The `verifyContent()` method is invoked by the base class `isLoadComplete()` method, which is the interface method used to check the load completion criteria.

> Written with [StackEdit](https://stackedit.io/).
