# Manipulate Settings with SeleniumConfig

**SeleniumConfig** declares settings and methods related to Selenium WebDriver and Grid configuration. This class is built on the **Settings API**, composed of defaults, stored values, and System properties. **SeleniumSettings** declares the constants, property names, and default values for the settings managed by SeleniumConfig. Defaults can be overridden via System properties or the _settings.propeties_ file in your user "home" directory.

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

## SeleniumSettings

The static `getConfig()` method returns a **SeleniumConfig** object that provides settings and methods related to Selenium WebDriver and Grid configuration. The individual settings managed to **SeleniumConfig** are declared by the **SeleniumSettings** enumeration. The following sections provide the details of these settings - constants, property names, defaults, interrelationships, and utilization.

### TARGET APPLICATION URI

**SeleniumConfig** provides a `getTargetUri()` method that assembles the URI of the target application from five discrete settings:

<table style="text-align: left; border: 1px solid black; border-collapse: collapse;">
    <tr>
        <th style="text-align: left; border: 1px solid black;">Constant</th>
        <th style="text-align: left; border: 1px solid black;">Property Name</th>
        <th style="text-align: center; border: 1px solid black;">Default</th>
    </tr>
    <tr>
        <td style="text-align: left; border: 1px solid black;">TARGET_SCHEME</td>
        <td style="text-align: left; border: 1px solid black;">selenium.target.scheme</td>
        <td style="text-align: center; border: 1px solid black;">http</td>
    </tr>
    <tr>
        <td style="text-align: left; border: 1px solid black;">TARGET_CREDS</td>
        <td style="text-align: left; border: 1px solid black;">selenium.target.creds</td>
        <td style="text-align: center; border: 1px solid black;">(none)</td>
    </tr>
    <tr>
        <td style="text-align: left; border: 1px solid black;">TARGET_HOST</td>
        <td style="text-align: left; border: 1px solid black;">selenium.target.host</td>
        <td style="text-align: center; border: 1px solid black;">localhost</td>
    </tr>
    <tr>
        <td style="text-align: left; border: 1px solid black;">TARGET_PORT</td>
        <td style="text-align: left; border: 1px solid black;">selenium.target.port</td>
        <td style="text-align: center; border: 1px solid black;">(none)</td>
    </tr>
    <tr>
        <td style="text-align: left; border: 1px solid black;">TARGET_PATH</td>
        <td style="text-align: left; border: 1px solid black;">selenium.target.path</td>
        <td style="text-align: center; border: 1px solid black;">/</td>
    </tr>
</table>

**TARGET_PORT** enables you to specify a non-default number. **TARGET_PATH** enables you to specify a base path from which all pages originate. **TARGET_CREDS** is used to provide credentials is standard URL format.

### SELENIUM GRID CONFIGURATION

**SeleniumConfig** provides several methods related to local and remote Selenium Grid configuration:

* `getHubConfig()` - Get the Grid hub configuration as a **GridHubConfiguration** object. This configuration is backed by a file whose name is specified by **HUB_CONFIG** (default: _hubConfig.json_). The hub host and port are specified by **HUB_HOST** (default: _localhost_) and **HUB_PORT** (default: 4444) respectively. 
* `getHubArgs()` - If running with a local Grid configuration, this method provides the command line arguments used to launch the local Grid hub.
* `getNodeConfig()` - Get the Grid node configuration as a **RegistrationRequest** object. This configuration is backed by a file whose name is specified by **NODE_CONFIG** (default: _nodeConfig.json_). The node host and port are specified by **NODE_HOST** (default: _localhost_) and **NODE_PORT** (default: 5555) respectively.
* `getNodeArgs()` - If running a local Grid configuration, this method provides the command line arguments used to launch the local Grid node.

### BROWSER CAPABILITIES

The `getBrowserCaps()` converts the configured browser settings into a **Capabilities** object:

* If **BROWSER_NAME** is specified...
  * ... **Selenium Foundation** searches for a capabilities file named _&lt;browser-name&gt;Caps.json_.
    * If found, the content of this file provides the JSON configuration object.
    * Otherwise, a basic JSON configuration is assembled from the browser name.
* Otherwise, the JSON configuration is acquired from **BROWSER_CAPS**.

As indicated by the sequence above, the desired browser can be specified solely by name. This name can be associated with a JSON configuration file, or it may simply specify the name associated with a particular browser by its corresponding driver. If browser name is omitted, a fully-specified JSON capabilities record is acquired from the settings collection. (By default, the browser capabilities setting specifies the PhantomJS browser.)

This process yields the configured JSON capabilities record, which `getBrowserCaps()` uses to instantiate a standard **RegistrationRequest** object. The **Capabilities** object embedded in this request is then extracted and returned to the caller.

### TIMEOUT SETTINGS AND THE `WaitType` ENUMERATION 

The following table documents the timeout settings defined by **Selenium Foundation**:

<table style="text-align: left; border: 1px solid black; border-collapse: collapse;">
    <tr>
        <th style="text-align: left; border: 1px solid black;">Constant</th>
        <th style="text-align: left; border: 1px solid black;">Property Name</th>
        <th style="text-align: center; border: 1px solid black;">Default</th>
        <th style="text-align: left; border: 1px solid black;">Description</th>
    </tr>
    <tr>
        <td style="text-align: left; border: 1px solid black;">PAGE_LOAD_TIMEOUT</td>
        <td style="text-align: left; border: 1px solid black;">selenium.timeout.pageload</td>
        <td style="text-align: center; border: 1px solid black;">30</td>
        <td style="text-align: left; border: 1px solid black;">Driver page load timeout interval; also used by page load completion and landing page verification features.</td>
    </tr>
    <tr>
        <td style="text-align: left; border: 1px solid black;">IMPLIED_TIMEOUT</td>
        <td style="text-align: left; border: 1px solid black;">selenium.timeout.implied</td>
        <td style="text-align: center; border: 1px solid black;">15</td>
        <td style="text-align: left; border: 1px solid black;">Driver implicit wait timeout interval; also used by stale element reference refresh feature.</td>
    </tr>
    <tr>
        <td style="text-align: left; border: 1px solid black;">SCRIPT_TIMEOUT</td>
        <td style="text-align: left; border: 1px solid black;">selenium.timeout.script</td>
        <td style="text-align: center; border: 1px solid black;">30</td>
        <td style="text-align: left; border: 1px solid black;">Driver asynchronous script timeout interval.</td>
    </tr>
    <tr>
        <td style="text-align: left; border: 1px solid black;">WAIT_TIMEOUT</td>
        <td style="text-align: left; border: 1px solid black;">selenium.timeout.wait</td>
        <td style="text-align: center; border: 1px solid black;">15</td>
        <td style="text-align: left; border: 1px solid black;"><b>Selenium Foundation</b> search context wait timeout interval.</td>
    </tr>
    <tr>
        <td style="text-align: left; border: 1px solid black;">HOST_TIMEOUT</td>
        <td style="text-align: left; border: 1px solid black;">selenium.timeout.host</td>
        <td style="text-align: center; border: 1px solid black;">30</td>
        <td style="text-align: left; border: 1px solid black;"><b>Selenium Foundation</b> HTTP host wait timeout interval.</td>
    </tr>
</table>

Typically, these settings are not accessed directly. Rather, they are proxied through the **WaitType** enumeration to streamline the process of reading the timeout intervals and acquiring search context wait objects.
