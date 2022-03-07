[![Maven Central](https://img.shields.io/maven-central/v/com.nordstrom.ui-tools/selenium-foundation.svg)](https://search.maven.org/search?q=g:com.nordstrom.ui-tools%20AND%20a:selenium-foundation&core=gav)

# INTRODUCTION
**Selenium Foundation** is an automation framework designed to extend and enhance the capabilities provided by **Selenium** (_WebDriver_).

#### Table of Contents

* [Selenium API Support](#selenium-api-support)
* [Dependency Artifact Coordinates](#dependency-artifact-coordinates)
* [Building Selenium Foundation](#building-selenium-foundation)
  * [Building in Eclipse](#building-in-eclipse)
  * [Building from Command Line](#building-from-command-line)
  * [Maven Support](#maven-support)
* [Getting Started](#getting-started)
  * [Required Configuration](#required-configuration)
* [Highlights](#highlights)
  * [Support for Appium Automation Engines](#support-for-appium-automation-engines)
  * [Automatic Driver Targeting](#automatic-driver-targeting)
  * [Automatic Targeted Session Configuration](#automatic-targeted-session-configuration)
  * [Support for Frame-Based Components and Shadow-DOM Hierarchies](#support-for-frame-based-components-and-shadow-dom-hierarchies)
  * [Landing Page Verification and Model-Directed Navigation](#landing-page-verification-and-model-directed-navigation)
  * [Customizable Transition Error Detection](#customizable-transition-error-detection)
  * [Component Collection Classes](#component-collection-classes)
  * [Automatic Stale Element Reference Protection](#automatic-stale-element-reference-protection)
  * [Optional Elements](#optional-elements)
  * [Page-Load Synchronization](#page-load-synchronization)
  * [Grid-Based Driver Creation](#grid-based-driver-creation)
  * [Automatic Phase-to-Phase Driver Hand-Off](#automatic-phase-to-phase-driver-hand-off)
  * [Automatic Capture of Screenshots and Page Source](#automatic-capture-of-screenshots-and-page-source)
  * [Support for TestNG and JUnit 4](#support-for-testng-and-junit-4)
  * [Essential Settings](#essential-settings)
  * [Overriding Defaults](#overriding-defaults)
  * [Installing Drivers](#installing-drivers)

#### In-Depth Documentation

* [Welcome to Selenium Foundation](docs/WelcomeToSeleniumFoundation.md#welcome)
* [Development Environment](docs/DevelopmentEnvironment.md#introduction)
* [Configuring Project Settings](docs/ConfiguringProjectSettings.md#introduction)
* [Local Grid Configuration](docs/LocalGridConfiguration.md#introduction)
* [Customizing Capabilities](docs/CustomizingCapabilities.md#introduction)
* [TestNG Support](docs/TestNGSupport.md#introduction)
* [JUnit 4 Support](docs/JUnit4Support.md#introduction)
* [Target Platform Feature](docs/TargetPlatformFeature.md#introduction)
* [Building Page Objects](docs/BuildingPageObjects.md#introduction)
* [Page Components](docs/PageComponents.md#introduction)
* [Transition Error Detection](docs/TransitionErrorDetection.md#introduction)
* [JavaScript Enhancements](docs/JavaScriptEnhancements.md#the-basics-what-webdriver-provides)

## Selenium API Support

**Selenium Foundation** includes support for both **Selenium 2** and **Selenium 3**. This project has transitioned from Maven to Gradle so that both configurations can be handled by a single [project definition file](build.gradle).

### Dependency Artifact Coordinates

The Maven group ID is `com.nordstrom.ui-tools`, and the artifact ID is `selenium-foundation`. Artifacts whose versions have the `s2` suffix support the **Selenium 2** API. Artifacts whose versions have the `s3` suffix support the **Selenium 3** API.

To add a dependency on **Selenium Foundation** for Maven, use the following:

| Selenium 2 | Selenium 3 |
|:---|:---|
| <pre>&lt;dependency&gt;<br/>&nbsp;&nbsp;&lt;groupId&gt;com.nordstrom.ui-tools&lt;/groupId&gt;<br/>&nbsp;&nbsp;&lt;artifactId&gt;selenium-foundation&lt;/artifactId&gt;<br/>&nbsp;&nbsp;&lt;version&gt;22.4.7-s2&lt;/version&gt;<br/>&lt;/dependency&gt;</pre> | <pre>&lt;dependency&gt;<br/>&nbsp;&nbsp;&lt;groupId&gt;com.nordstrom.ui-tools&lt;/groupId&gt;<br/>&nbsp;&nbsp;&lt;artifactId&gt;selenium-foundation&lt;/artifactId&gt;<br/>&nbsp;&nbsp;&lt;version&gt;22.4.7-s3&lt;/version&gt;<br/>&lt;/dependency&gt;</pre> |

To add a dependency for Gradle:

| Platform | Dependency |
|:---:|:---|
| **Selenium 2** | <pre>dependencies {<br/>&nbsp;&nbsp;compile 'com.nordstrom.ui-tools:selenium-foundation:22.4.7-s2'<br/>}</pre> |
| **Selenium 3** | <pre>dependencies {<br/>&nbsp;&nbsp;compile 'com.nordstrom.ui-tools:selenium-foundation:22.4.7-s3'<br/>}</pre> |

### Building Selenium Foundation

In order to build **Selenium Foundation**, start by setting up your [development environment](docs/DevelopmentEnvironment.md#introduction).

#### Building in Eclipse

For Eclipse, we recommend enabling Gradle integration through the official [BuildShip](https://marketplace.eclipse.org/content/buildship-gradle-integration) plug-in. By default, this project will build its **Selenium 3** configuration. To build the **Selenium 2** configuration, add this program argument to a run configuration that includes the `clean` and `build` tasks:

> `-Pprofile=selenium2`

[Learn more...](docs/DevelopmentEnvironment.md#gradle-configuration)

#### Building from Command Line

Both configurations of **Selenium Foundation** can by built from the command line. 

> **`gradle build`** `# build Selenium 3 configuration`  
> **`gradle build -Pprofile=selenium2`** `# build Selenium 2 configuration`  

Use the `install` task to install SNAPSHOT builds in your local Maven repository:

> **`gradle install`** `# build Selenium 3 configuration`  
> **`gradle install -Pprofile=selenium2`** `# build Selenium 2 configuration`  

#### Maven Support

Although Gradle is used to produce official releases, **Selenium Foundation** also includes a fully-functional Maven POM file.

> **`mvn package -Pselenium3`** `# build Selenium 3 configuration`  
> **`mvn package -Pselenium2`** `# build Selenium 2 configuration`

Note that the version number in this POM file is merely a placeholder - a token that gets replaced during the normal build process. Finalized, functional `POMs` can be found within the **Selenium Foundation** JARs themselves at:

> **`META-INF\maven\com.nordstrom.ui-tools\selenium-foundation\pom.xml`**

## Getting Started

The [QuickStart](src/test/java/com/nordstrom/automation/selenium/QuickStart.java) class provides a fully-functional example of a test class built around **Selenium Foundation**,  [TestNG Foundation](https://github.com/sbabcoc/TestNG-Foundation), and the [Settings API](https://github.com/sbabcoc/Settings). It demonstrates how to set up required elements and introduces several key features that you're likely to use on a regular basis. 

### Required Configuration

**Selenium Foundation** supports both **TestNG** and **JUnit 4** frameworks, but the default configuration doesn't activate test lifecycle features for either of them. Choose your platform and apply the corresponding required configuration to your project:

* [TestNG Required Configuration](docs/TestNGSupport.md#testng-required-configuration)
* [JUnit 4 Required Configuration](docs/JUnit4Support.md#junit-4-required-configuration)

## Highlights

### Support for Appium Automation Engines

In addition to support for all of the standard Java-based browser drivers, the `Local Grid` feature of **Selenium Foundation** provides the ability to drive mobile and desktop applications via **Appium**. Driver plug-ins are available for all of the major [automation engines](docs/ConfiguringProjectSettings.md#appium-automation-engine-support), with the ability to customize out-of-the-box settings with [configurable modifications](docs/CustomizingCapabilities.md#specifying-modifiers-for-browser-capabilities-and-node-configurations) and [command line options](docs/LocalGridConfiguration.md#appium-server-arguments). 

### Automatic Driver Targeting

**Selenium Foundation** provides a complete set of base classes for building well-factored page models. This includes page components and frames. **Selenium Foundation** allows you to focus on modeling your application (instead of managing which window or frame the driver is addressing) by handling all driver targeting for you. You'll never see `driver.switchTo(...)` in page model automation built with **Selenium Foundation**, because the framework automatically ensures that the driver is addressing the window or frame associated with each page model method before it's invoked.

### Automatic Targeted Session Configuration

**Selenium Foundation** provides a [`target platform`](docs/TargetPlatformFeature.md#introduction) feature that enables you to define collections of configurations that can be assigned to specific test methods. Prior to the start of each test, you have the chance to "activate" the assigned platform (e.g. - change screen dimensions).
 
### Support for Frame-Based Components and Shadow-DOM Hierarchies

**Selenium Foundation** provides base classes for modeling frame-based components and shadow-DOM hierarchies. These base classes handle the specific details of interacting with these DOM features through the underlying Selenium API, managing search context and driver targeting for you. The implementation of your components will be totally dedicated to the functionality of the elements you're modeling - never cluttered with boilerplate code to switch driver focus or traverse into shadow hierarchies.

### Landing Page Verification and Model-Directed Navigation

Page classes can be explicitly associated with web application paths through the **`@PageUrl`** annotation. These associations can be declared as either fixed paths or patterns, and these declarations are used by **Selenium Foundation** to verify landing page paths at page transitions. You can also perform direct navigation to web application paths associated with page classes through the **`@PageUrl`** annotation.

### Customizable Transition Error Detection

In conjunction with automatic landing page verification, **Selenium Foundation** invokes registered custom [transition error detectors](docs/TransitionErrorDetection.md#introduction). Implement the [TransitionErrorDetector](src/main/java/com/nordstrom/automation/selenium/interfaces/TransitionErrorDetector.java) interface, then register your detectors in the corresponding service loader configuration file (**META-INF/services/com.nordstrom.automation.selenium.interfaces.TransitionErrorDetector**).

Examples of the sorts of conditions you may want to detect include error pages (e.g. - page not found) or non-context error messages (e.g. - communication issues, access token timeout). For recoverable conditions, error detectors can also server as error handler. For example, you could implement a detector that automatically logs back in if your test encounters an access timeout.

### Component Collection Classes

**Selenium Foundation** also includes collection classes ([ComponentList](src/main/java/com/nordstrom/automation/selenium/model/ComponentList.java), [ComponentMap](src/main/java/com/nordstrom/automation/selenium/model/ComponentMap.java), [FrameList](src/main/java/com/nordstrom/automation/selenium/model/FrameList.java), [FrameMap](src/main/java/com/nordstrom/automation/selenium/model/FrameMap.java), [ShadowRootList](src/main/java/com/nordstrom/automation/selenium/model/ShadowRootList.java), and [ShadowRootMap](src/main/java/com/nordstrom/automation/selenium/model/ShadowRootMap.java)) that enable you to define collections of components for your page models. For example, you can define a **SearchResultTile** component and include a map of these tiles keyed by product ID in your **SearchResultsPage** class. **Selenium Foundation** collections are lazy-initialized automatically - the composition of the collection is determined when it's instantiated, but each item in the collection is only populated when it's explicitly referenced.

### Automatic Stale Element Reference Protection

One of the most impactful features of **Selenium Foundation** saves your automation from the dreaded **StaleElementReferenceException** failure. Web element search operations performed within the **Selenium Foundation** framework return enhanced references, which retain all of the parameters needed to re-acquire the reference if it goes stale. Every web element method call is guarded by an automatic recovery feature. If a reference goes stale, **Selenium Foundation** re-acquires the reference and re-issues the web element method call that encountered the exception. Your automation continues on normally, blissfully unaware of the disaster that was averted.

### Optional Elements

Another useful extension provided by **Selenium Foundation** is the optional element. This feature enables you to model elements that only exist on the page under specific conditions. For example, you can model an error message that only exists when a form is submitted with no value in a required field. Determining if the element exists is as easy as calling the `hasReference()` method of the optional element object.

### Page-Load Synchronization

**Selenium Foundation** automatically synchronizes your automation with ordinary page transitions, ensuring that tests don't get tripped up by application hesitation. Synchronizing your automation with dynamic content creation is easily done by implementing a simple interface (**DetectsLoadCompletion**). This greatly simplifies the modeling of single-page applications and pages rendered with dynamic content loading.

### Grid-Based Driver Creation

To avoid divergent behavior between local and remote execution, **Selenium Foundation** acquires driver sessions for local runs from a local instance of **Selenium Grid**. In addition to eliminating ordinary behavioral differences, this strategy provides two major benefits:

1. Adding support for a new driver is a simple configuration change - No need to crack open the code!
2. You get explicit control over the maximum number of concurrent sessions, so you can run your tests in parallel without over-taxing your system.

### Automatic Phase-to-Phase Driver Hand-Off

Drivers allocated for per-test configuration setup methods (i.e. - **`@BeforeMethod`**) are automatically handed off to the tests for which configuration is being performed. Drivers allocated for tests are automatically handed off to per-test configuration cleanup methods (i.e. - **`@AfterMethod`**).  This hand-off behavior greatly simplifies the implementation of generic setup and cleanup processing that interacts with your application under test.

### Automatic Capture of Screenshots and Page Source

To assist in root-cause analysis, **Selenium Foundation** automatically captures a screenshot and page source for each failed test. By using the **ReporterAppender**, the log output of each **TestNG** test is captured as part of the test result object. This information is automatically shown on test result pages in **Jenkins**. No more digging through intermingled output in console logs!

### Support for TestNG and JUnit 4

**Selenium Foundation** includes support for both **TestNG** and **JUnit 4**, enabled by several core abstractions, and through features provided by the **TestNG Foundation** and **JUnit Foundation** libraries.

All of the features of **Selenium Foundation** are available regardless of which testing framework you choose. Once the initial configuration is done, the abstraction provided by the **TestBase** interface enables your code to be almost entirely framework-agnostic. This is clearly demonstrated in [ModelTestCore](src/test/java/com/nordstrom/automation/selenium/core/ModelTestCore.java), which contains the implementations for a collection of tests that are invoked from both TestNG (via [ModelTest](src/test/java/com/nordstrom/automation/selenium/model/ModelTest.java)) and JUnit 4 (via [JUnitModelTest](src/test/java/com/nordstrom/automation/selenium/junit/JUnitModelTest.java)).

#### Learn more about...
* [TestNG Support](docs/TestNGSupport.md#introduction)
* [JUnit 4 Support](docs/JUnit4Support.md#introduction)

## Essential Settings 

You'll probably find that the defaults assigned to most settings will suffice in most basic scenarios. However, it's likely that you'll need to override one or more of the following. The **Property Name** column indicates the name of the System property associated with the setting. To override a setting, you can either add a line for the setting to your _settings.properties_ file or define a System property. 

| Setting | Property Name | Default |
| --- | --- |:---:|
| **`BROWSER_NAME`** | `selenium.browser.name` | _(none)_ * |
| **`TARGET_HOST`** | `selenium.target.host` | `localhost` |
| **`TARGET_PATH`** | `selenium.target.path` | `/` |

\* NOTE: By default, HtmlUnit is selected as the browser. For easier override, this is specified through **`BROWSER_CAPS`** instead of **`BROWSER_NAME`**. For details, see [Configuring Project Settings](docs/ConfiguringProjectSettings.md#introduction). 

## Overriding Defaults 

**SeleniumConfig** searches a series of locations for a _settings.properties_ file. This file will typically be stored in your user "home" folder. Any settings declared in this file will override the defaults assigned in the **SeleniumSettings** enumeration. Settings that are declared as System properties will override both the defaults assigned by **SeleniumSettings** and settings declared in _settings.properties_. For example: 

| _settings.properties_ |
| --- |
| selenium.target.host=my.server.com |
| selenium.browser.name=chrome |

This sample _settings.properties_ file overrides the values of **TARGET_HOST** and **BROWSER_NAME**. The latter can be overridden by System property declaration: 
> `-Dselenium.browser.name=firefox`

The hierarchy of evaluation produces the following results: 

> **BROWSER_NAME** = <mark>firefox</mark>; **TARGET_HOST** = <mark>my.server.com</mark>; **TARGET_PATH** = <mark>/</mark> 

## Installing Drivers 

Whichever browser you choose to run your automation on, you need to make sure to install the latest driver for that browser compatible with your target version of **Selenium WebDriver**, along with a compatible release of the browser itself. We recommend that you install the drivers and browsers on the file search path to avoid the need to provide additional configuration details via scenario-specific means. 

Here are the official homes for several of the major drivers: 

* GhostDriver (PhantomJS) - [http://phantomjs.org/download.html](http://phantomjs.org/download.html)
* ChromeDriver - [https://sites.google.com/a/chromium.org/chromedriver/downloads](https://sites.google.com/a/chromium.org/chromedriver/downloads)
* IEDriver - [http://selenium-release.storage.googleapis.com/index.html?path=2.53/](http://selenium-release.storage.googleapis.com/index.html?path=2.53/)

**NOTE**: GhostDriver and ChromeDriver are simple binary installations, but several system configuration changes must be applied for IEDriver to work properly. For details, visit the InternetExplorerDriver project Wiki on GitHub and follow the [Required Configuration](https://github.com/SeleniumHQ/selenium/wiki/InternetExplorerDriver#required-configuration) procedure.

> Written with [StackEdit](https://stackedit.io/).
