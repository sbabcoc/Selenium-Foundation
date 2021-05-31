
# Introduction

Building the **Selenium Foundation** project requires a few standard tools and a few basic configurations. For executing tests in **Eclipse**, there are also configuration options you'll need to adjust for **TestNG** and **JUnit**. The information on this page should have you up and running in short order.

## Developer Tools

The majority of the environment needed to build **Selenium Foundation** is comprised of standard tools installed with default settings:

| Utility / System | Details |
|---:|:---|
| Package manager | Chocolatey 0.10.8 |
| Version control | git version 2.27 |
| Command shell | GNU bash, version 4.4.23(1) |
| Gradle (build tool) | version 6.5 |
| Maven (build tool) | version 3.5.2 |
| Eclipse (IDE) | 2020-12 (4.18.0) |
| Maven Plug-In | m2e 1.17.1.20201207-1112 |
| BuildShip Plug-In | 3.1.5.v20210113-0904 |
| TestNG Plug-In | 7.3.0.202011271758 |

## Java Versions

To build both versions of **Selenium Foundation** requires two Java development kits:

* Java 7: version 1.7.0_80
* Java 8: version 1.8.0_251

## Gradle Configuration:

The Gradle project file for **Selenium Foundation** is configured to build the **selenium3** variant by default. If you want to build the **selenium2** variant, create a run configuration that includes the `clean` and `build` tasks, and add the following program argument on the `Project Settings` tab:

> `-Pprofile=selenium2`

## Maven Configuration:

### Project Profiles

The Maven POM for **Selenium Foundation** defined two mutually-exclusive profiles (**selenium2** and **selenium3**), neither of which is active by default. Select one or the other to finalize the source set. For example:

> Project > Properties > Maven > Active Maven Profiles: [selenium3]

### JDK Toolchains

To build the **Selenium Foundation** project with **Maven**, you'll also need to add a `toolchain` specification to your system configuration:

###### C:\\Users\\&lt;username&gt;\\.m2\\toolchains.xml
```xml
<?xml version="1.0" encoding="UTF8"?>
<toolchains>
  <!-- JDK toolchains -->
  <toolchain>
    <type>jdk</type>
    <provides>
      <version>7</version>
      <vendor>oracle</vendor>
    </provides>
    <configuration>
      <jdkHome>C:\Program Files\Java\jdk1.7.0_80</jdkHome>
    </configuration>
  </toolchain>
  <toolchain>
    <type>jdk</type>
    <provides>
      <version>8</version>
      <vendor>oracle</vendor>
    </provides>
    <configuration>
      <jdkHome>C:\Program Files\Java\jdk1.8.0_251</jdkHome>
    </configuration>
  </toolchain>
</toolchains>
```

## Environment Variables

| Variable | Target |
|:---|:---|
| **`GRADLE_HOME`** | Root folder of Gradle installation |
| **`M2`** | Path to Maven `bin` folder
| **`M2_HOME`** | Root folder of Maven installation |
| **`M2_REPO`** | Root folder of Maven repository |
| **`JDK7_HOME`** | Root folder of JDK 7 installation |
| **`JDK8_HOME`** | Root folder of JDK 8 installation |

## TestNG Eclipse Configuration

In the **Selenium Foundation** POM file, the configuration for the Maven Surefire plug-in declares an `argLine`  property that specifies the `-javaagent` option. The declaration of this option isn't needed to run or debug **TestNG** tests and may cause test execution to fail.

To resolve this issue, disable the option to pass the `argLine` property to **TestNG** in your **Eclipse** preferences:

> Preferences > TestNG > Maven > &#9744; argLine

## JUnit Run Configuration

The JUnit support provided by **Selenium Foundation** relies on [event notifications](JUnit4Support.md#outline-of-required-elements) published by **JUnit Foundation**. Notifications are enabled by a Java agent, which uses bytecode enhancement to install hooks on test and configuration methods.

To run or debug **JUnit 4** tests in **Eclipse**, specify the `-javaagent` option in the `VM arguments` field on the `Arguments` tab of the **JUnit** run configuration:

> -javaagent:&lt;maven-repo&gt;/repository/com/nordstrom/tools/junit-foundation/12.5.0/junit-foundation-12.5.0.jar

> Written with [StackEdit](https://stackedit.io/).