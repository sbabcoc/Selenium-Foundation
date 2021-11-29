# Introduction

In addition to its TestNG support, **Selenium Foundation** also includes support for **JUnit 4**. This support is built upon **JUnit Foundation**, which provides the framework for method interception (used for driver management), artifact capture (used to acquire screenshots and page source), and automatic retry of failed tests.

## JUnit 4 Required Configuration

* Add the [**JUnit Foundation** Java agent](https://github.com/sbabcoc/JUnit-Foundation#how-to-enable-notifications) to your project's test configuration. You'll find more details about this [below](#outline-of-required-elements).
  * **NOTE**: The `-javaagent` specification you add to your project configuration may not be active in all contexts. For example, you'll probably find that this option must also be added to your [IDE run/debug configurations](https://github.com/sbabcoc/JUnit-Foundation#ide-configuration-for-junit-foundation).
* Add a service loader run watcher configuration file in your project's **_resources/META-INF/services_** folder:

###### com.nordstrom.automation.junit.JUnitWatcher
```
com.nordstrom.automation.selenium.junit.DriverListener
com.nordstrom.automation.selenium.junit.DriverWatcher
```

## JUnit 4 Required Elements

There are several required elements that must be included in every JUnit 4 test class to activate the features of **Selenium Foundation**. To assist you in this process, we've included the [JUnitBase](../src/main/java/com/nordstrom/automation/selenium/junit/JUnitBase.java) class as a starter. This class includes all of the required elements outlined below.

**JUnitBase** is an abstract class that implements the [TestBase](../src/main/java/com/nordstrom/automation/selenium/core/TestBase.java) interface, which provides a common abstraction for both TestNG and JUnit 4 tests.

### Outline of Required Elements

The following is an outline of the elements that must be included in every JUnit 4 project that uses **Selenium Foundation**:

* **JUnit Foundation** [event notifications](https://github.com/sbabcoc/JUnit-Foundation#how-to-enable-notifications):  
The JUnit support provided by **Selenium Foundation** relies on event notifications published by **JUnit Foundation**. Notifications are enabled by a Java agent, which uses bytecode enhancement to install hooks on test and configuration methods.
###### Maven configuration for Java agent:
```xml
    <plugins>
      <!-- This provides the path to the Java agent -->
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-dependency-plugin</artifactId>
        <version>3.1.1</version>
        <executions>
          <execution>
            <id>getClasspathFilenames</id>
            <goals>
              <goal>properties</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>2.22.0</version>
        <configuration>
          <argLine>-javaagent:${com.nordstrom.tools:junit-foundation:jar}</argLine>
        </configuration>
      </plugin>
    </plugins>
```  
###### Gradle configuration for Java agent:  
```gradle
test {
    jvmArgs "-javaagent:${classpath.find { it.name.contains('junit-foundation') }.absolutePath}"
}
```
###### Gradle configuration (Android Studio):  
```gradle
android {
    testOptions {
        unitTests.all {
            jvmArgs "-javaagent:${classpath.find { it.name.contains('junit-foundation') }.absolutePath}"
        }
    }
}
```

* [DriverWatcher](../src/main/java/com/nordstrom/automation/selenium/junit/DriverWatcher.java):  
Activated by the service loader configuration specified [above](#junit-4-required-configuration), **DriverWatcher** implements the **JUnit Foundation**  [MethodWatcher](https://github.com/sbabcoc/JUnit-Foundation/blob/master/src/main/java/com/nordstrom/automation/junit/MethodWatcher.java) interface to manage driver sessions. It provides initial page support, and it also supplies a JUnit 4 [TestWatcher](https://junit.org/junit4/javadoc/4.12/org/junit/rules/TestWatcher.html):
  * **`DriverWatcher.getTestWatcher()`**:  
  The test rule returned by this static method is responsible for closing the driver attached to the current test method.
* [DriverListener](../src/main/java/com/nordstrom/automation/selenium/junit/DriverListener.java):  
Activated by the service loader configuration specified [above](#junit-4-required-configuration), **DriverListener** implements the **JUnit Foundation** [ShutdownListener](https://github.com/sbabcoc/JUnit-Foundation/blob/master/src/main/java/com/nordstrom/automation/junit/ShutdownListener.java) interface to shut down the local Selenium Grid servers at the end of the run.
* [RuleChain](http://junit.org/junit4/javadoc/latest/org/junit/rules/RuleChain.html):  
 Use **RuleChain** for attaching test rules that must be applied in a specific order. The [JUnitBase](../src/main/java/com/nordstrom/automation/selenium/junit/JUnitBase.java) class includes a **RuleChain** that specifies three watchers that manage core features of **Selenium Foundation**:
  * [ScreenshotCapture](../src/main/java/com/nordstrom/automation/selenium/junit/ScreenshotCapture.java):  
  **ScreenshotCapture** is a JUnit 4 test watcher that automatically captures a screenshot in the event of test failure.
  * [PageSourceCapture](../src/main/java/com/nordstrom/automation/selenium/junit/PageSourceCapture.java):  
  **PageSourceCapture** is a JUnit 4 test watcher that automatically captures page source in the event of test failures.
  * [DriverWatcher](../src/main/java/com/nordstrom/automation/selenium/junit/DriverWatcher.java):  
  As described previously, the test watcher returned by `DriverWatcher.getTestWatcher()` closes the driver attached to the current test method.

## Automatic Retry of Failed Tests

**Selenium Foundation** includes an implementation of the [JUnitRetryAnalyzer](https://github.com/sbabcoc/JUnit-Foundation/blob/master/src/main/java/com/nordstrom/automation/junit/JUnitRetryAnalyzer.java) interface of [JUnit Foundation](https://github.com/sbabcoc/JUnit-Foundation). This retry analyzer considers any test that fails due to a **WebDriverException** to be retriable. By default, this retry analyzer is disabled. To enable automatic retry of **WebDriverException** failures:

* Add a service loader retry analyzer configuration file in the **_META-INF/services_** folder:

###### com.nordstrom.automation.junit.JUnitRetryAnalyzer
```
com.nordstrom.automation.selenium.junit.RetryAnalyzer
```

* Specify a positive value for the **MAX_RETRY** setting of **JUnit Foundation**:

| _junit.properties_ |
| --- |
| junit.max.retry=2 |

In this example, these two configurations will enable **JUnit Foundation** to retry tests that fail with **WebDriverException** twice before counting them as failures. See the [JUnit Foundation](https://github.com/sbabcoc/JUnit-Foundation#automatic-retry-of-failed-tests) documentation for more details.

## Demonstrated Features

The **JUnitBase** class demonstrates several features of the **Selenium Foundation** API:

* **`TestBase.optionalOf(Object)`**:  
This static utility method wraps the specified object in an [Optional](https://google.github.io/guava/releases/19.0/api/docs/com/google/common/base/Optional.html) object. If the object to be wrapped is 'null', this method returns an empty optional.
* **`RuleChainWalker.getAttachedRule(RuleChain, Class<T extends TestRule>)`**:  
This static utility method gets reference to an instance of the specified test rule type on the supplied rule chain. In **JUnitBase**, this is used to acquire a reference to the **ScreenshotCapture** watcher for capturing on-demand screenshot artifacts.
* **`ScreenshotCapture.getAtomIdentity()`**:  
This instance method of **ScreenshotCapture** enables test code to acquire the [AtomIdentity](https://github.com/sbabcoc/JUnit-Foundation/blob/master/src/main/java/com/nordstrom/automation/junit/AtomIdentity.java) object for the current JUnit 4 test method. This object can be interrogated for many useful propeties of the test method, including test class instance, **Description** object, and instance parameters.
* **`ScreenshotCapture.getDescription()`**:  
This instance method of **ScreenshotCapture** enables test code to acquire the [Description](http://junit.org/junit4/javadoc/latest/org/junit/runner/Description.html) object for the current JUnit 4 test method. This object can be interrogated for many useful propeties of the test method, including method name, attached annotations, and containing class.

## Driver Acquisition and Hand-Off

In the preceding section, driver sessions are acquired automatically for each test or requested implicitly by applying the **`@InitialPage`** annotation. The core functionality used to initiate driver sessions implicitly can also be invoked ad hoc to acquire drivers explicitly:

```java
WebDriver driver = GridUtility.getDriver();
```

This method uses the configured settings for Selenium Grid and desired browser from the current test execution context to instantiate a new driver session.

If the **`@InitialPage`** annotation is applied to a **`@Before`** configuration method, the driver instantiated for this method is automatically handed off to the test that follows. The initial page as specified for the configuration method is handed off as well. If actions performed by your configuration method trigger page transitions, you need to store the final page accessed by the configuration method as the initial page for the test method:

```java
@Before
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
