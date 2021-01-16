
# Introduction

In **Selenium Foundation**, we've specified default values that work well in many scenarios. We also provide ways to fine-tune the default configuration as needed. One area where this is especially true is in the handling of capabilities for driver requests and local Grid node configuration.

## Desired Capabilities for Driver Requests

There are two settings used to select which browser you want to run your tests on:

* **`BROWSER_NAME`** - This setting specifies the browser name or "personality" for new session requests.
* **`BROWSER_CAPS`** - If **`BROWSER_NAME`** is undefined, this setting specifies the **Capabilities** for new session requests. This can be either a file path (absolute, relative, or simple filename) or a direct value.
* By default, **`BROWSER_NAME`** is unspecified and **`BROWSER_CAPS`** specifies the `HtmlUnit` browser.

### Selecting Browser Capabilities by Personality

The most concise way to customize the default browser configuration is to specify **`BROWSER_NAME`**.

| Scenario | **`BROWSER_NAME`** |
|:---|:---|
| You want to run your tests with `Chrome` | `chrome` |
| You want to run with 'headless' `Chrome` | `chrome.headless` |

Both of these values specify "personalities" defined by **`ChromePlugin`**. When **Selenium Foundation** creates a browser session at the beginning of each test, it finds the `desired capabilities` that match the specified "personality" in the collection supported by the configured **Selenium Grid** instance.

Once the specified "personality" is resolved to `desired capabilities`, **Selenium Foundation** determines if an [associated modifier](#specifying-modifiers-for-browser-capabilities-and-node-configurations) has been specified.

### Specifying Browser Capabilities Directly

If the "personalities" defined by the plug-in for your desired browser don't meet your needs, you can specify your desired capabilities in **`DRIVER_CAPS`**.

| Scenario | **`BROWSER_CAPS`** |
|:---|:---|
| You want to run `HtmlUnit` emulating `Chrome` | `{"browserName": "htmlunit", "browserVersion": "chrome"}` |
| You want to run with 'headless' `Firefox` | `firefoxHeadless.json` |

###### firefoxHeadless.json
```json
{
  "browserName": "firefox",
  "moz:firefoxOptions": {"args": ["-headless"]}
}
```

As shown, the value of **`BROWSER_CAPS`** must resolve to a JSON `Capabilities` specification. This can be either a direct value or a file path (absolute, relative, or simple filename).

> **NOTE**: Both of the example shown above are available via "personalities" - the first is `htmlunit` from **`HtmlUnitPlugin`**, and the second is `firefox.headless` from **`FirefoxPlugin`**.

Once the specified `desired capabilities` have been loaded, **Selenium Foundation** determines if an [associated modifier](#specifying-modifiers-for-browser-capabilities-and-node-configurations) has been specified.

## Specifying Modifiers for Browser Capabilities and Node Configurations

After resolving browser capabilities or driver plug-in node configurations, **Selenium Foundation** checks for a modifiers associated with the "personality" of each specification. If the corresponding modifier setting is specified, its resolved value is applied to the specification .

Modifiers are specified in the configuration as either JSON strings or file paths (absolute, relative, or simple filename). Property names for modifiers correspond to "personality" values within the capabilities themselves (in order of precedence):

* `personality`: Selenium Foundation "personality" name
* `automationName`: 'appium' automation engine name
* `browserName`: Selenium driver browser name

The first defined value is selected as the "personality" of the specified **Capabilities** object. The full name of the property used to specify modifiers is the "personality" plus a context-specific suffix:

* For node configuration capabilities: `<personality>.node.mods`
* For "desired capabilities" requests: `<personality>.caps.mods`

> Written with [StackEdit](https://stackedit.io/).
