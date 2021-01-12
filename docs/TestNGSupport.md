# Introduction

In addition to its JUnit 4 support, **Selenium Foundation** also includes support for **TestNG**. This support is built upon **TestNG Foundation**, which provides the framework for method interception (used for driver management), artifact capture (used to acquire screenshots and page source), and automatic retry of failed tests.

## TestNG Required Configuration

Add a service loader run watcher configuration file in the **_META-INF/services_** folder:

###### org.testng.ITestNGListener
```
com.nordstrom.automation.testng.ListenerChain
```

## TestNG Required Elements

There are several required elements that must be included in every TestNG test class to activate the features of **Selenium Foundation**. To assist you in this process, we've included the [TestNgBase](https://github.com/sbabcoc/Selenium-Foundation/tree/master/src/main/java/com/nordstrom/automation/selenium/support/TestNgBase.java) class as a starter. This class includes all of the required elements outlined below, and adds the [ScreenshotCapture](https://github.com/sbabcoc/Selenium-Foundation/tree/master/src/main/java/com/nordstrom/automation/selenium/listeners/ScreenshotCapture.java) and [PageSourceCapture](https://github.com/sbabcoc/Selenium-Foundation/tree/master/src/main/java/com/nordstrom/automation/selenium/listeners/PageSourceCapture.java) listeners.

**TestNgBase** is an abstract class that implements the [TestBase](https://github.com/sbabcoc/Selenium-Foundation/tree/master/src/main/java/com/nordstrom/automation/selenium/core/TestBase.java) interface, which provides a common abstraction for both TestNG and JUnit 4 tests.

### Outline of Required Elements

The following is an outline of the elements that must be included in every TestNG test that uses **Selenium Foundation**:

* [ListenerChain](https://github.com/sbabcoc/TestNG-Foundation/blob/master/src/main/java/com/nordstrom/automation/testng/ListenerChain.java):  
**ListenerChain** is a TestNG listener that enables you to add other listeners at runtime and guarantees the order in which they're invoked. This is similar in behavior to a JUnit 4 rule chain. This facility is activated by the service loader configuration specified [above](#testng-required-configuration).
* The [@LinkedListeners](https://github.com/sbabcoc/TestNG-Foundation/blob/master/src/main/java/com/nordstrom/automation/testng/LinkedListeners.java) annotation:  
 To attach listeners to an active **ListenerChain**, mark your test class with the **`@LinkedListeners`** annotation. The [TestNgBase](https://github.com/sbabcoc/Selenium-Foundation/tree/master/src/main/java/com/nordstrom/automation/selenium/support/TestNgBase.java) class is marked with a **`@LinkedListeners`** annotation that specifies four listeners that manage several core features of **Selenium Foundation**:
  * [DriverListener](https://github.com/sbabcoc/Selenium-Foundation/tree/master/src/main/java/com/nordstrom/automation/selenium/listeners/DriverListener.java):  
  **DriverListener** is a TestNG listener that manages driver sessions and local Selenium Grid servers.
  * [ExecutionFlowController](https://github.com/sbabcoc/TestNG-Foundation/blob/master/src/main/java/com/nordstrom/automation/testng/ExecutionFlowController.java):  
  **ExecutionFlowController** is a TestNG listener that propagates test context attributes:  
  [_before_ method] → [test method] → [_after_ method]
  * [ScreenshotCapture](https://github.com/sbabcoc/Selenium-Foundation/tree/master/src/main/java/com/nordstrom/automation/selenium/listeners/ScreenshotCapture.java):  
  **ScreenshotCapture** is a TestNG listener that automatically captures screenshots in the event of test failures. Tests are also able to request on-demand screenshot capture through the `captureArtifact(ITestResult)` method.
  * [PageSourceCapture](https://github.com/sbabcoc/Selenium-Foundation/tree/master/src/main/java/com/nordstrom/automation/selenium/listeners/PageSourceCapture.java):  
  **PageSourceCapture** is a TestNG listener that automatically captures page source in the event of test failures. Tests are also able to request on-demand page source capture through the `captureArtifact(ITestResult)` method.

## Automatic Retry of Failed Tests

**Selenium Foundation** includes a context-specific extension of the [RetryManager](https://github.com/sbabcoc/TestNG-Foundation/blob/master/src/main/java/com/nordstrom/automation/testng/RetryManager.java) analyzer of [TestNG Foundation](https://github.com/sbabcoc/TestNG-Foundation). This retry analyzer considers any test that fails due to a **WebDriverException** to be retriable. By default, this retry analyzer is disabled. To enable automatic retry of **WebDriverException** failures, specify a positive value for the **MAX_RETRY** setting of **TestNG Foundation**:

| _testng.properties_ |
| --- |
| testng.max.retry=2 |

The base class for this retry analyzer enables you to add your own analyzers through the **ServiceLoader**. You can also entirely replace this analyzer with your own. See the [TestNG Foundation](https://github.com/sbabcoc/TestNG-Foundation#attaching-retry-analyzers-via-retrymanager) documentation for more details.

## Demonstrated Features

The **QuickStart** class demonstrates several important **Selenium Foundation** features:

* [InitialPage](https://github.com/sbabcoc/Selenium-Foundation/tree/master/src/main/java/com/nordstrom/automation/selenium/annotations/InitialPage.java):  
**InitialPage** is a Java annotation that enables you to specify the initial page class and/or URL that should be loaded at the start of the test method. This can be applied to each test individually, or it can be applied at the class level to specify the default page for all tests in the class. It can also be applied to **`@Before...`** configuration methods to provide driver sessions opened to the desired page.
* [SeleniumConfig](https://github.com/sbabcoc/Selenium-Foundation/tree/master/src/main/java/com/nordstrom/automation/selenium/SeleniumConfig.java):  
**SeleniumConfig** declares settings and methods related to Selenium WebDriver and Grid configuration. This class is built on the **Settings API**, composed of defaults, stored values, and System properties.
* [SeleniumSettings](https://github.com/sbabcoc/Selenium-Foundation/tree/master/src/main/java/com/nordstrom/automation/selenium/SeleniumConfig.java):  
**SeleniumSettings** declares the constants, property names, and default values for the settings managed by SeleniumConfig. Defaults can be overridden via System properties or the _settings.propeties_ file in your user "home" directory. See **ESSENTIAL SETTINGS** below for more details.
* [ReporterAppender](https://github.com/sbabcoc/logback-testng/blob/master/src/main/java/com/github/sbabcoc/logback/testng/ReporterAppender.java):  
**ReporterAppender** is a **Logback** appender for TestNG Reporter. The **Selenium Foundation** project ships with a _logback.xml_ file that attaches this appender. See the complete **logback-testng** information page [here](https://github.com/sbabcoc/logback-testng).
* **`TestBase.optionalOf(Object)`**:  
This static utility method wraps the specified object in an [Optional](https://google.github.io/guava/releases/19.0/api/docs/com/google/common/base/Optional.html) object. If the object to be wrapped is 'null', this method returns an empty optional.

## Driver Acquisition and Hand-Off

In the preceding section, driver sessions are acquired automatically for each test or requested implicitly by applying the **`@InitialPage`** annotation. The core functionality used to initiate driver sessions implicitly can also be invoked ad hoc to acquire drivers explicitly:

```java
WebDriver driver = GridUtility.getDriver();
```

This method uses the configured settings for Selenium Grid and desired browser from the current test execution context to instantiate a new driver session.

If the **`@InitialPage`** annotation is applied to a **`@BeforeMethod`** configuration method, the driver instantiated for this method is automatically handed off to the test that follows. The initial page as specified for the configuration method is handed off as well. If actions performed by your configuration method trigger page transitions, you need to store the final page accessed by the configuration method as the initial page for the test method:

```java
@BeforeMethod
@InitialPage(LoginPage.class)
public void logInBeforeTest() {
    LoginPage loginPage = getInitialPage();
    MainMenuPage mainMenuPage = loginPage.logInAs(USER.StandardUser);
    // update initial page for test method
    setInitialPage(mainMenuPage);
}

@Test
public void testMenuFeatures() {
    MainMenuPage mainMenuPage = getInitialPage();
    ...
}

```

> Written with [StackEdit](https://stackedit.io/).