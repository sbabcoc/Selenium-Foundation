# Introduction

The `Target Platform` feature of **Selenium Foundation** enables you to define a collection of "platforms" and associate each test with a specific platform. Prior to the start of each test, you have the chance to "[activate](#automatic-platform-activation)" the platform. Beyond these general characteristics, how you define "platform" and "activation" is entirely up to you.

Examples:
* You're testing a responsive web application and want to implement tests for different screen dimensions - SMALL | MEDIUM | LARGE
* You're testing a localized web application and want to verify language-specific scenarios - EN_US | FR_CA | ES_MX
* You're testing a web application that supports role-based access control - GUEST | USER | ADMIN

The collection of platforms, what they mean, and how they're activated is only limited by your imagination.

The `Target Platform` feature is supported for both [**TestNG**](#platform-targeting-for-testng) and [**JUnit 4**](#platform-targeting-for-junit-4). The platform-related operations available for your implementation are identical for both. Whichever test framework you choose, the first step is to define your `platform enumeration`.

## Defining Your Platform Enumeration

To define the collection of platforms upon which your tests will run, start by extending the **`PlatformEnum`** interface, defining the names of your platforms:

```java
package com.nordstrom.automation.selenium.platform;

interface PhaseName extends PlatformEnum {
    String PHASE1_NAME = "phase-1";
    String PHASE2_NAME = "phase-2";
    String PHASE3_NAME = "phase-3";
}
```

Next, declare a Java enumeration that implements this interface:

```java
package com.nordstrom.automation.selenium.platform;

public enum Transition implements PhaseName {
    PHASE1("green", PHASE1_NAME),
    PHASE2("amber", PHASE2_NAME),
    PHASE3("coral", PHASE3_NAME);
    
    private String color;
    private String name;
    
    Transition(String color, String name) {
        this.color = color;
        this.name = name;
    }
    
    public String getColor() {
        return color;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public boolean matches(String contextPlatform) {
        return name.equals(contextPlatform);
    }
}
```

This basic example declares three platforms: **PHASE1**, **PHASE2**, and **PHASE3**. The **`PlatformEnum`** interface defines two methods:

* `getName()` - Get the name of this platform constant.
* `matches(String)` - Determine if the specified context platform matches this constant.

The only property of each platform constant that you're required to define is its name. In real-world implementations, though, you'll probably add related information as well. In this example, each constant defines an associated color. Here are other possibilities:

* Screen dimensions
* Language codes
* Account credentials

Add fields to your enumeration for these related items, and methods with which to retrieve them. The values are declared as arguments of each constant, with a constructor to assign these values to their respective fields. Note that declaring the platform names as interface constants allows them to be used to populate the enumeration and in declarations of the `@TargetPlatform` annotation (see examples below). This avoids the need to hardcode platform names in the annotations or declare them twice in the enumeration.

## Automatic Platform Activation

As indicated previously, **Selenium Foundation** provides the opportunity to activate the target platform prior to the start of each test. To enable this automatic activation behavior, specify the initial page that your tests will interact with via the `@InitialPage` annotation. By specifying the initial page, you can be certain that the driver is launched and has opened your target site, ready for action.

The examples shown below for [**TestNG**](#platform-targeting-for-testng) and [**JUnit 4**](#platform-targeting-for-junit-4) both specify the initial page to open at the beginning of each test. Their platform activation methods store the name of the target platform in a session cookie. In your own implementation, perform whatever operation are needed to activate your platform (e.g. - change screen dimensions).

## Platform Targeting for TestNG

The easiest way to enable target platform support for **TestNG** tests is to subclass **`TestNgPlatformBase`**. This class extends **`TestNgBase`** and defines all of the elements required to activate the `Target Platform` feature:

* Implementation of the **`PlatformTargetable<P>`** interface:
    * `TestNgPlatformBase(Class<P>)` constructor
    * `getSubPath()` method
    * `getTargetPlatform()` method
    * `activatePlatform(WebDriver)` method
    * `activatePlatform(WebDriver, P)` stub
    * `getValidPlatforms()` method
    * `platformFromString(String)` method
    * `getPlatformType()` method
* Attachment of **`PlatformInterceptor`** to the listener chain

This last element refers to the [`listener chain`](TestNGSupport#testng-required-configuration), which is a core **TestNG** listener that must be activated to drive fundamental aspects of **Selenium Foundation**. Additional [required elements](TestNGSupport#testng-required-elements) are defined by the **`TestNgBase`** class.

```java
package com.nordstrom.automation.selenium.support;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.exceptions.PlatformActivationFailedException;
import com.nordstrom.automation.selenium.model.ExamplePage;
import com.nordstrom.automation.selenium.platform.Transition;
import com.nordstrom.automation.selenium.platform.TargetPlatform;

@InitialPage(ExamplePage.class)
public class TestNgPlatformTest extends TestNgPlatformBase<Transition> {

    public TestNgPlatformTest() {
        super(Transition.class);
    }
    
    @Test
    public void testDefaultPlatform() {
        assertTrue(getTargetPlatform().matches(Transition.PHASE1_NAME));
        assertEquals(getTargetPlatform().getColor(), "green");
    }
    
    @Test
    @TargetPlatform(Transition.PHASE2_NAME)
    public void testPlatformTwo() {
        assertTrue(getTargetPlatform().matches(Transition.PHASE2_NAME));
        assertEquals(getTargetPlatform().getColor(), "amber");
    }
    
    @Override
    public void activatePlatform(WebDriver driver, Transition platform)
            throws PlatformActivationFailedException {
        driver.manage().addCookie(new Cookie("color", platform.getColor()));
    }

    @Override
    public Transition getDefaultPlatform() {
        return Transition.PHASE1;
    }
}
```

### Acquiring the Current Test

Given the intentionally generic nature of the `Target Platform` feature, no direct reference is provided to the method for which platform activation is being performed. However, this is easily acquired:

```java
...

    @Override
    public void activatePlatform(WebDriver driver, Transition platform)
            throws PlatformActivationFailedException {
        ITestResult result = Reporter.getCurrentTestResult();
        if (result != null) {
            ITestNgMethod method = result.getMethod();
            ...
        }
    }

...
```

## Platform Targeting for JUnit 4

The easiest way to enable target platform support for **JUnit 4** tests is to subclass **`JUnitPlatformBase`**. This class extends **`JUnitBase`** and defines all of the elements required to activate the `Target Platform` feature:

* Implementation of the **`PlatformTargetable<P>`** interface:
    * `JUnitPlatformBase(Class<P>)` constructor
    * `getSubPath()` method
    * `getTargetPlatform()` method
    * `activatePlatform(WebDriver)` method
    * `activatePlatform(WebDriver, P)` stub
    * `getValidPlatforms()` method
    * `platformFromString(String)` method
    * `getPlatformType()` method
* Instantiation of **`TargetPlatformRule`**

Operations of the `Target Platform` feature are driven by [`run watchers`](JUnit4Support#junit-4-required-configuration), which must be connected to drive fundamental aspects of **Selenium Foundation**. These run watchers are in turn driven by test lifecycle events hooked into the core of **JUnit 4** by the **JUnit Foundation** [Java agent](JUnit4Support#outline-of-required-elements).

```java
package com.nordstrom.automation.selenium.junit;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;

import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.exceptions.PlatformActivationFailedException;
import com.nordstrom.automation.selenium.model.ExamplePage;
import com.nordstrom.automation.selenium.platform.Transition;
import com.nordstrom.automation.selenium.platform.TargetPlatform;

@InitialPage(ExamplePage.class)
public class JUnitPlatformTest extends JUnitPlatformBase<Transition> {

    public JUnitPlatformTest() {
        super(Transition.class);
    }
    
    @Test
    public void testDefaultPlatform() {
        assertTrue(getTargetPlatform().matches(Transition.PHASE1_NAME));
        assertEquals("green", getTargetPlatform().getColor());
    }
    
    @Test
    @TargetPlatform(Transition.PHASE2_NAME)
    public void testPlatformTwo() {
        assertTrue(getTargetPlatform().matches(Transition.PHASE2_NAME));
        assertEquals("amber", getTargetPlatform().getColor());
    }
    
    @Override
    public void activatePlatform(WebDriver driver, Transition platform)
            throws PlatformActivationFailedException {
        driver.manage().addCookie(new Cookie("color", platform.getColor()));
    }

    @Override
    public Transition getDefaultPlatform() {
        return Transition.PHASE1;
    }
}
```

### Acquiring the Current Test

Given the intentionally generic nature of the `Target Platform` feature, no direct reference is provided to the method for which platform activation is being performed. However, this is easily acquired:

```java
...

    @Override
    public void activatePlatform(WebDriver driver, Transition platform)
            throws PlatformActivationFailedException {
        AtomicTest atomicTest = LifecycleHooks.getAtomicTestOf(this);
        FrameworkMethod method = atomicTest.getIdentity();
        ...
    }

...
```

> Written with [StackEdit](https://stackedit.io/).
