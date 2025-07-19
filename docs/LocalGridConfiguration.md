# Introduction

By default, **Selenium Foundation** acquires driver sessions from an instance of **Selenium Grid**. If you specify an existing instance in your [configuration](ConfiguringProjectSettings.md#selenium-grid-configuration), sessions will be acquired from there. If this configuration setting is left undefined or specifies an inactive `localhost` URL, **Selenium Foundation** will automatically launch a local Grid instance as specified by the local Grid configuration.

## Stand-Alone Selenium Grid with [local-grid-parent](https://github.com/sbabcoc/local-grid-parent)

In addition to providing a local Grid instance for running Selenium tests on your machine, this **Selenium Foundation** feature can be used to launch stand-alone instances that can be utilized in more conventional fashion by tests running on other machines. The easiest way to manage a stand-alone instance of **Selenium Grid** is via the [Local Grid Parent](https://github.com/sbabcoc/local-grid-parent) utility. This utility leverages the capabilities of **Selenium Foundation** to configure and manage Selenium Grid instances.

## Local Selenium Grid Configuration

To maximize flexibility, configurability, consistency, and control, the browser sessions dispensed by **Selenium Foundation** are provided by an instance of **Selenium Grid**. To use an existing Grid instance for session provisioning, specify its endpoint URL in the [**`HUB_HOST`** setting](ConfiguringProjectSettings.md#selenium-grid-configuration).  By default, **Selenium Foundation** will launch a local Grid and acquire sessions from there.

The unit tests of this project acquire browser sessions from a local Grid instance that gets launched at the start of the test run and torn down when the run is complete. By default, the local Grid runs in "servlet container" mode, and only non-browser support feature tests are run. Profiles defined in the Gradle project file allows you to specify which browser to target with the corresponding unit tests. This aspect of the **Selenium Foundation** project provides a working example of how to configure for the local Grid feature, as detailed in the following sections.

### Specifying plug-ins via System property

The first place **Selenium Foundation** checks for the specification of driver plug-ins is in the System properties. You can set the `selenium.grid.plugins` property to a file-separator-delimited one or more fully qualified driver plug-in class names for the browsers that should be supported by the local Grid:

###### System property specification
```shell
-Dselenium.grid.plugins=com.nordstrom.automation.selenium.plugins.EdgePlugin
```

### ServiceLoader provider configuration file

The set of drivers supported by the local Grid instance managed by **Selenium Foundation** is configured by specifying plug-ins for the corresponding drivers in a **ServiceLoader** [provider configuration file](https://docs.oracle.com/javase/tutorial/ext/basics/spi.html#register-service-providers). For example, here's the contents of the [actual configuration file](../src/test/resources/META-INF/services/com.nordstrom.automation.selenium.DriverPlugin) for the project unit tests:

###### com.nordstrom.automation.selenium.DriverPlugin
```shell
com.nordstrom.automation.selenium.plugins.HtmlUnitPlugin
```

### Declaring Driver Dependencies

In addition to declaring driver plug-ins in the **ServiceLoader** provider configuration file, the Java project itself must declare the dependencies of the corresponding driver(s). These dependencies vary by the version of **Selenium API** you're using, and they're documented in the plug-in classes themselves. For example, here are the Maven dependency declarations for **`HtmlUnitPlugin`**:

| Selenium 3 | Selenium 4 |
|:---|:---|
| <pre>&lt;dependency&gt;<br/>&nbsp;&nbsp;&lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;<br/>&nbsp;&nbsp;&lt;artifactId&gt;htmlunit-driver&lt;/artifactId&gt;<br/>&nbsp;&nbsp;&lt;version&gt;2.70.0&lt;/version&gt;<br/>&nbsp;&nbsp;&lt;exclusions&gt;<br/>&nbsp;&nbsp;&nbsp;&nbsp;&lt;exclusion&gt;<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;artifactId&gt;selenium-support&lt;/artifactId&gt;<br/>&nbsp;&nbsp;&nbsp;&nbsp;&lt;/exclusion&gt;<br/>&nbsp;&nbsp;&lt;/exclusions&gt;<br/>&lt;/dependency&gt;</pre> | <pre>&lt;dependency&gt;<br/>&nbsp;&nbsp;&lt;groupId&gt;com.nordstrom.ui-tools&lt;/groupId&gt;<br/>&nbsp;&nbsp;&lt;artifactId&gt;:htmlunit-remote&lt;/artifactId&gt;<br/>&nbsp;&nbsp;&lt;version&gt;4.33.0&lt;/version&gt;<br/>&lt;/dependency&gt;</pre> |

### Driver Plug-Ins for Desktop Browsers and Appium Engines

Driver plug-ins encapsulate the specific details related to launching **Selenium Grid** nodes that support the corresponding drivers. **Selenium Foundation** provides [driver plug-ins](ConfiguringProjectSettings.md#desktop-browser-support) for all of the major browsers. It also provides plug-ins for all of the major [automation engines](ConfiguringProjectSettings.md#appium-automation-engine-support) of the `Appium` project.

#### Drivers for Desktop Browsers

Since the release of **Selenium Foundation** [28.0.0](https://github.com/sbabcoc/Selenium-Foundation/releases/tag/v28.0.0), we now use **Selenium Manager** (Selenium 4) and **Web Driver Manager** (Selenium 3) to acquire compatible drivers for the browsers targeted by your tests. If the manager is unable to locate or download a required driver, **DriverExecutableNotFoundException** is thrown.

**NOTE**: This driver acquisition process is bypassed for test classes that implement the [DriverProvider](ConfiguringProjectSettings.md#testing-with-non-default-browser-sessions) interface.

#### Appium-Specific Configuration

The automatic driver installation feature does not include management of Appium [automation engines](ConfiguringProjectSettings.md#appium-automation-engine-support), which must be installed separately, along with Appium's dependencies. With a conventional installation, **Selenium Foundation** can use your system configuration to locate the components that comprise an `Appium` node. For non-standard installations, **Selenium Foundation** provides [settings](ConfiguringProjectSettings.md#appium-binary-paths) that enable you to supply explicit paths to these items.

#### Appium Server Arguments

**Selenium Foundation** gives you the ability to completely control the configuration of the `Appium` Grid node that it uses to provide sessions for your tests, via the **`APPIUM_CLI_ARGS`** setting. This setting can define multiple `Appium` server arguments together, and can be declared multiple times when specified in the _settings.properties_ file. These are presented to **Selenium Foundation** as a collection of values that it processes and passes on to 'Appium` when it launches the Grid node.

#### Appium Driver Dependencies 

Although **Selenium Foundation** doesn't need the Java bindings for `Appium` to launch the Grid node, you'll need to declare this dependency in your Java projects to acquire device-specific drivers like **AndroidDriver** or **IOSDriver**. Here are the Maven artifact coordinates that correspond to each version of the **Selenium API**:

| Selenium 3 | Selenium 4 |
|:---|:---|
| <pre>&lt;dependency&gt;<br/>&nbsp;&nbsp;&lt;groupId&gt;io.appium&lt;/groupId&gt;<br/>&nbsp;&nbsp;&lt;artifactId&gt;java-client&lt;/artifactId&gt;<br/>&nbsp;&nbsp;&lt;version&gt;7.6.0&lt;/version&gt;<br/>&lt;/dependency&gt;</pre> | <pre>&lt;dependency&gt;<br/>&nbsp;&nbsp;&lt;groupId&gt;io.appium&lt;/groupId&gt;<br/>&nbsp;&nbsp;&lt;artifactId&gt;java-client&lt;/artifactId&gt;<br/>&nbsp;&nbsp;&lt;version&gt;9.4.0&lt;/version&gt;<br/>&lt;/dependency&gt;</pre> |

### Additional Local Grid Settings

To enable the `Local Grid` feature to support both **Selenium 3** and **Selenium 4**, the core configuration in **`SeleniumConfig`** defines version-specific default values for several settings:

| Setting | Property Name | `s3` Default | `s4` Default |
|---|---|---|---|
| **`GRID_LAUNCHER`** | `selenium.grid.launcher` | org.openqa.grid.selenium.GridLauncherV3 | org.openqa.selenium.grid.Bootstrap |
| **`LAUNCHER_DEPS`** | `selenium.launcher.deps` | [source](/src/selenium3/java/com/nordstrom/automation/selenium/SeleniumConfig.java#L174) | [source](/src/selenium4/java/com/nordstrom/automation/selenium/SeleniumConfig.java#L323) |
| **`HUB_PORT`** | `selenium.hub.port` | 4445 | 4446 |
| **`HUB_CONFIG`** | `selenium.hub.config` | hubConfig-s3.json | hubConfig-s4.json |
| **`NODE_CONFIG`** | `selenium.node.config` | nodeConfig-s3.json | nodeConfig-s4.json |

#### NOTES

* The **`GRID_LAUNCHER`** setting specifies the fully-qualified name of the main Java class for launching Grid servers.
* The **`LAUNCHER_DEPS`** setting specifies a semicolon-delimited list of fully-qualified names of context classes for the dependencies of the **`GRID_LAUNCHER`** class.
* The **`HUB_PORT`** setting specifies the HTTP port assigned to the Grid hub server. This is only used if the [**`HUB_HOST`** setting](ConfiguringProjectSettings.md#selenium-grid-configuration) is undefined. 
* Specifying **`HUB_PORT`** as `0` directs **Selenium Foundation** to automatically select any available port.
* The **`HUB_CONFIG`** setting specifies the configuration file for Grid hub servers.
* The **`NODE_CONFIG`** setting specifies the configuration template used to build configurations for Grid node servers. 

### Building Driver-Specific Node Configurations

During the **Local Grid** start-up process, **Selenium Foundation** builds a driver-specific node configuration for each plug-in specified in the **ServiceLoader** [provider configuration file](#serviceloader-provider-configuration-file):

* The **Capabilities** specification for the node is acquired from the driver plug-in.
* A base node configuration is built from this specification suitable for the target **Selenium API** version.
* **Selenium Foundation** determines if an [associated modifier](CustomizingCapabilities#specifying-modifiers-for-browser-capabilities-and-node-configurations) has been specified for the base configuration.
* The base configuration is merged with the configuration template specified by the **`NODE_CONFIG`** setting.
* The resulting node configuration is written to disk (unless the target file already exists)

## Launching the Local Selenium Grid

Once the configuration of the `Local Grid` is resolved, **Selenium Foundation** commences to launch:

* Launch the **Selenium Grid** hub server:
  * ... with the grid launcher class specified by the **`GRID_LAUNCHER`** setting
  * ... with dependency contexts specified by the **`LAUNCHER_DEPS`** setting
  * ... with the hub configuration specified by the **`HUB_CONFIG`** setting
  * ... specifying the IP address returned by `HostUtils.getLocalHost()`
  * ... listening to the port specified by the **`HUB_PORT`** setting
* Update the values of **`HUB_HOST`** and **`HUB_PORT`** to reflect the grid hub server configuration.
* For each plug-in specified in the **ServiceLoader** [provider configuration file](#serviceloader-provider-configuration-file):
  * Launch the driver-specific **Selenium Grid** node server:
    * `for `**`RemoteWebDriver`**` plug-in`:
      * ... with the grid launcher class used to launch the grid hub server
      * ... with additional dependency contexts specified by the plug-in
      * ... propagating the values of System properties specified by the plug-in
      * ... with the [assembled driver-specific node configuration](#building-driver-specific-node-configurations)
      * ... specifying the IP address returned by `HostUtils.getLocalHost()`
      * ... listening to the port returned by `PortProber.findFreePort()`
    * `for `[**`Appium`**](ConfiguringProjectSettings.md#appium-binary-paths)`plug-in`:
      * ... with `Node` executable specified by the **`NODE_BINARY_PATH`** setting
        * ... searching the System configuration if unspecified
      * ... with `Appium` main script specified by the **`APPIUM_BINARY_PATH`** setting
        * ... searching the global `Node` modules repository if unspecified
      * ... with command-line arguments specified by the **`APPIUM_CLI_ARGS`** setting
      * ... with the [assembled driver-specific node configuration](#building-driver-specific-node-configurations)
      * ... specifying the IP address returned by `HostUtils.getLocalHost()`
      * ... listening to the port returned by `PortProber.findFreePort()`

> Written with [StackEdit](https://stackedit.io/).
