# Background

## WebDriver API

**WebDriver** is a tool for automating web application tests. It aims to provide a friendly API that's easy to explore and understand, which helps to make tests easier to read and maintain. **WebDriver** provides a broad range of low-level classes and methods for interacting with web applications - locating elements, populating field, clicking buttons, examining text. What it lacks is fundamental building block for modeling the structures and behaviors of web applications. Consequently, automated tests that rely solely on the **WebDriver** API are likely to repeat the same sequences of steps and will be plagued by failures rooted in fundamental deficiencies of the API.

## Page Object Design Pattern

A popular strategy to reduce duplication and accelerate development of automated web application tests is the **Page Object Design Pattern**. In this pattern, each web application page that automated tests will interact with is modeled as a class, with methods to access the functionality and content of the page. Instead of accessing the browser directly through the **WebDriver** API, tests interact with the web application through page objects. The benefit is that tests are isolated from changes to the structure, behavior, or content of pages with which they interact. Revisions needed to support such changes are typically contained within the page model implementation.

### Design Pattern References

* [Getting started (including complete examples) from Kim Schiller](https://www.pluralsight.com/guides/getting-started-with-page-object-pattern-for-your-selenium-tests)
* [Pattern overview from Martin Fowler](https://martinfowler.com/bliki/PageObject.html)
* [Pattern overview from SeleniumHQ](https://github.com/SeleniumHQ/selenium/wiki/PageObjects)
* [In-depth pattern description from Guru99](https://www.guru99.com/page-object-model-pom-page-factory-in-selenium-ultimate-guide.html)

### Page Object Pitfalls

While the page object design pattern dramatically reduces the amount of boilerplate code in test methods, much of this ends up in the page object implementations. **Selenium Foundation** eliminates all of the common code that you'll find in typical page object implementations.

The core **WebDriver** API includes two features designed for page model implementations - the <span style="color:blue">PageFactory</span> class and the <span style="color:blue">@FindBy</span> annotation. We discourage the use of these features, because these rely on "magic" behavior - replacing <span style="color:blue">WebElement</span> property references in your code with hidden **`findElement()`** method calls. These obscure the interactions of your page model with the driver session, and they can trigger unexpected <span style="color:blue">NoSuchElementException</span> and <span style="color:blue">StaleElementReferenceException</span> failures.

**Selenium Foundation** doesn't support the <span style="color:blue">PageFactory</span> approach, but facilitates the use of locator enumeration through the <span style="color:blue">ByEnum</span> interface.

# WebDriver Automation with Selenium Foundation

## Consistent Support for Local and Remote Configurations

In typical **WebDriver** API projects, the process for acquiring driver instances for interacting with local machine browser sessions is completely different from how remote sessions are acquired. This introduces divergent behavior that can result in failures during remote operation that don't manifest locally. Local browser support is hard-coded into the project implementation, so the process of adding support for additional browsers involves revising the code that provides local driver instances. This is intrusive, exposing existing clients to the risk of breakage and forcing clients who need the newly added browsers to wait for the next release.

Another disadvantage to this approach is the difficulty it creates in limiting the number of sessions that are created when running suites of automated tests in parallel. The level of control you get is typically restricted to specifying a maximum number of threads that will be spawned, which doesn't have a strict one-to-one correlation to the number of tests that will be executed at any given moment. Consequently, you're forced to throttle the thread count to prevent the system from becoming over-taxed by running too many concurrent sessions, which can trigger timeout failures and other erratic behavior. The need to specify a conservative thread count ceiling typically results in significant under-utilization of system resources, limiting throughput.

**Selenium Foundation** takes a different approach, using a local Selenium Grid instance to provide browser sessions. This strategy offers several benefits:

*   The set of available browser types is determined by configuration, not implementation. To add another browser, install the driver and update the Grid configuration.
*   Local browser sessions are identical to remote sessions. This eliminates divergent behavior that could otherwise lead to unexpected failures in remote automation.
*   Maximum concurrent session count is explicitly specified in the Grid configuration, enabling you to precisely fine-tune system utilization to maximize throughput. 

## Selenium Foundation Test Support

To eliminate unnecessary boilerplate code, **Selenium Foundation** performs many common set-up and tear-down operations automatically.

*   By default, each test has a browser session created for it automatically. For scenarios with unique requirements, this behavior can be overridden with a simple annotation.
*   Test classes and test methods can declare the initial page class that should be loaded prior to the start of the test.
*   Prior to invoking methods of page objects and page components (see below), the driver is automatically targeted at the associated window or frame.
*   Facilities are provided to automatically synchronize with the page load completion at transitions (more later).
*   At the end of each test, the browser session is closed automatically.
*   If a test fails, a screenshot and page source are automatically saved for diagnostic purposes.
*   If a test fails with a retriable exception (any subclass of WebDriverException) and automatic retry is enabled, the test is executed again.

## Building Page Objects with Selenium Foundation

**Selenium Foundation** provides functional building blocks for implementing page objects that eliminate unnecessary boilerplate code.

*   Page classes can declare the path at which their associated web application page is found. This enables automatic landing page verification and class-based direct navigation.
*   Out of the box, you get basic page transition synchronization. 
*   For scenarios with more intricate behavior (e.g. - single-page applications), a facility is provided for page models to define custom page load completion logic.
*   Each page object is associated with the browser window for which it was created, and driver focus is managed automatically. With this support, testing multi-window applications is greatly simplified.
*   Element references returned by the foundation API are wrapped to provide automatic recovery from <span style="color:blue">_stale element reference exceptions_</span>. This makes it easy to test applications with dynamic content.
*   Efficient support is provided for optional elements - controls and content that are present in some scenarios and absent in others.

### Structuring Page Models with Components

**Selenium Foundation** facilitates the implementation of well-structured page models through the use of <span style="color:blue">_page components_</span>, which enable you to model groups of functionally related elements as discrete objects. Page components are provided with a broad range of basic facilities:

*   The search context for a component can be either a standard element or a frame.
*   For frame-based components, driver focus is managed automatically. 
*   Components can be nested and aggregated to create models that accurately reflect the conceptual structure of the pages being modeled.
*   For grouping of elements that are repeated (e.g. - item summary tiles on a search results page), components can be collected into indexed lists and keyed maps.
