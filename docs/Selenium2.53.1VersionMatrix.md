# Selenium 2.53.1 Version Matrix

## Chrome (`chrome`)

| Application | Driver |
|---|---|
| 87.0.4280.88 | [87.0.4280.88](https://chromedriver.storage.googleapis.com/index.html?path=87.0.4280.88/) |

```xml
<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>selenium-chrome-driver</artifactId>
    <version>2.53.1</version>
</dependency>
``` 

## Edge (`MicrosoftEdge`)

| Application | Driver |
|---|---|
| 87.0.664.66 | [87.0.664.66 (x64)](https://msedgedriver.azureedge.net/87.0.664.66/edgedriver_win64.zip) |

```xml
<dependency>
  <groupId>org.seleniumhq.selenium</groupId>
  <artifactId>selenium-edge-driver</artifactId>
  <version>2.53.1</version>
</dependency>
```

## Firefox (`firefox`)

| Application | Driver |
|---|---|
| [84.0.1](https://ftp.mozilla.org/pub/firefox/releases/84.0.1/) | N/A |

```xml
<dependency>
  <groupId>org.seleniumhq.selenium</groupId>
  <artifactId>selenium-firefox-driver</artifactId>
  <version>2.53.1</version>
</dependency>
```

## HtmlUnit (`htmlunit`)

**HtmlUnit** is a headless browser composed entirely of Maven dependencies. No application or driver binaries exist for **HtmlUnit**.

```xml
<dependency>
  <groupId>net.sourceforge.htmlunit</groupId>
  <artifactId>htmlunit</artifactId>
  <version>2.22</version>
</dependency>
<dependency>
  <groupId>org.seleniumhq.selenium</groupId>
  <artifactId>htmlunit-driver</artifactId>
  <version>2.22</version>
</dependency>
```

## Internet Explorer (`internet explorer`)

| Application | Driver |
|---|---|
| [11.00.9600.16428](https://www.microsoft.com/en-us/download/confirmation.aspx?id=41628) | [2.53.1](https://selenium-release.storage.googleapis.com/index.html?path=2.53/) |

```xml
<dependency>
  <groupId>org.seleniumhq.selenium</groupId>
  <artifactId>selenium-ie-driver</artifactId>
  <version>2.53.1</version>
</dependency>
```

## Opera (`operablink`)

| Application | Driver |
|---|---|
| [73.0.3856.284](https://ftp.opera.com/pub/opera/desktop/73.0.3856.284/) | [87.0.4280.67](https://github.com/operasoftware/operachromiumdriver/releases/tag/v.87.0.4280.67) |

```xml
<!-- NOTE: The Selenium Foundation project includes this artifact -->
<dependency>
  <groupId>org.seleniumhq.selenium</groupId>
  <artifactId>selenium-opera-driver</artifactId>
  <version>2.53.1</version>
</dependency>
```

## PhantomJS (`phantomjs`)

**PhantomJS** is a headless browser packaged as a command line utility. No application binary exists for **PhantomJS**.

| Application | Driver |
|---|---|
| N/A | [2.1.1](https://bitbucket.org/ariya/phantomjs/downloads/) |

```xml
<dependency>
  <groupId>com.codeborne</groupId>
  <artifactId>phantomjsdriver</artifactId>
  <version>1.3.0</version>
</dependency>
```

## Safari (`safari`)

| Application | Driver |
|---|---|
| 14.0.2 | N/A |

```xml
<dependency>
  <groupId>org.seleniumhq.selenium</groupId>
  <artifactId>selenium-safari-driver</artifactId>
  <version>2.53.1</version>
</dependency>
```
