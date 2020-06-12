# Introduction

In conjunction with automatic landing page verification, **Selenium Foundation** invokes registered custom _transition error detectors_. Implement the [TransitionErrorDetector](https://github.com/Nordstrom/Selenium-Foundation/blob/master/src/main/java/com/nordstrom/automation/selenium/interfaces/TransitionErrorDetector.java) interface, then register your detectors in the corresponding service loader configuration file (**META-INF/services/com.nordstrom.automation.selenium.interfaces.TransitionErrorDetector**).

Examples of the sorts of conditions you may want to detect include error pages (e.g. - page not found) or non-context error messages (e.g. - communication issues, access token timeout). For recoverable conditions, error detectors can also server as error handler. For example, you could implement a detector that automatically logs back in if your test encounters an access timeout.

**Selenium Foundation** calls your registered transition error detectors in three scenarios:

* While waiting for a new browser window to appear when transitioning to a landing page that opens in a new window
* While waiting for a container (i.e. - page or component) that implements completion reporting to finish loading
* While waiting for a reference element to go stale when transitioning to a landing page that doesn't implement load completion reporting

###### Detector for '404 - Page Not Found'
```java
package com.example;

import org.openqa.selenium.By;
import com.nordstrom.automation.selenium.core.ByType.ByEnum;
import com.nordstrom.automation.selenium.interfaces.TransitionErrorDetector;
import com.nordstrom.automation.selenium.model.ComponentContainer;
import com.nordstrom.automation.selenium.model.RobustWebElement;

public class PageNotFoundDetector implements TransitionErrorDetector {

    private static final String MESSAGE = "Page not found: %s";

    private enum Using implements ByEnum {
        IMAGE_404(By.cssSelector("img[alt^='404']"));
        
        private By locator;
        
        Using(By locator) {
            this.locator = locator;
        }
        
        @Override
        public By locator() {
            return locator;
        }
    }

    @Override
    public String scanForErrors(ComponentContainer context) {
        RobustWebElement errorCheck = context.findOptional(Using.IMAGE_404);
        return errorCheck.hasReference() ? String.format(MESSAGE, context.getDriver().getCurrentUrl()) : null;
    }
}
```

In the preceding example, the error detection code searches for an image tag with alternate test that begins with "404". (This detector would identify the current "page not found" page of GitHub itself.) Note the use of the "optional element" feature of **Selenium Foundation**, which determines if the indicated element can be found immediately. Even if the target session is configured with non-zero [implicit wait](https://www.selenium.dev/documentation/en/webdriver/waits/#implicit-wait) interval, the `findOptional()` call will return a result after a single scan of the search context. To determine if the optional element was actually found, invoke the `hasReference()` method.

###### Detector for Non-Context Error Message
```java
package com.example;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import com.nordstrom.automation.selenium.core.ByType.ByEnum;
import com.nordstrom.automation.selenium.core.WebDriverUtils;
import com.nordstrom.automation.selenium.interfaces.TransitionErrorDetector;
import com.nordstrom.automation.selenium.model.ComponentContainer;

public class ErrorMessageDetector implements TransitionErrorDetector {

    private enum Using implements ByEnum {
        ALERT_CONTENT(By.cssSelector("div.a-alert-content"));
        
        private By locator;
        
        Using(By locator) {
            this.locator = locator;
        }
        
        @Override
        public By locator() {
            return locator;
        }
    }

    @Override
    public String scanForErrors(ComponentContainer context) {
        List<WebElement> alerts = context.findElements(Using.ALERT_CONTENT);
        if (WebDriverUtils.filterHidden(alerts)) {
            return null;
        }
        StringBuilder errors = new StringBuilder();
        for (WebElement alert : alerts) {
            errors.append(alert.getText()).append('\n');
        }
        return errors.toString();
    }
}
```

In the preceding example, the error detection code determines if any elements matching the specified locator are visible. If any visible alerts are found, the messages they contain are collected and returned. Note that this code assumes at least one matching element will be found (normally hidden). If no matching elements exist and the target session is configured with a non-zero implicit wait interval, landing page verification will be blocked until the wait interval expires.

###### Detector to Handle Access Timeout
```java
package com.example;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import com.nordstrom.automation.selenium.core.ByType.ByEnum;
import com.nordstrom.automation.selenium.core.WebDriverUtils;
import com.nordstrom.automation.selenium.interfaces.TransitionErrorDetector;
import com.nordstrom.automation.selenium.model.ComponentContainer;
import com.nordstrom.automation.selenium.model.RobustWebElement;

public class ErrorMessageDetector implements TransitionErrorDetector {

    private enum Using implements ByEnum {
        POPUP_DIALOG(By.cssSelector("div.popup-dialog"));
        
        private By locator;
        
        Using(By locator) {
            this.locator = locator;
        }
        
        @Override
        public By locator() {
            return locator;
        }
    }

    @Override
    public String scanForErrors(ComponentContainer context) {
        List<WebElement> dialogs = context.findElements(Using.POPUP_DIALOG);
        if (WebDriverUtils.filterHidden(dialogs)) {
            return null;
        }
        for (WebElement dialog : dialogs) {
            
        }
    }
}
```
