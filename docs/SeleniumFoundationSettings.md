# Selenium Foundation Settings

Configuration settings for **Selenium Foundation**, controlled via `settings.properties` or System property declarations.  

**NOTE**: The System property key associated with each setting can be revealed by tapping the disclosure triangle next to the setting comnstant name.

---

## Target URI

Settings that define the components of the target URI used for test navigation.

| Setting | Default | Description |
|---------|---------|-------------|
| <details><summary>**TARGET_SCHEME**</summary>`selenium.target.scheme`</details> | `http` | Scheme component of the target URI |
| <details><summary>**TARGET_CREDS**</summary>`selenium.target.creds`</details> | *(none)* | <details><summary>Credentials component of the target URI</summary>Provides credentials in standard URL format<br>(e.g. - `username:password`)</details> |
| <details><summary>**TARGET_HOST**</summary>`selenium.target.host`</details> | `localhost` | Host component of the target URI |
| <details><summary>**TARGET_PORT**</summary>`selenium.target.port`</details> | *(none)* | <details><summary>Port component of the target URI</summary>Specifies a non-default port number</details> |
| <details><summary>**TARGET_PATH**</summary>`selenium.target.path`</details> | `/` | <details><summary>Path component of the target URI</summary>Specifies a base path from which all pages originate</details> |

---

## Selenium Grid

Settings that control the local Selenium Grid instance.

| Setting | Default | Description |
|---------|---------|-------------|
| <details><summary>**GRID_PLUGINS**</summary>`selenium.grid.plugins`</details> | *(none)* | <details><summary>Path-delimited list of fully-qualified driver plug-in class names.</summary>**NOTE**: Overrides the `ServiceLoader` provider configuration file.</details> |
| <details><summary>**GRID_SERVLETS**</summary>`selenium.grid.servlets`</details> | *(none)* | <details><summary>Comma-delimited list of fully-qualified servlet class names.</summary>**Selenium 3**: Hosted by the hub server.<br>**Selenium 4**: Hosted by `ServletContainer`.</details> |
| <details><summary>**SHUTDOWN_GRID**</summary>`selenium.grid.shutdown`</details> | `true` | Whether to shut down the local Grid instance at the end of the test run |
| <details><summary>**GRID_LAUNCHER**</summary>`selenium.grid.launcher`</details> | <details><summary>*version-specific*</summary>**Selenium 3:** `org.openqa.grid.selenium.GridLauncherV3`<br>**Selenium 4**: `org.openqa.selenium.grid.Bootstrap`</details> | Fully-qualified name of the `GridLauncher` class. |
| <details><summary>**LAUNCHER_DEPS**</summary>`selenium.launcher.deps`</details> | *version-specific* | Path-delimited list of fully-qualified context class names for Grid launcher dependencies |
| <details><summary>**SLOT_MATCHER**</summary>`selenium.slot.matcher`</details> | `FoundationSlotMatcher` | Slot matcher used by the hub server |
| <details><summary>**GRID_WORKING_DIR**</summary>`selenium.grid.working.dir`</details> | *(none)* | Working directory for local Grid server processes |
| <details><summary>**GRID_LOGS_FOLDER**</summary>`selenium.grid.log.folder`</details> | `logs` | <details><summary>Log file folder for local Grid server processes.</summary>If relative, resolved against `GRID_WORKING_DIR` (or `user.dir` if unset).</details> |
| <details><summary>**GRID_NO_REDIRECT**</summary>`selenium.grid.no.redirect`</details> | `false` | Whether to suppress capture of Grid server output to log files |
| <details><summary>**GRID_EXAMPLES**</summary>`selenium.grid.examples`</details> | `true` | Whether to install the `ExamplePageServlet` on the hub server |
| <details><summary>**GRID_LIFECYCLE**</summary>`selenium.grid.lifecycle`</details> | `true` | Whether to install the `LifecycleServlet` on hub and node servers (enables remote shutdown) |

---

## Hub Server

Settings that configure the Selenium Grid hub server.

| Setting | Default | Description |
|---------|---------|-------------|
| <details><summary>**HUB_CONFIG**</summary>`selenium.hub.config`</details> | <details><summary>*version-specific*</summary> **Selenium 3**: `hubConfig-s3.json`<br>**Selenium 4**: `hubConfig-s4.json`</details> | Hub server configuration file name or path. |
| <details><summary>**HUB_HOST**</summary>`selenium.hub.host`</details> | <details><summary>*version-specific*</summary>**Selenium 3**: `http://localhost:4445/wd/hub`<br>**Selenium 4**: `http://localhost:4446/wd/hub`</details> | URL for the Grid hub endpoint (`[scheme:][//authority]/wd/hub`). |
| <details><summary>**HUB_PORT**</summary>`selenuim.hub.port`</details> | <details><summary>*version-specific*</summary> **Selenium 3**: `4445`<br>**Selenium 4**: `4446`</details> | Port for the local hub server. |
| <details><summary>**HUB_DEBUG**</summary>`selenium.hub.debug`</details> | <details><summary>`false`</summary>Adds JDWP library to suspend the hub server on launch, listening at port 8000</details> | Whether to launch the hub server with JDWP debugging enabled |

---

## Node Server

Settings that configure Selenium Grid node servers.

| Setting | Default | Description |
|---------|---------|-------------|
| <details><summary>**NODE_CONFIG**</summary>`selenium.node.config`</details> | <details><summary>*version-specific*</summary>**Selenium 3**: `nodeConfig-s3.json`<br>**Selenium 4**: `nodeConfig-s4.json`</details> | Node server configuration template file name or path. |
| <details><summary>**NODE_DEBUG**</summary>`selenium.node.debug`</details> | <details><summary>`false`</summary>Adds JDWP library to suspend the node server on launch, listening at port 8001</details> | Whether to launch node servers with JDWP debugging enabled |

---

## Browser

Settings that specify the browser used for new WebDriver sessions.

| Setting | Default | Description |
|---------|---------|-------------|
| <details><summary>**BROWSER_NAME**</summary>`selenium.browser.name`</details> | *(none)* | Browser name or ["personality"](CustomizingCapabilities.md#selecting-browser-capabilities-by-personality) for new session requests |
| <details><summary>**BROWSER_CAPS**</summary>`selenium.browser.caps`</details> | *(none)* | <details><summary>Capabilities for new session requests. [Learn more...](CustomizingCapabilities.md#specifying-browser-capabilities-directly)</summary>Used when **BROWSER_NAME** is undefined. This setting may specify a file path (absolute, relative, or simple filename) or a JSON `Capabilities` object.</details> |

---

## Timeouts

Settings that control various wait and timeout intervals (all values in seconds).

| Setting | Default | Description |
|---------|---------|-------------|
| <details><summary>**PAGE_LOAD_TIMEOUT**</summary>`selenium.timeout.pageload`</details> | `30s` | Maximum number of seconds allowed for a page to finish loading |
| <details><summary>**IMPLIED_TIMEOUT**</summary>`selenium.timeout.implied`</details> | `15s` | Maximum number of seconds the driver will search for an element |
| <details><summary>**SCRIPT_TIMEOUT**</summary>`selenium.timeout.script`</details> | `30s` | Maximum number of seconds allowed for an asynchronous script to finish |
| <details><summary>**WAIT_TIMEOUT**</summary>`selenium.timeout.wait`</details> | `15s` | Maximum number of seconds to wait for a search context event |
| <details><summary>**HOST_TIMEOUT**</summary>`selenium.timeout.host`</details> | `30s` | Maximum number of seconds to wait for a Grid server to launch |

---

## Context

Settings that define the current test context.

| Setting | Default | Description |
|---------|---------|-------------|
| <details><summary>**CONTEXT_PLATFORM**</summary>`selenium.context.platform`</details> | `support` | Target platform for the current test context [Learn more...](TargetPlatformFeature.md#introduction) |
| <details><summary>**ALLOWED_EXCEPTIONS**</summary>`selenium.allowed.exceptions`</details> | *(none)* | <details><summary>Java exceptions that scripts are allowed to return [Learn more...](ScriptExecRuntime.md#allowed-exceptions)</summary>This setting specifies a comma-delimited list of exceptions (or packages/subtrees containing exceptions) that executed JavaScript snippets are allowed to return.</details> |

---

## Appium

Settings that configure the Appium mobile automation server.

| Setting | Default | Description |
|---------|---------|-------------|
| <details><summary>**APPIUM_CONFIG**</summary>`appium.config`</details> | *(none)* | <details><summary>`Appium` server configuration file name or path  [Learn more...](https://appium.io/docs/en/2.7/guides/config)</summary>The `Appium` configuration can be JSON, YAML, or JavaScript (for dynamic configuration)</details> |
| <details><summary>**APPIUM_CLI_ARGS**</summary>`appium.cli.args`</details> | *(none)* | <details><summary>Server arguments passed to `Appium` on launch.</summary>May be declared multiple times in `settings.properties`. For complex configurations, consider using **APPIUM_CONFIG** instead.</details> |
| <details><summary>**APPIUM_BINARY_PATH**</summary>`appium.binary.path`</details> | `$APPIUM_BINARY_PATH` env var | Path to the `Appium` main script file. If unset, the global Node package repository is searched. |
| <details><summary>**APPIUM_WITH_PM2**</summary>`appium.with.pm2`</details> | `false` | <details><summary>Whether `Appium` should be managed by the `PM2` process manager (required for standalone node)</summary>**NOTE**: `Appium` requires an active execution context. To run `Appium` as a stand-alone **Selenium Grid** node, the server must to executed as a daemon process. Starting the server via the `PM2` utility provides the required persistent execution context.</details> |

---

## Node.js / NPM

Settings that locate the Node.js runtime and associated package managers.

| Setting | Default | Description |
|---------|---------|-------------|
| <details><summary>**NODE_BINARY_PATH**</summary>`node.binary.path`</details> | `$NODE_BINARY_PATH` env var | Path to the Node.js JavaScript runtime. If unset, the system PATH is searched. |
| <details><summary>**NPM_BINARY_PATH**</summary>`npm.binary.path`</details> | *(none)* | Path to the `NPM` package manager utility. If unset, the system PATH is searched. |
| <details><summary>**PM2_BINARY_PATH**</summary>`pm2.binary.path`</details> | *(none)* | Path to the `PM2` process manager utility. If unset, the system PATH is searched. |
