# Selenium 2.53.1 Version Matrix

## Chrome (`chrome`)

| Application | Driver |
|---|---|
| 71.0.3578.98 (latest) | [2.45](https://chromedriver.storage.googleapis.com/index.html?path=2.45/) |

```
<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>selenium-chrome-driver</artifactId>
    <version>2.53.1</version>
</dependency>
``` 

## Edge (`MicrosoftEdge`)

| Application | Driver |
|---|---|
| ? | ? |

```
<dependency>
  <groupId>org.seleniumhq.selenium</groupId>
  <artifactId>selenium-edge-driver</artifactId>
  <version>2.53.1</version>
</dependency>
```

## Firefox (`firefox`)

| Application | Driver |
|---|---|
| [47.0.1](https://ftp.mozilla.org/pub/firefox/releases/47.0.1/) | N/A |

```
<dependency>
  <groupId>org.seleniumhq.selenium</groupId>
  <artifactId>selenium-firefox-driver</artifactId>
  <version>2.53.1</version>
</dependency>
```

## HtmlUnit (`htmlunit`)

**HtmlUnit** is a headless browser composed entirely of Maven dependencies. No application or driver binaries exist for **HtmlUnit**.

```
<dependency>
  <groupId>net.sourceforge.htmlunit</groupId>
  <artifactId>htmlunit</artifactId>
  <version>2.21</version>
</dependency>
<dependency>
  <groupId>org.seleniumhq.selenium</groupId>
  <artifactId>htmlunit-driver</artifactId>
  <version>2.21</version>
</dependency>
```

## Internet Explorer (`internet explorer`)

| Application | Driver |
|---|---|
| [11.0.9600.19129CO](http://g.msn.com/1me10IE11ENUS/WOL_Win7_64Full) | [2.53.1](https://selenium-release.storage.googleapis.com/index.html?path=2.53/) |

```
<dependency>
  <groupId>org.seleniumhq.selenium</groupId>
  <artifactId>selenium-ie-driver</artifactId>
  <version>2.53.1</version>
</dependency>
```

## Opera (`operablink`)

| Application | Driver |
|---|---|
| [40.0.2308.90](https://ftp.opera.com/pub/opera/desktop/40.0.2308.90/) | [0.2.2](https://github.com/operasoftware/operachromiumdriver/releases/tag/v0.2.2) |

```
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

```
<dependency>
  <groupId>com.codeborne</groupId>
  <artifactId>phantomjsdriver</artifactId>
  <version>1.3.0</version>
</dependency>
```

## Safari (`safari`)

| Application | Driver |
|---|---|
| ? | ? |

```
<dependency>
  <groupId>org.seleniumhq.selenium</groupId>
  <artifactId>selenium-safari-driver</artifactId>
  <version>2.53.1</version>
</dependency>
```
