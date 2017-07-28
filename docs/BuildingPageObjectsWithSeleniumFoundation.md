# Introduction
Using the page-model pattern to implement WebDriver automation provides many structural benefits. However, plain-vanilla Selenium doesn't provide much in the way of base-class support for building page objects. **Selenium Foundation** includes a whole range of building blocks to help you create efficient, reliable WebDriver automation.

###### Page-model class
```java
package sixth_example;
 
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import com.nordstrom.automation.selenium.annotations.PageUrl;
import com.nordstrom.automation.selenium.core.ByType.ByEnum;
import com.nordstrom.automation.selenium.interfaces.DetectsLoadCompletion;
import com.nordstrom.automation.selenium.model.Page;
 
@PageUrl("/")
public class GoogleSearchPage extends Page implements DetectsLoadCompletion {
    
    public GoogleSearchPage(WebDriver driver) {
        super(driver);
    }
     
    private enum Using implements ByEnum {
        SEARCH_BOX(By.name("q")),
        SEARCH_BUTTON(By.name("btnG")),
        RESULT_STATS(By.id("resultStats"));
        
        private By locator;
        
        Using(By locator) {
            this.locator = locator;
        }
        
        @Override
        public By locator() {
            return locator;
        }
    }

    public GoogleSearchPage doGoogleSearch(String searchString) {
        findElement(Using.SEARCH_BOX).sendKeys(searchString);
        findElement(Using.SEARCH_BUTTON).click();
        return new GoogleSearchPage(driver);   
    }
     
    @Override
    public boolean isLoadComplete() {
        return findOptional(Using.RESULT_STATS).hasReference();
    }
}
```

# Declaring Locator Enumerations

To make your code more maintainable and readable, we recommend the use of locator enumerations. The <span style="color:blue">Using</span> enumeration in the previous example demonstrates this pattern. This example also demonstrates implementation of the <span style="color:blue">ByEnum</span> interface. By implementing this interface, we can utilize the web element location methods (e.g. - **`findElement()`**) that accept locator constants.

# Declaring Web Application Page Path

Typically, each page-model class is associated with a single page of the application under test, located at a fixed path (e.g. - <span style="color:blue">_/c/nordstrom_</span>). It's also common for the paths of web application pages to conform with a pattern or template (e.g. - <span style="color:blue">_/s/&lt;item-description&gt;/&lt;item-identifier&gt;_</span>). **Selenium Foundation** provides a <span style="color:blue">@PageUrl</span> annotation that enables you to associate page-model classes with their corresponding paths.

An example of this annotation is found in the simple page class found above. The specified path ("/") declares that this page-model class is associated with the root of the target web application - in this case, Google. Template-based paths look like this:

###### Template-based path
```java
@PageUrl(pattern="/s/[a-z]+(-[a-z]+)*/\\d+", value="/s/wolf-blake-watch-roll/3892364")
public class ProductPage extends Page {
    ...
}
```

In this form of the <span style="color:blue">@PageUrl</span> annotation, the pattern attribute specifies a regular expression that identifies the valid format for the path, and the _value_ attribute provides an exemplar.

Once a page-model class is associated with a fixed path, the class itself is all you need for direct navigation:

###### Direct navigation to annotated page
```java
...
// perform direct navigation to exemplar product page, re-using the current window
ProductPage productPage = someOtherPage.openAnnotatedPage(ProductPage.class, false);
...
```

In this example, **Selenium Foundation** navigates to the path specified by the <span style="color:yellowgreen">**_value_**</span> attribute of the <span style="color:blue">@PageUrl</span> annotation of the <span style="color:blue">ProductPage</span> class. This is the exemplar page for this class.

# <span class="confluence-anchor-link conf-macro output-inline" id="BuildingPageObjectswithSeleniumFoundation-page-load-sync" data-macro-name="anchor" data-hasbody="false"></span>Page Transition Synchronization

Whenever a page object method returns a new page object, this informs **Selenium Foundation** that the method triggered a page transition. In response, **Selenium Foundation** performs basic synchronization - waiting for a previously-acquired web element reference to go stale. This strategy is effective for basic web applications with simple page load behavior.

For web applications with more complex page load behavior (single-page, dynamic content, etc.), the <span style="color:blue">DetectsLoadCompletion</span> interface enables implementers to provide custom page-load completion detection:

###### DetectsLoadCompletion
```java
public class OpctPage extends Page implements DetectsLoadCompletion {
  
    ...
 
    @Override
    public boolean isLoadComplete() {
        WebElement body = findElement(By.tagName("body"));
        String cursor = body.getCssValue("cursor");
        return !("wait".equals(cursor));
    }
}
```

In this example, the page is done loading when the value of the <span style="color:yellowgreen">**_cursor_**</span> CSS property of the **_<span style="color:yellowgreen">body</span>_** tag no longer equals _"<span style="color:red">wait</span>"_. This method, which is declared by the new container object, will be called every 500 mS until it returns _'true'_ or the page load timeout interval expires. Any type of container class can implement <span style="color:blue">DetectsLoadCompletion</span> - page, component, or frame.

Note that automatic page load synchronization is activated by a method that returns a <span style="color:yellowgreen">**new**</span> page object. No synchronization is performed if a method returns the page object it's standing on (i.e. - 'this'). However, the <span style="color:blue">DetectsLoadCompletion</span> interface includes a static method to invoke the same page-load synchronization logic that **Selenium Foundation** would:

###### Explicit synchronization
```java
public class SkuDescriptionSearchDialog extends Dialog {
 
    ...
 
    /**
     * Reset the search criteria.
     */
    public void resetSearch() {
        findElement(Using.RESET_BUTTON.locator).click();
        getParentPage().getWait(WaitType.PAGE_LOAD).until(DetectsLoadCompletion.pageLoadIsComplete());
    }
}
```

In this scenario, the <span style="color:blue">Dialog</span> class doesn't implement <span style="color:blue">DetectsLoadCompletion</span>, but the parent page does. The 'wait' object is acquired in the context of the parent page, so the **`pageLoadIsComplete()`** 'wait' proxy will invoke the **`isLoadComplete()`** method of the parent page class.

# Automatic Driver Targeting

For web applications that use frames or multiple windows, a major source of boilerplate code is management of the driver target. In addition to being extremely repetitive, this code is also surprisingly difficult to implement correctly. **Selenium Foundation** completely eliminates the need for explicit driver targeting. You get to focus on scenario-specific details instead of low-level plumbing. For more details, see [Selenium Foundation Test Support](https://confluence.nordstrom.net/display/MTEC/Selenium+Foundation+Test+Support#SeleniumFoundationTestSupport-auto-driver-target)

# Wrapped Element References

One of the most common sources of "noise" failures in WebDriver automation is the <span style="color:blue">StaleElementReferenceException</span>, which occurs when you try to use a web element reference that the driver no longer recognizes. Some action of the user or the application has altered the structure or content of the page, causing the browser to decide that it needs to rebuild its model of the page (the DOM), replacing all of its previous element identifiers with new ones. Even if the structure and content of the page is identical to what it was previously (e.g. - after simply refreshing the page), element references that were acquired prior to the browser rebuilding the DOM are unusable. This can be particularly vexing when you're automating a web application with dynamic content. Your automation can fail at any moment, and your code quickly becomes littered with try/catch blocks as you try to prevent these random, pointless failures.

**Selenium Foundation** saves you from this frustration by producing and using wrapped element references that automatically handle stale element reference failures. The <span style="color:blue">RobustWebElement</span> class is a fault-tolerant wrapper for native WebElement objects. It retains the locator that was used to find the reference and the context in which the search was performed, and every wrapper method includes handling for <span style="color:blue">StaleElementReferenceException</span> failures. Whenever a failure occurs, <span style="color:blue">RobustWebElement</span> uses the original locator and search context to reacquire the reference that went stale. Prior to locating a new reference for an affected element, the search context hierarchy is refreshed as needed.

*   If a new reference for the affected element is acquired, the request that triggered the <span style="color:blue">StaleElementReferenceException</span> failure will be re-issued. Your automation will continue to run, completely unaware that the entire world shifted underneath.
*   If the attempt to reacquire the element reference or refresh the search context hierarchy fails, the original exception is re-thrown. This gives you the diagnostic information about what your automation was attempting to accomplish at the point where it failed.

# Using Optional Elements

When developing models for web application interfaces, you're likely to encounter elements that only exist in specific scenarios. For example, your web application displays an error message when a required value is omitted, and the element that contains this message is dynamically created when the error is detected. <span style="color:blue">Selenium Foundation</span> provides a convenient, efficient method for handling these sorts of scenarios - the optional element:

###### Optional elements
```java
public class ItemListRulesDialog extends Dialog {
 
    ...
  
    /**
     * Get the header text for any error messages that are being shown.
     *
     * @return error message header text; 'null' if none is present
     */
    public String getErrorHeader() {
        RobustWebElement header = findOptional(Using.ERROR_HDR.locator);
        return (header.hasReference()) ? header.getText() : null;
    }
}
```

In this example, the <span style="color:blue">ERROR&#95;HDR</span> element only exists when an error has been detected. This method searches for the optional error header element, returning the error message header text if it's found or _'null'_ if it's not. Unlike the handling of conventional element search requests, in which the configuration of the "implicit wait" setting can cause the API to poll the DOM repeatedly if a specified element isn't immediately available, optional elements are always only searched for once. This means that you always get control back right away, and you don't need to handle <span style="color:blue">NoSuchElementException</span> failures.

As indicated by the example, the **`findOptional()`** method returns a <span style="color:blue">RobustWebElement</span> object. To determine if the specified element was actually found, use the **`hasReference()`** method. If the result is _'true'_, a reference has been acquired for the element (i.e. - the optional element exists).

In almost all respects, optional elements for which references have been acquired behave exactly the same as there conventional counterparts. The one scenario in which they differ is when a stale element reference can't be reacquired:

*   For conventional <span style="color:blue">RobustWebElement</span> objects, invoking any method will result in a <span style="color:blue">StaleElementReferenceException</span> failure.
*   For optional <span style="color:blue">RobustWebElement</span> objects...
    *   ... invoking most methods will result in a <span style="color:blue">NullPointerException</span> failure, caused by <span style="color:blue">NoSuchElementException</span>.
    *   ... invoking **`isDisplayed()`** or **`isEnabled()`** will return _'false'_.