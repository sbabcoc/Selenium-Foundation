
# Introduction

The configuration of **Selenium Foundation** projects is an aggregation of several factors:

* Project specification (e.g. - `pom.xml`, `build.gradle`)
* ServiceLoader [provider configuration files](https://docs.oracle.com/javase/tutorial/ext/basics/spi.html#register-service-providers):
  * Specification of driver plug-ins for local Grid
  * Specification of TestNG listener classes
  * Specification of JUnit watcher classes
* Configuration settings for...
  * ... Selenium Foundation (_settings.properties_)
  * ... [TestNG Foundation](https://github.com/sbabcoc/TestNG-Foundation) (_testng.properties_)
  * ... [JUnit Foundation](https://github.com/sbabcoc/JUnit-Foundation) (_junit.properties_)

Note that configuration settings can be overridden via System properties, and reasonable defaults are provided where they make sense. More on this in the next section.

# Generic Interfaces to Version-Specific Features

Each release of **Selenium Foundation** is published in two flavors: `s2` for Selenium 2 and `s3` for Selenium 3. Most of the code in this project is version-agnostic, built on interfaces that are consistent between the two major versions of the Selenium API. Functionality implemented with interfaces that differ between Selenium 2 and Selenium 3 is contained in flavor-specific folders in the **Selenium Foundation** project (`java-s2` and `java-s3`). These features include:

* Concrete configuration: `com.nordstrom.automation.selenium.SeleniumConfig`
* Driver wrapper: `com.nordstrom.automation.selenium.interfaces.WrapsDriver`
* JSON data utility: `com.nordstrom.automation.selenium.utility.DataUtils`
* Local Grid driver plug-ins: `com.nordstrom.automation.selenium.plugins.*`

# Concrete Configuration (SeleniumConfig)

**`SeleniumConfig`** declares settings and methods related to Selenium WebDriver and Grid configuration. This class is built on the **Settings API**, composed of defaults, stored values, and System properties. **`SeleniumSettings`** declares the constants, property names, and default values for the settings managed by **`SeleniumConfig`**. Defaults can be overridden via System properties or the _settings.properties_ file typically found in your user "home" directory.

The configuration object itself is maintained as a static singleton, ubiquitously available through a static method of the `SeleniumConfig` class:

```java
SeleniumConfig config = SeleniumConfig.getConfig();
```

## OVERRIDING DEFAULTS 

**`SeleniumConfig`** searches a series of locations for a _settings.properties_ file. This file will typically be stored in your user "home" folder. Any settings declared in this file will override the defaults assigned in the **`SeleniumSettings`** enumeration. Settings that are declared as System properties will override both the defaults assigned by **`SeleniumSettings`** and settings declared in _settings.properties_. For example: 

| _settings.properties_ |
|:---|
| selenium.target.host=my.server.com |
| selenium.browser.name=chrome |

This sample _settings.properties_ file overrides the values of **`TARGET_HOST`** and **`BROWSER_NAME`**. The latter can be overridden by System property declaration: 

> `-Dselenium.browser.name=firefox`

The hierarchy of evaluation produces the following results: 

> **`BROWSER_NAME`** = <mark>firefox</mark>; **`TARGET_HOST`** = <mark>my.server.com</mark>; **`TARGET_PATH`** = <mark>/</mark>

## Location Strategy for _settings.properties_

As mentioned previously, you're able to override defaults in **`SeleniumConfig`** with values defined in a _settings.properties_ file. This file is typically stored in your user "home" folder, but **Selenium Foundation** employs a file location strategy that searches for _settings.properties_ in a number of places. This strategy is described in detail under [DEFAULT_LOCATION_STRATEGY](https://commons.apache.org/proper/commons-configuration/apidocs/org/apache/commons/configuration2/io/FileLocatorUtils.html#DEFAULT_LOCATION_STRATEGY) in the documentation for **Apache Commons Configuration**.

## Configuration-Based API Abstration 

**`SeleniumConfig`** is more than a collection of settings; this class also provides generic interfaces to several core aspects of the WebDriver API that differ between Selenium 2 and Selenium 3:

* Convert JSON string to **`Capabilities`** object: `getCapabilitiesForJson(String capabilities)`
* Convert object to JSON string: `toJson(Object obj)`
* Get context class names for Grid dependencies: `getDependencyContexts()`
* Create Grid node configuration file from JSON: `createNodeConfig(String jsonStr, URL hubUrl)`
* Merge capabilities objects: `mergeCapabilities(Capabilities target, Capabilities change)`


## THE **`SeleniumSettings`** OBJECT

The static `getConfig()` method returns a **`SeleniumConfig`** object that provides settings and methods related to Selenium WebDriver and Grid configuration. The individual settings managed by **`SeleniumConfig`** are declared by the **`SeleniumSettings`** enumeration. The following sections provide the details of these settings - constants, property names, defaults, interrelationships, and utilization.

### Selenium Grid Configuration

| Setting | Property Name | Default |
|---|---|---|
| **`HUB_HOST`** | `selenium.hub.host` | _(none)_ |
| **`GRID_PLUGINS`** | `selenium.grid.plugins` | _none_ |

The **`HUB_HOST`** setting specifies the URL of the **Selenium Grid** hub server that will supply sessions for your tests. No default value is specified for this setting. If you specify a value for this setting, you must provide the full URL of an active hub server, including the base path. For example:

> `-Dselenium.hub.host=http://192.168.1.2/wd/hub`

> If this setting is left undefined or specifies an inactive `localhost` URL, **Selenium Foundation** will automatically launch a local Grid instance as specified by the [local Grid configuration](LocalGridConfiguration.md).

The **`GRID_PLUGINS`** setting specifies a semicolon-delimited list of fully-qualified names of local **Selenium Grid** driver plugin classes.

**NOTE**: Defining a value for this setting overrides the **ServiceLoader** specification defined by the associated provider configuration file (_com.nordstrom.automation.selenium.DriverPlugin_).

#### Selenium Grid Configuration Methods 

**`SeleniumConfig`** provides several methods related to local and remote Selenium Grid configuration:

* `getSeleniumGrid()` - Get an object that represents the active Selenium Grid. If indicated, this method wil launch a local Grid instance.
* `getHubUrl()` - Get the URL for the configured Selenium Grid hub host. In local Grid configurations, this value will be populated when the Grid is launched.
* `shutdownGrid(boolean localOnly)` - Shut down the active Selenium Grid. To enable shutdown of remote Grid instances, attached Grid nodes must install the **`LifecycleServlet`**:

> -servlets org.openqa.grid.web.servlet.LifecycleServlet

#### Grid Configuration for Selenium Foundation unit tests

As of this writing, the unit tests are [configured](LocalGridConfiguration.md) to get their browsers from a local Selenium Grid instance serving [HtmlUnit](http://htmlunit.sourceforge.net/) sessions. Described as "a GUI-less browser for Java programs", this browser is perfect for rendering the simple pages used to exercise the features of **Selenium Foundation**. 

#### Desktop Browser Support

**Selenium Foundation** provides the ability to drive web applications through common desktop browsers. Currently, plug-ins are available for the following browsers:

| Browser | Selenium 2 | Selenium 3 |
| --- |:---:|:---:|
| Chrome | [source](../src/seleniun2/java/com/nordstrom/automation/selenium/plugins/ChromePlugin.java) | [source](../src/selenium3/java/com/nordstrom/automation/selenium/plugins/ChromePlugin.java) |
| Edge | [source](../src/selenium2/java/com/nordstrom/automation/selenium/plugins/EdgePlugin.java) | [source](../src/selenium3/java/com/nordstrom/automation/selenium/plugins/EdgePlugin.java) |
| Firefox | [source](../src/selenium2/java/com/nordstrom/automation/selenium/plugins/FirefoxPlugin.java) | [source](../src/selenium3/java/com/nordstrom/automation/selenium/plugins/FirefoxPlugin.java) |
| HtmlUnit | [source](../src/selenium2/java/com/nordstrom/automation/selenium/plugins/HtmlUnitPlugin.java) | [source](../src/selenium3/java/com/nordstrom/automation/selenium/plugins/HtmlUnitPlugin.java) |
| Internet Explorer | [source](../src/selenium2/java/com/nordstrom/automation/selenium/plugins/InternetExplorerPlugin.java) | [source](../src/selenium3/java/com/nordstrom/automation/selenium/plugins/InternetExplorerPlugin.java) |
| Opera | [source](../src/selenium2/java/com/nordstrom/automation/selenium/plugins/OperaPlugin.java) | [source](../src/selenium3/java/com/nordstrom/automation/selenium/plugins/OperaPlugin.java) |
| PhantomJS | [source](../src/selenium2/java/com/nordstrom/automation/selenium/plugins/PhantomJsPlugin.java) | [source](../src/selenium3/java/com/nordstrom/automation/selenium/plugins/PhantomJsPlugin.java) |
| Safari | [source](../src/selenium2/java/com/nordstrom/automation/selenium/plugins/SafariPlugin.java) | [source](../src/selenium3/java/com/nordstrom/automation/selenium/plugins/SafariPlugin.java) |

#### Appium Automation Engine Support

In addition to support for desktop browsers, **Selenium Foundation** provides the ability to drive mobile and desktop applications via Appium. Currently, plug-ins are available for the following automation engines:

| Automation Engine | Plug-In |
| --- |:---:|
| Espresso | [source](../src/main/java/com/nordstrom/automation/selenium/plugins/EspressoPlugin.java) |
| Mac2 | [source](../src/main/java/com/nordstrom/automation/selenium/plugins/Mac2Plugin.java) |
| UiAutomator2 | [source](../src/main/java/com/nordstrom/automation/selenium/plugins/UiAutomator2Plugin.java) |
| Windows | [source](../src/main/java/com/nordstrom/automation/selenium/plugins/WindowsPlugin.java) |
| XCUITest | [source](../src/main/java/com/nordstrom/automation/selenium/plugins/XCUITestPlugin.java) |

Note that these plug-ins are not version-specific; each will work with either Selenium 2 or Selenium 3 API, and no Java dependencies are required for **Selenium Foundation** to launch the associated Grid node. However, you'll need to have Appium and its dependencies [installed](http://appium.io/docs/en/about-appium/getting-started/) on your system. At a minimum, you need `NodeJS`, `NPM` (Node Package Manager), and `Appium` itself. By default, **Selenium Foundation** will search for `NodeJS` and `NPM` on the system path, and expects to find `Appium` in the global Node package repository. However, you can supply explicit paths to these item in your settings:

###### Appium Binary Paths

| Item | Setting | Property | Default |
| --- | --- | --- | --- |
| **NodeJS** | **`NODE_BINARY_PATH`** | `node.binary.path` | NODE_BINARY_PATH environment variable |
| **NPM** | **`NPM_BINARY_PATH`** | `npm.binary.path` | _(none)_ |
| **Appium** | **`APPIUM_BINARY_PATH`** | `appium.binary.path` | APPIUM_BINARY_PATH environment variable |

### Browser Capabilities

The `getCurrentCapabilities()` method converts the configured browser settings into a **`Capabilities`** object:

* If **`BROWSER_NAME`** is specified, **Selenium Foundation** uses this name to acquire a **`Capabilities`** object from the active Selenium Grid.
* Otherwise, the JSON configuration is acquired from **`BROWSER_CAPS`**, which can be either a file path (absolute, relative, or simple filename) or a direct value.

As indicated by the sequence above, the desired browser can be specified solely by name. This name can be associated with a Selenium Grid browser "personality", or it may simply specify the name associated with a particular browser by its corresponding driver. If browser name is omitted, a fully-specified JSON capabilities record is acquired from the settings collection. (By default, the browser capabilities setting specifies the **HtmlUnit** browser.)

The `getCapabilitiesForName(String browserName)` method enables you to get a **`Capabilities`** object for a specified name or "personality" from the active Selenium Grid. This bypasses the configured default browser specification, allowing you to acquire browser sessions of any type supported by the active Grid.

##### Testing with non-default browser sessions

The easiest way to provide non-default browser sessions to your test methods is to implement the [DriverProvider](../src/main/java/com/nordstrom/automation/selenium/interfaces/DriverProvider.java) interface:

```java
public class MyChromeTest extends TestNgBase implements DriverProvider {
    
    ...
    
    @Override
    public WebDriver provideDriver(Method method) {
        SeleniumConfig config = SeleniumConfig.getConfig();
        URL remoteAddress = config.getSeleniumGrid().getHubServer().getUrl();
        Capabilities desiredCapabilities = config.getCapabilitiesForName("chrome");
        return GridUtility.getDriver(remoteAddress, desiredCapabilities);
    }
}
```

With this implementation, every test method will be provided with a Chrome browser session regardless of configured default browser specification. Note that the active Selenium Grid must be configured to support Chrome for this to actually work.

### Target Application URI

**`SeleniumConfig`** provides a `getTargetUri()` method that assembles the URI of the target application from five discrete settings:

| **Constant** | **Property Name** | **Default** |
|---|---|:---:|
| **`TARGET_SCHEME`** | selenium.target.scheme | http |
| **`TARGET_CREDS`** | selenium.target.creds | _(none)_ |
| **`TARGET_HOST`** | selenium.target.host | localhost |
| **`TARGET_PORT`** | selenium.target.port | _(none)_ |
| **`TARGET_PATH`** | selenium.target.path | / |

**`TARGET_PORT`** enables you to specify a non-default port number. **`TARGET_PATH`** enables you to specify a base path from which all pages originate. **`TARGET_CREDS`** is used to provide credentials in standard URL format.

There's also a `setTargetUri(URI targetUrl)` method that enables you to set this core value and its component settings dynamically. The **Selenium Foundation** unit tests use this method to set the root of their page navigations to the hub server of the local Grid instance.

### Timeout Settings and the `WaitType` Enumeration 

The following table documents the timeout settings defined by **Selenium Foundation**:

| **Constant** | **Property Name** | **Default** | **Description** |
|---|---|:---:|---|
| **`PAGE_LOAD_TIMEOUT`** | selenium.timeout.pageload | 30 | Driver page load timeout interval; also used by page load completion and landing page verification features. |
| **`IMPLIED_TIMEOUT`** | selenium.timeout.implied | 15 | Driver implicit wait timeout interval; also used by stale element reference refresh feature. |
| **`SCRIPT_TIMEOUT`** | selenium.timeout.script | 30 | Driver asynchronous script timeout interval. |
| **`WAIT_TIMEOUT`** | selenium.timeout.wait | 15 | Selenium Foundation search context wait timeout interval. |
| **`HOST_TIMEOUT`** | selenium.timeout.host | 30 | Selenium Foundation HTTP host wait timeout interval. |

Typically, these settings are not accessed directly. Rather, they are proxied through the **`WaitType`** enumeration to streamline the process of reading the timeout intervals and acquiring search context wait objects.

> Written with [StackEdit](https://stackedit.io/).
