# Introduction

In addition to its TestNG and JUnit 4 support, **Selenium Foundation** also includes support for **JUnit 5** (**Jupiter**). This support is built upon [**Jupiter Foundation**](https://github.com/sbabcoc/Jupiter-Foundation), a peer to **JUnit Foundation** and **TestNG Foundation** that provides automatic retry of failed tests, artifact capture (used to acquire screenshots and page source), and resolved-argument capture for parameterized tests.

**NOTE**: Unlike **JUnit Foundation**, **Jupiter Foundation** requires no Java agent and no bytecode instrumentation. JUnit 5's own extension model already provides first-class hooks (`InvocationInterceptor`, `TestWatcher`, `ExecutionCondition`) for everything **JUnit Foundation** had to build by hand for JUnit 4.

## Jupiter Required Configuration

* Add a **JUnit Platform** test execution listener service loader configuration file in your project's **_resources/META-INF/services_** folder:

###### org.junit.platform.launcher.TestExecutionListener
```
com.nordstrom.automation.selenium.jupiter.JupiterGridListener
```

* Ensure your build runs Jupiter tests through the **JUnit Platform** launcher. For Gradle, this means a `Test` task with `useJUnitPlatform()` configured:

###### Gradle configuration:
```gradle
tasks.register('testJupiter', Test) {
  useJUnitPlatform()
  testClassesDirs = sourceSets.test.output.classesDirs
  classpath = sourceSets.test.runtimeClasspath
}
```

**NOTE**: This is a genuinely separate concern from the **JUnit Foundation** Java agent configuration described in the [JUnit 4 Support](JUnit4Support.md#introduction) documentation — a project that supports both JUnit 4 and JUnit 5 test suites needs both configurations, typically as separate Gradle `Test` tasks (see **Selenium Foundation**'s own `build.gradle` for a working three-framework example: `testNG`, `test` for JUnit 4, and `testJupiter`).

## Jupiter Required Elements

There are several required elements that must be included in every JUnit 5 test class to activate the features of **Selenium Foundation**. To assist you in this process, we've included the [JupiterBase](https://github.com/sbabcoc/Selenium-Foundation/tree/master/src/main/java/com/nordstrom/automation/selenium/jupiter/JupiterBase.java) class (for `PER_METHOD` test instance lifecycle) and [JupiterClassBase](https://github.com/sbabcoc/Selenium-Foundation/tree/master/src/main/java/com/nordstrom/automation/selenium/jupiter/JupiterClassBase.java) class (for `PER_CLASS` lifecycle) as starters. These classes include all of the required elements outlined below.

**JupiterBase**/**JupiterClassBase** are abstract classes that implement the [TestBase](https://github.com/sbabcoc/Selenium-Foundation/tree/master/src/main/java/com/nordstrom/automation/selenium/core/TestBase.java) interface, the same common abstraction shared by TestNG and JUnit 4 tests.

### Outline of Required Elements

The following elements are declared as `@RegisterExtension` fields on [JupiterTestBase](https://github.com/sbabcoc/Selenium-Foundation/tree/master/src/main/java/com/nordstrom/automation/selenium/jupiter/JupiterTestBase.java), the shared parent of `JupiterBase`/`JupiterClassBase`:

* [DriverWatcher](https://github.com/sbabcoc/Selenium-Foundation/tree/master/src/main/java/com/nordstrom/automation/selenium/jupiter/DriverWatcher.java):  
Implements JUnit 5's `InvocationInterceptor` interface to manage driver sessions around every test invocation. Unlike JUnit 4's `DriverWatcher` (which needed a separately-returned `TestWatcher` for driver cleanup), a single interceptor wraps the entire invocation lifecycle - setup, the invocation itself, and cleanup - in one place, via a `try`/`finally` block. This is also where target platform filtering is resolved and checked (see the [Target Platform Feature](TargetPlatformFeature.md#introduction) documentation), rather than through a separately-registered `ExecutionCondition` - `ExtensionContext.getTestInstance()` is not reliably populated at method-level `ExecutionCondition` evaluation time under `PER_METHOD` lifecycle, so target platform resolution needs the live instance `DriverWatcher` already has.
* [PageSourceCapture](https://github.com/sbabcoc/Selenium-Foundation/tree/master/src/main/java/com/nordstrom/automation/selenium/jupiter/PageSourceCapture.java) / [ScreenshotCapture](https://github.com/sbabcoc/Selenium-Foundation/tree/master/src/main/java/com/nordstrom/automation/selenium/jupiter/ScreenshotCapture.java):  
Built on **Jupiter Foundation**'s `ArtifactCollector`, these automatically capture page source and a screenshot, respectively, on test failure. Unlike JUnit 4's `Rule`-based watchers (which require explicit ordering via `@Rule(order=...)` or `RuleChain`), these are plain `@RegisterExtension` fields - no ordering declaration is needed between them and `DriverWatcher`, since neither depends on the other's side effects.
* [SeleniumRetryExtension](https://github.com/sbabcoc/Selenium-Foundation/tree/master/src/main/java/com/nordstrom/automation/selenium/jupiter/SeleniumRetryExtension.java):  
Extends **Jupiter Foundation**'s `RetryExtension`, re-applying driver-lifecycle setup/teardown on every retry attempt beyond the first (a consequence of JUnit 5's `Invocation.proceed()` contract being callable only once - see `RetryExtension`'s own javadoc for the full explanation).
* [JupiterGridListener](https://github.com/sbabcoc/Selenium-Foundation/tree/master/src/main/java/com/nordstrom/automation/selenium/jupiter/JupiterGridListener.java):  
Activated by the service loader configuration specified [above](#jupiter-required-configuration), this manages local Selenium Grid startup/shutdown for the whole test run - the Jupiter-native equivalent of the JUnit 4 `GridWatcher`/`DriverListener` pair. Unlike those two, no root-runner detection is needed - `TestExecutionListener.testPlanExecutionStarted`/`Finished` are guaranteed by the JUnit Platform Launcher to each fire exactly once per session.

## Automatic Retry of Failed Tests

**Selenium Foundation** provides [SeleniumRetryExtension](https://github.com/sbabcoc/Selenium-Foundation/tree/master/src/main/java/com/nordstrom/automation/selenium/jupiter/SeleniumRetryExtension.java), which re-applies driver setup/teardown on every retry attempt. Retry itself is governed by **Jupiter Foundation**'s own settings:

* Specify a positive value for the **MAX_RETRY** setting:

| _jupiter.properties_ |
| --- |
| jupiter.max.retry=2 |

* Register at least one [JupiterRetryAnalyzer](https://github.com/sbabcoc/Jupiter-Foundation/blob/master/src/main/java/com/nordstrom/automation/jupiter/JupiterRetryAnalyzer.java) implementation that approves the failure as retriable - retry is opt-in, not automatic, even with `MAX_RETRY` configured.

**NOTE**: Unlike the JUnit 4 integration, which supplies a ready-made `JUnitRetryAnalyzer` that specifically approves `WebDriverException` failures, **Selenium Foundation** does not yet provide an equivalent `JupiterRetryAnalyzer` implementation. Until one is added, you'll need to supply your own analyzer (via `META-INF/services/com.nordstrom.automation.jupiter.JupiterRetryAnalyzer`) to enable retry for Jupiter test classes.

## Demonstrated Features

The **JupiterBase**/**JupiterClassBase** classes demonstrate several features of the **Selenium Foundation** API:

* **`TestBase.optionalOf(Object)`**:  
This static utility method wraps the specified object in an [Optional](https://google.github.io/guava/releases/19.0/api/docs/com/google/common/base/Optional.html) object. If the object to be wrapped is 'null', this method returns an empty optional.
* **`JupiterTestBase.getExtensionContext()`**:  
This instance method enables test code to acquire the current test's `ExtensionContext` - the Jupiter-native analog to JUnit 4's `AtomIdentity`/`Description` objects, exposing the current test method, display name, and containing class.

## Driver Acquisition and Hand-Off

As with the other supported frameworks, driver sessions are acquired automatically for each test, or requested implicitly by applying the **`@InitialPage`** annotation. The core functionality used to initiate driver sessions implicitly can also be invoked ad hoc to acquire drivers explicitly:

```java
WebDriver driver = GridUtility.getDriver();
```

If the **`@InitialPage`** annotation is applied to a **`@BeforeEach`** configuration method, the driver instantiated for this method is automatically handed off to the test that follows:

```java
@BeforeEach
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

## Known Limitations

This is a newer integration than the TestNG and JUnit 4 support, and a few gaps are worth knowing about directly:

* **No `PER_CLASS` target-platform support yet.** `JupiterPlatformBase` (the `PER_METHOD` target-platform base class) exists; a `PER_CLASS` equivalent has not yet been built.
* **No Selenium-specific `JupiterRetryAnalyzer`.** See the note under [Automatic Retry of Failed Tests](#automatic-retry-of-failed-tests) above.
* **Most of `JupiterModelTest`'s coverage is intentionally `@Disabled`**, mirroring `JUnitModelTest`'s own scope - the same underlying page-object-model behavior is already exercised via the TestNG suite (`ModelTest`), and running it a third time added significant build time with no added coverage. Only `testBasicPage()` runs directly under Jupiter.

> Written with [StackEdit](https://stackedit.io/).
