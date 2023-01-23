
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
| Eclipse (IDE) | 2020-12 (4.18.0) |
| BuildShip Plug-In | 3.1.5.v20210113-0904 |
| TestNG Plug-In | 7.3.0.202011271758 |

## Java Versions

To build **Selenium Foundation**, you'll need a Java 8 software development kit:

* Java 8: version 1.8.0_311

## Gradle Configuration:

The Gradle project file for **Selenium Foundation** includes a **selenium3Deps** sub-file. This reflects the past (and probable future) support for multiple versions of the Selenium API. 

### JDK Toolchains

To build the **Selenium Foundation** project, you'll also need to add a **Maven** `toolchain` specification to your system configuration. This configuration allows **Gradle** to auto-discover the JDK installations on your machine:

###### C:\\Users\\&lt;username&gt;\\.m2\\toolchains.xml
```xml
<?xml version="1.0" encoding="UTF8"?>
<toolchains>
  <!-- JDK toolchains -->
  <toolchain>
    <type>jdk</type>
    <provides>
      <version>8</version>
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
| **`JDK8_HOME`** | Root folder of JDK 8 installation |

## JUnit Run Configuration

The JUnit support provided by **Selenium Foundation** relies on [event notifications](JUnit4Support.md#outline-of-required-elements) published by **JUnit Foundation**. Notifications are enabled by a Java agent, which uses bytecode enhancement to install hooks on test and configuration methods.

To run or debug **JUnit 4** tests in **Eclipse**, specify the `-javaagent` option in the `VM arguments` field on the `Arguments` tab of the **JUnit** run configuration:

> -javaagent:&lt;maven-repo&gt;/repository/com/nordstrom/tools/junit-foundation/16.0.1/junit-foundation-16.0.1.jar

> Written with [StackEdit](https://stackedit.io/).
