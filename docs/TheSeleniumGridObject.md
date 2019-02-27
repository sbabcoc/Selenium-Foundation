## THE **`SeleniumGrid`** OBJECT

The **`SeleniumGrid`** object provides an interface to [Selenium Grid](https://github.com/SeleniumHQ/selenium/wiki/Grid2) collections - both local and remote. A standard grid object is available through the configuration, and independent instances can be created as needed.

### Using the standard **`SeleniumGrid`** object

By default, **Selenium Foundation** acquires its browser sessions from an instance of the [Selenium Grid](https://seleniumhq.github.io/docs/grid.html). If no remote Grid instance is specified in your project's configuration, **Selenium Foundation** will launch and manage a local instance for you.

As stated in the main [README](../README.md#grid-based-driver-creation) file, **Selenium Foundation** acquires local browser sessions from a local Grid instance to avoid divergent behavior and special-case code to support both local and remote operation.

* Methods
* Settings
* Sample code

### Creating independent **`SeleniumGrid`** objects

* Introduction
* Methods
* Sample code
