# INTRODUCTION

**Selenium Foundation** is an automation framework designed to extend and enhance the capabilities provided by **Selenium 2.0** (_WebDriver_). The last stable release of Selenium 2.0 is **2.53.1**.

The [QuickStart](https://git.nordstrom.net/projects/MFATT/repos/selenium-foundation/browse/src/test/java/com/nordstrom/automation/selenium/QuickStart.java) class provides a fully-functional example of a test class built around **Selenium Foundation**, **TestNG Foundation**, and the **Settings API**. It demonstrates how to set up required elements and introduces several key features that you're likely to use on a regular basis. 

## TESTNG REQUIRED ELEMENTS

There are several required elements that must be included in every TestNG test class to activate the features of **Selenium Foundation**. To assist you in this process, we've included the [TestNgBase](https://git.nordstrom.net/projects/MFATT/repos/selenium-foundation/browse/src/main/java/com/nordstrom/automation/selenium/support/TestNgBase.java) class as a starter. This class includes all of the required elements outlined below, and adds the [ScreenshotCapture](https://git.nordstrom.net/projects/MFATT/repos/selenium-foundation/browse/src/main/java/com/nordstrom/automation/selenium/listeners/ScreenshotCapture.java) listener.

**TestNgBase** is an abstract class that implements the [TestBase](https://git.nordstrom.net/projects/MFATT/repos/selenium-foundation/browse/src/main/java/com/nordstrom/automation/selenium/core/TestBase.java) interface, which provides a common abstraction for both TestNG and JUnit tests.

### Outline of Required Elements

The following is an outline of the elements that must be included in every TestNG test that uses **Selenium Foundation**:

* [ListenerChain](https://git.nordstrom.net/projects/MFATT/repos/testng-foundation/browse/src/main/java/com/nordstrom/automation/testng/ListenerChain.java):  
**ListenerChain** is a TestNG listener that enables you to add other listeners at runtime and guarantees the order in which they're invoked. This is similar in behavior to a JUnit rule chain.
* The [@LinkedListeners](https://git.nordstrom.net/projects/MFATT/repos/testng-foundation/browse/src/main/java/com/nordstrom/automation/testng/LinkedListeners.java) annotation:  
 To attach listeners to an active **ListenerChain**, mark your test class with the **`@LinkedListeners`** annotation. The [TestNgBase](https://git.nordstrom.net/projects/MFATT/repos/selenium-foundation/browse/src/main/java/com/nordstrom/automation/selenium/support/TestNgBase.java) class is marked with a **`@LinkedListeners`** annotation that specifies three listeners that manage several core features of **Selenium Foundation**:
  * [ScreenshotCapture](https://git.nordstrom.net/projects/MFATT/repos/selenium-foundation/browse/src/main/java/com/nordstrom/automation/selenium/listeners/ScreenshotCapture.java):  
**ScreenshotCapture** is a TestNG listener that automatically captures screenshots in the event of test failures. Tests are also able to request on-demand screenshot capture through the `captureArtifact(ITestResult)` method.
  * [DriverListener](https://git.nordstrom.net/projects/MFATT/repos/selenium-foundation/browse/src/main/java/com/nordstrom/automation/selenium/listeners/DriverListener.java):  
**DriverListener** is a TestNG listener that manages driver sessions and local Selenium Grid servers.
  * [ExecutionFlowController](https://git.nordstrom.net/projects/MFATT/repos/testng-foundation/browse/src/main/java/com/nordstrom/automation/testng/ExecutionFlowController.java):  
**ExecutionFlowController** is a TestNG listener that propagates test context attributes:  
 [_before_ method] → [test method] → [_after_ method]
  * Note that the **`@LinkedListeners`** annotation declared by **TestNgBase** also includes the **ScreenshotCapture** listener, which automatically captures a screenshot in the event of test failure.
 
The **`@LinkedListeners`** annotation is processed by the **ListenerChain**, which must be activated so that the other core listeners will be connected and functioning correctly. Although you can use the standard TestNG **`@Listeners`** annotation to activate **ListenerChain**, we recommend that you use the **ServiceLoader** mechanism for this purpose in your actual project. This is the technique employed by the test suite of the **Selenium Foundation** project. For details, see [Selenium Foundation Test Support](docs/SeleniumFoundationTestSupport.md).

## DEMONSTRATED FEATURES

The **QuickStart** class demonstrates several important **Selenium Foundation** features:

* [InitialPage](https://git.nordstrom.net/projects/MFATT/repos/selenium-foundation/browse/src/main/java/com/nordstrom/automation/selenium/annotations/InitialPage.java):  
**InitialPage** is a Java annotation that enables you to specify the initial page class and/or URL that should be loaded at the start of the test method. This can be applied to each test individually, or it can be applied at the class level to specify the default page for all tests in the class. It can also be applied to **`@Before...`** configuration methods to provide driver sessions opened to the desired page.
* [SeleniumConfig](https://git.nordstrom.net/projects/MFATT/repos/selenium-foundation/browse/src/main/java/com/nordstrom/automation/selenium/SeleniumConfig.java):  
**SeleniumConfig** declares settings and methods related to Selenium WebDriver and Grid configuration. This class is built on the **Settings API**, composed of defaults, stored values, and System properties.
* [SeleniumSettings](https://git.nordstrom.net/projects/MFATT/repos/selenium-foundation/browse/src/main/java/com/nordstrom/automation/selenium/SeleniumConfig.java):  
**SeleniumSettings** declares the constants, property names, and default values for the settings managed by SeleniumConfig. Defaults can be overridden via System properties or the _settings.propeties_ file in your user "home" directory. See **ESSENTIAL SETTINGS** below for more details.
* [ReporterAppender](https://github.com/sbabcoc/logback-testng/blob/master/src/main/java/com/github/sbabcoc/logback/testng/ReporterAppender.java):  
**ReporterAppender** is a **Logback** appender for TestNG Reporter. The **Selenium Foundation** project ships with a _logback.xml_ file that attaches this appender. See the complete **logback-testng** information page [here](https://github.com/sbabcoc/logback-testng).
* **`TestBase.optionalOf(Object)`**:  
This static utility method wraps the specified object in an [Optional](https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html) object. If the object to be wrapped is 'null', this method returns an empty optional.

## DRIVER ACQUISITION AND HAND-OFF

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
    LoginPage loginPage = (LoginPage) getInitialPage();
    MainMenuPage mainMenuPage = loginPage.logInAs(USER.StandardUser);
    // update initial page for test method
    setInitialPage(mainMenuPage);
}

@Test
public void testMenuFeatures() {
    MainMenuPage mainMenuPage = (MainMenuPage) getInitialPage();
    ...
}

```

## ESSENTIAL SETTINGS 

You'll probably find that the defaults assigned to most settings will suffice in most basic scenarios. However, it's likely that you'll need to override one or more of the following. The **Property Name** column indicates the name of the System property associated with the setting. To override a setting, you can either add a line for the setting to your _settings.properties_ file or define a System property. 

<table style="text-align: left; border: 1px solid black; border-collapse: collapse;">
    <tr>
        <th style="text-align: left; border: 1px solid black;">Constant</th>
        <th style="text-align: left; border: 1px solid black;">Property Name</th>
        <th style="text-align: center; border: 1px solid black;">Default</th>
    </tr>
    <tr>
        <td style="text-align: left; border: 1px solid black;">BROWSER_NAME</td>
        <td style="text-align: left; border: 1px solid black;">selenium.browser.name</td>
        <td style="text-align: center; border: 1px solid black;">(none) *</td>
    </tr>
    <tr>
        <td style="text-align: left; border: 1px solid black;">TARGET_HOST</td>
        <td style="text-align: left; border: 1px solid black;">selenium.target.host</td>
        <td style="text-align: center; border: 1px solid black;">localhost</td>
    </tr>
    <tr>
        <td style="text-align: left; border: 1px solid black;">TARGET_PATH</td>
        <td style="text-align: left; border: 1px solid black;">selenium.target.path</td>
        <td style="text-align: center; border: 1px solid black;">/</td>
    </tr>
</table>

\* NOTE: By default, PhantomJS is selected as the browser. For easier override, this is specified through **BROWSER_CAPS** instead of **BROWSER_NAME**. For details, see [Manipulate Settings with SeleniumConfig](docs/ManipulatingSettingsWithSeleniumConfig.md). 

## OVERRIDING DEFAULTS 

**SeleniumConfig** searches a series of locations for a _settings.properties_ file. This file will typically be stored in your user "home" folder. Any settings declared in this file will override the defaults assigned in the **SeleniumSettings** enumeration. Settings that are declared as System properties will override both the defaults assigned by **SeleniumSettings** and settings declared in _settings.properties_. For example: 

<table style="text-align: left; border: 1px solid black; border-collapse: collapse;">
    <tr style="text-align: left; border: 1px solid black;">
        <th><i>settings.properties</i></th>
    </tr>
    <tr>
        <td>selenium.target.host=my.server.com</td>
    </tr>
    <tr>
        <td>selenium.browser.name=chrome</td>
    </tr>
</table>

This sample _settings.properties_ file overrides the values of **TARGET_HOST** and **BROWSER_NAME**. The latter can be overridden by System property declaration: 

> `-Dselenium.browser.name=firefox`

The hierarchy of evaluation produces the following results: 

> **BROWSER_NAME** = <mark>firefox</mark>; **TARGET_HOST** = <mark>my.server.com</mark>; **TARGET_PATH** = <mark>/</mark> 

## INSTALLING DRIVERS 

Whichever browser you choose to run your automation on, you need to make sure to install the latest driver for that browser compatible with **Selenium WebDriver 2.53.1**, along with a compatible release of the browser itself. We recommend that you install the drivers and browsers on the file search path to avoid the need to provide additional configuration details via scenario-specific means. 

Here are the official homes for several of the major drivers: 

* GhostDriver (PhantomJS) - [http://phantomjs.org/download.html](http://phantomjs.org/download.html)
* ChromeDriver - [https://sites.google.com/a/chromium.org/chromedriver/downloads](https://sites.google.com/a/chromium.org/chromedriver/downloads)
* IEDriver - [http://selenium-release.storage.googleapis.com/index.html?path=2.53/](http://selenium-release.storage.googleapis.com/index.html?path=2.53/)

**NOTE**: GhostDriver and ChromeDriver are simple binary installations, but several system configuration changes must be applied for IEDriver to work properly. For details, visit the InternetExplorerDriver project Wiki on GitHub and follow the [Required Configuration](https://github.com/SeleniumHQ/selenium/wiki/InternetExplorerDriver#required-configuration) procedure.

## JUNIT SUPPORT

In addition to the TestNG support documented above, **Selenium Foundation** also includes support for JUnit. This support is built upon **JUnit Foundation**, which provides the framework for method interception and artifact capture used for driver management and screenshots.

### JUnit Required Elements

There are several required elements that must be included in every JUnit test class to activate the features of **Selenium Foundation**. To assist you in this process, we've included the [JUnitBase](https://git.nordstrom.net/projects/MFATT/repos/selenium-foundation/browse/src/main/java/com/nordstrom/automation/selenium/junit/JUnitBase.java) class as a starter. This class includes all of the required elements outlined below.

**JUnitBase** is an abstract class that implements the [TestBase](https://git.nordstrom.net/projects/MFATT/repos/selenium-foundation/browse/src/main/java/com/nordstrom/automation/selenium/core/TestBase.java) interface, which provides a common abstraction for both TestNG and JUnit tests.

### Outline of Required Elements

The following is an outline of the elements that must be included in every JUnit test that uses **Selenium Foundation**:

* [HookInstallingRunner](https://git.nordstrom.net/projects/MFATT/repos/junit-foundation/browse/src/main/java/com/nordstrom/automation/junit/HookInstallingRunner.java):  
**HookInstallingRunner** is a JUnit test runneruses bytecode enhancement to install hooks on test and configuration methods to enable method pre-processing and post-processing. This closely resembles the **IInvokedMethodListener** feature of TestNG.
* [RuleChain](http://junit.org/junit4/javadoc/latest/org/junit/rules/RuleChain.html):  
 Use **RuleChain** for attaching test rules that must be applied in a specific order. The [JUnitBase](https://git.nordstrom.net/projects/MFATT/repos/selenium-foundation/browse/src/main/java/com/nordstrom/automation/selenium/junit/JUnitBase.java) class includes a **RuleChain** that specifies two watchers that manage core features of **Selenium Foundation**:
  * [DriverWatcher](https://git.nordstrom.net/projects/MFATT/repos/selenium-foundation/browse/src/main/java/com/nordstrom/automation/selenium/junit/DriverWatcher.java):  
**DriverWatcher** is a JUnit test watcher that manages driver sessions and local Selenium Grid servers.
  * [ScreenshotCapture](https://git.nordstrom.net/projects/MFATT/repos/selenium-foundation/browse/src/main/java/com/nordstrom/automation/selenium/junit/ScreenshotCapture.java):  
  The **RuleChain** declared by **JUnitBase** also includes the **ScreenshotCapture** watcher, which automatically captures a screenshot in the event of test failure.

### DEMONSTRATED FEATURES

The **JUnitBase** class demonstrates several features of the **Selenium Foundation** API:

* **`TestBase.optionalOf(Object)`**:  
This static utility method wraps the specified object in an [Optional](https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html) object. If the object to be wrapped is 'null', this method returns an empty optional.
* **`RuleChainWalker.getAttachedRule(RuleChain, Class<T extends TestRule>)`**:  
This static utility method gets reference to an instance of the specified test rule type on the supplied rule chain. In **JUnitBase**, this is use to acquire a reference to the **ScreenshotCapture** watcher for capturing on-demand screenshot artifacts.
* **`ScreenshotCapture.getDescription()`**:  
This instance method of **ScreenshotCapture** enables test code to acquire the [Description](http://junit.org/junit4/javadoc/latest/org/junit/runner/Description.html) object for the current JUnit test method. This object can be interrogated for many useful propeties of the test method, including method name, attached annotations, and containing class.

## FEATURE PARITY

All of the feature of **Selenium Foundation** are available regardless of which testing framework you choose - either TestNG or JUnit. Once the initial configuration is done, the abstraction provided by the **TestBase** interface enables your code to be almost entirely framework-agnostic. This is clearly demonstrated in [ModelTestCore](https://git.nordstrom.net/projects/MFATT/repos/selenium-foundation/browse/src/test/java/com/nordstrom/automation/selenium/core/ModelTestCore.java), which contains the implementations for a collection of tests that are invoked from both TestNG (via [ModelTest](https://git.nordstrom.net/projects/MFATT/repos/selenium-foundation/browse/src/test/java/com/nordstrom/automation/selenium/model/ModelTest.java)) and JUnit (via [JUnitModelTest](https://git.nordstrom.net/projects/MFATT/repos/selenium-foundation/browse/src/test/java/com/nordstrom/automation/selenium/junit/JUnitModelTest.java)).