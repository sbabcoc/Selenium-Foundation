package com.nordstrom.automation.selenium;

import static org.testng.Assert.assertEquals;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.github.sbabcoc.logback.testng.ReporterAppender;
import com.nordstrom.automation.selenium.SeleniumConfig.SeleniumSettings;
import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.listeners.DriverManager;
import com.nordstrom.automation.selenium.model.ExamplePage;
import com.nordstrom.automation.testng.ExecutionFlowController;
import com.nordstrom.automation.testng.ListenerChain;
import com.nordstrom.automation.testng.ListenerChainable;

/**
 * <b>INTRODUCTION</b>
 * <p>
 * <b>Selenium Foundation</b> is an automation framework designed to extend and enhance the capabilities provided
 * by <b>Selenium 2.0</b> (<i>WebDriver</i>). The last stable release of Selenium 2.0 is <b>2.53.1</b>. 
 * <p>
 * This <b>QuickStart</b> class provides a fully-functional example of a test class built around <b>Selenium
 * Foundation</b>, <b>TestNG Foundation</b>, and the <b>Settings API</b>. It demonstrates how to set up required 
 * elements and introduces several key features that you're likely to use on a regular basis.
 * <p>
 * <b>REQUIRED ELEMENTS</b>
 * <ul>
 *     <li>{@link ListenerChain}: <br>
 *     <b>ListenerChain</b> is a TestNG listener that enables you to add other listeners at runtime and guarantees the
 *     order in which they're invoked. This is similar in behavior to a JUnit rule chain.</li>
 *     <li>The {@link ListenerChainable} interface: <br>
 *     Test classes that implement the <b>ListenerChainable</b> interface get the opportunity to attach listeners to
 *     the chain before the <b>SuiteRunner</b> starts.</li>
 *     <li>The {@link #attachListeners} method: <br>
 *     This reference implementation of the {@code attachListeners} method declared by the <b>ListenerChainable</b>
 *     interface attaches two listeners that manage several core features of <b>Selenium Foundation</b>: <ul>
 *         <li>{@link DriverManager}: <br>
 *         <b>DriverManager</b> is a TestNG listener that manages driver sessions and local Selenium Grid servers.</li>
 *         <li>{@link ExecutionFlowController}: <br>
 *         <b>ExecutionFlowController</b> is a TestNG listener that propagates test context attributes: <br>
 *         [<i>before</i> method] &rarr; [test method] &rarr; [<i>after</i> method]</li></ul>
 *     </li>
 * </ul>
 * <p>
 * <b>DEMONSTRATED FEATURES</b>
 * <ul>
 *     <li>{@link InitialPage}: <br>
 *     <b>InitialPage</b> is a Java annotation that enables you to specify the initial page class and/or URL that
 *     should be loaded at the start of the test method. This can be applied to each test individually, or it can
 *     be applied at the class level to specify the default page for all test in the class. It can also be applied
 *     to <b>@Before...</b> configuration methods to provide driver sessions opened to the desired page.</li>
 *     <li>{@link SeleniumConfig}: <br>
 *     <b>SeleniumConfig</b> declares settings and methods related to Selenium WebDriver and Grid configuration.
 *     This class is built on the <b>Settings API</b>, composed of defaults, stored values, and System properties.</li>
 *     <li>{@link SeleniumSettings}: <br>
 *     <b>SeleniumSettings</b> declares the constants, property names, and default values for the settings managed
 *     by <b>SeleniumConfig</b>. Defaults can be overridden via System properties or the <i>settings.propeties</i>
 *     file in your user "home" directory. See <b>ESSENTIAL SETTINGS</b> below for more details.</li>
 *     </li>
 *     <li>{@link ReporterAppender}: <br>
 *     </li>
 * </ul>
 * <p>
 * <b>ESSENTIAL SETTINGS</b>
 * <p>
 * You'll probably find that the defaults assigned to most settings will suffice in most basic scenarios. However, it's
 * likely that you'll need to override one or more of the following. The <b>Property Name</b> column indicates the name
 * of the System property associated with the setting. To override a setting, you can either add a line for the setting
 * to your <i>settings.properties</i> file or define a System property.
 * <p>
 * <table style="text-align: left; border: 1px solid black; border-collapse: collapse;">
 *     <tr>
 *         <th style="text-align: left; border: 1px solid black;">Constant</th>
 *         <th style="text-align: left; border: 1px solid black;">Property Name</th>
 *         <th style="text-align: center; border: 1px solid black;">Default</th>
 *     </tr>
 *     <tr>
 *         <td style="text-align: left; border: 1px solid black;">{@link SeleniumSettings#BROWSER_NAME BROWSER_NAME}</td>
 *         <td style="text-align: left; border: 1px solid black;">selenium.browser.name</td>
 *         <td style="text-align: center; border: 1px solid black;">(none) *</td>
 *     </tr>
 *     <tr>
 *         <td style="text-align: left; border: 1px solid black;">{@link SeleniumSettings#TARGET_HOST TARGET_HOST</td>
 *         <td style="text-align: left; border: 1px solid black;">selenium.target.host</td>
 *         <td style="text-align: center; border: 1px solid black;">localhost</td>
 *     </tr>
 *     <tr>
 *         <td style="text-align: left; border: 1px solid black;">{@link SeleniumSettings#TARGET_PATH TARGET_PATH</td>
 *         <td style="text-align: left; border: 1px solid black;">selenium.target.path</td>
 *         <td style="text-align: center; border: 1px solid black;">/</td>
 *     </tr>
 * </table>
 * <p>
 * * <b>NOTE</b>: By default, PhantonJS is selected as the browser. For easier override, this is specified through
 * {@link SeleniumSettings#BROWSER_CAPS BROWSER_CAPS} instead of {@link SeleniumSettings#BROWSER_NAME BROWSER_NAME}.
 * For details, see <a href="../../../../../../../docs/ManipulateSettingsWithSeleniumConfig.md">Manipulate Settings
 * with SeleniumConfig</a>. 
 * <p>
 * <b>OVERRIDING DEFAULTS</b>
 * <p>
 * <b>SeleniumConfig</b> searches a series of locations for a <i>settings.properties</i> file. This file will typically
 * be stored in your user "home" folder. Any settings declared in this file will override the defaults assigned in the
 * <b>SeleniumSettings</b> enumeration. Settings that are declared as System properties will override both the defaults
 * assigned by <b>SeleniumSettings</b> and settings declared in <i>settings.properties</i>. For example: 
 * <p>
 * <table style="text-align: left; border: 1px solid black; border-collapse: collapse;">
 *     <tr style="text-align: left; border: 1px solid black;">
 *         <th><i>settings.properties</i></th>
 *     </tr>
 *     <tr>
 *         <td>selenium.target.host=my.server.com</td>
 *     </tr>
 *     <tr>
 *         <td>selenium.browser.name=chrome</td>
 *     </tr>
 * </table>
 * <p>
 * This sample <i>settings.properties</i> file overrides the values of {@link SeleniumSettings#TARGET_HOST TARGET_HOST}
 * and {@link SeleniumSettings#BROWSER_NAME BROWSER_NAME}. The latter can be overridden by System property declaration:
 * <p>
 * <blockquote>{@code -Dselenium.browser.name=firefox}</blockquote>
 * <p>
 * The hierarchy of evaluation produces the following results:
 * <p>
 * <blockquote>
 *     <b>BROWSER_NAME</b> = <mark>firefox</mark>; 
 *     <b>TARGET_HOST</b> = <mark>my.server.com</mark>; 
 *     <b>TARGET_PATH</b> = <mark>/</mark>
 * </blockquote>
 * <p>
 * <b>INSTALLING DRIVERS</b>
 * <p>
 * Whichever browser you choose to run your automation on, you need to make sure to install the latest driver for that
 * browser compatible with <b>Selenium WebDriver 2.53.1</b>, along with a compatible release of the browser itself. We
 * recommend that you install the drivers and browsers on the file search path to avoid the need to provide additional
 * configuration details via scenario-specific means.
 * <p>
 * Here are the official homes for several of the major drivers: <ul>
 *     <li>GhostDriver (PhantomJS) - <a href="http://phantomjs.org/download.html">
 *     http://phantomjs.org/download.html</a></li>
 *     <li>ChromeDriver - <a href="https://sites.google.com/a/chromium.org/chromedriver/downloads">
 *     https://sites.google.com/a/chromium.org/chromedriver/downloads</a></li>
 *     <li>IEDriver - <a href="http://selenium-release.storage.googleapis.com/index.html?path=2.53/">
 *     http://selenium-release.storage.googleapis.com/index.html?path=2.53/</a></li>
 * </ul>
 * <b>NOTE</b>: GhostDriver and ChromeDriver are simple binary installations, but several system configuration changes
 * must be applied for IEDriver to work properly. For details, visit the InternetExplorerDriver project Wiki on GitHub
 * and follow the <a href="https://github.com/SeleniumHQ/selenium/wiki/InternetExplorerDriver#required-configuration">
 * Required Configuration</a> procedure.
 */
@Listeners({ListenerChain.class})
@InitialPage(ExamplePage.class)
public class QuickStart implements ListenerChainable {
	
	private static final String PAGE_TITLE = "Example Page";
	private static final Logger LOGGER = LoggerFactory.getLogger(QuickStart.class);
	
	@Test
	public void dummyTest() {
		SeleniumConfig config = SeleniumConfig.getConfig();
		LOGGER.info("The configured browser is: " + config.getString(SeleniumSettings.BROWSER_NAME.key()));
		ExamplePage examplePage = (ExamplePage) DriverManager.getInitialPage();
		assertEquals(examplePage.getTitle(), PAGE_TITLE, "Unexpeced page title");
	}

	@Override
	public void attachListeners(ListenerChain listenerChain) {
		listenerChain.around(DriverManager.class).around(ExecutionFlowController.class);
	}
}
