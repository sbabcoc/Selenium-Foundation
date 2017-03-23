package com.nordstrom.automation.selenium.core;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.openqa.grid.internal.utils.GridHubConfiguration;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;

import com.nordstrom.automation.selenium.SeleniumConfig;

/**
 * This class provides basic support for interacting with a Selenium Grid instance.
 */
public class GridUtility {
	
	private static final String GRID_HUB = "GridHub";
	private static final String GRID_NODE = "GridNode";
	
	private GridUtility() {
		throw new AssertionError("GridUtility is a static utility class that cannot be instantiated");
	}
	
	/**
	 * Determine if the configured Selenium Grid hub is active.<br>
	 * <b>NOTE</b>: If configured for local execution, this method ensures that a local hub and node are active.
	 * 
	 * @return 'true' if configured hub is active; otherwise 'false'
	 * @throws UnknownHostException No response was received from the configured hub host. 
	 * @throws MalformedURLException The configured hub settings produce a malformed URL.
	 */
	public static boolean isHubActive() throws UnknownHostException, MalformedURLException {
		return isHubActive(Reporter.getCurrentTestResult());
	}
	
	/**
	 * Determine if the configured Selenium Grid hub is active.<br>
	 * <b>NOTE</b>: If configured for local execution, this method ensures that a local hub and node are active.
	 * 
	 * @param testResult configuration context (TestNG test result object)
	 * @return 'true' if configured hub is active; otherwise 'false'
	 * @throws UnknownHostException No response was received from the configured hub host. 
	 * @throws MalformedURLException The configured hub settings produce a malformed URL.
	 */
	public static boolean isHubActive(ITestResult testResult) throws UnknownHostException, MalformedURLException {
		
		if (testResult == null) throw new NullPointerException("Test result object must be non-null");
		
		SeleniumConfig config = SeleniumConfig.getConfig(testResult);
		GridHubConfiguration hubConfig = config.getHubConfig();
		
		boolean isActive = isHubActive(hubConfig);
		
		if (!isActive) {
			// get IP address of the configured hub host
			InetAddress addr = InetAddress.getByName(hubConfig.getHost());
			// if configured for local hub
			if (isThisMyIpAddress(addr)) {
				try {
					// launch local Selenium Grid hub
					Process gridHub = GridProcess.start(testResult, config.getHubArgs());
					testResult.getTestContext().setAttribute(GRID_HUB, gridHub);
					// launch local Selenium Grid node
					Process gridNode = GridProcess.start(testResult, config.getNodeArgs());
					testResult.getTestContext().setAttribute(GRID_NODE, gridNode);
					// FIXME - Find method to confirm that Grid is ready
					Thread.sleep(5000);
					isActive = true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return isActive;
	}

	/**
	 * Determine if the configured Selenium Grid hub is active.<br>
	 * 
	 * @param hubConfig hub configuration object
	 * @return 'true' if configured hub is active; otherwise 'false'
	 * @throws MalformedURLException The configured hub settings produce a malformed URL.
	 */
	static boolean isHubActive(GridHubConfiguration hubConfig) throws MalformedURLException {
		HttpHost host = new HttpHost(hubConfig.getHost(), hubConfig.getPort());
		HttpClient client = HttpClientBuilder.create().build();
		URL sessionURL = new URL("http://" + hubConfig.getHost() + ":" + hubConfig.getPort() + "/grid/api/hub/");
		BasicHttpEntityEnclosingRequest basicHttpEntityEnclosingRequest = 
				new BasicHttpEntityEnclosingRequest("GET", sessionURL.toExternalForm());
		try {
			HttpResponse response = client.execute(host, basicHttpEntityEnclosingRequest);
			return (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
		} catch (IOException e) {
		}
		return false;
	}
	
	/**
	 * Get the Selenium driver for the current configuration context.
	 * 
	 * @return driver object (may be 'null')
	 */
	public static WebDriver getDriver() {
		return getDriver(Reporter.getCurrentTestResult());
	}
	
	/**
	 * Get the Selenium driver for the specified configuration context.
	 * 
	 * @param testResult configuration context (TestNG test result object)
	 * @return driver object (may be 'null')
	 */
	public static WebDriver getDriver(ITestResult testResult) {
		if (testResult == null) throw new NullPointerException("Test result object must be non-null");
		
		SeleniumConfig config = SeleniumConfig.getConfig(testResult);
		GridHubConfiguration hubConfig = config.getHubConfig();
		try {
			URL hubUrl = new URL("http://" + hubConfig.getHost() + ":" + hubConfig.getPort() + "/wd/hub");
			if (isHubActive(testResult)) {
				return new RemoteWebDriver(hubUrl, config.getBrowserCaps());
			} else {
				throw new IllegalStateException("No Selenium Grid instance was found at " + hubUrl);
			}
		} catch (UnknownHostException e) {
			throw new RuntimeException("Specified Selenium Grid host '" + hubConfig.getHost() + "' was not found", e);
		} catch (MalformedURLException e) {
			throw new RuntimeException("Selenium Grid host specification '" + hubConfig.getHost() + "' is malformed", e);
		}
	}
	
	/**
	 * Get the Selenium Grid hub server process for the current configuration context.
	 * 
	 * @return process object for the hub (may be 'null')
	 */
	public static Process getGridHub() {
		return getGridHub(Reporter.getCurrentTestResult().getTestContext());
	}
	
	/**
	 * Get the Selenium Grid hub server process for the specified configuration context.
	 * 
	 * @param testContext configuration context (TestNG test context object)
	 * @return process object for the hub (may be 'null')
	 */
	public static Process getGridHub(ITestContext testContext) {
		return (Process) testContext.getAttribute(GRID_HUB);
	}

	/**
	 * Get the Selenium Grid node server process for the current configuration context.
	 * 
	 * @return process object for the node (may be 'null')
	 */
	public static Process getGridNode() {
		return getGridNode(Reporter.getCurrentTestResult().getTestContext());
	}
	
	/**
	 * Get the Selenium Grid node server process for the specified configuration context.
	 * 
	 * @param testContext configuration context (TestNG test context object)
	 * @return process object for the node (may be 'null')
	 */
	public static Process getGridNode(ITestContext testContext) {
		return (Process) testContext.getAttribute(GRID_NODE);
	}

	/**
	 * Determine if the specified address is local to the machine we're running on.
	 * 
	 * @param addr Internet protocol address object
	 * @return 'true' if the specified address is local; otherwise 'false'
	 */
	public static boolean isThisMyIpAddress(InetAddress addr) {
		// Check if the address is a valid special local or loop back
		if (addr.isAnyLocalAddress() || addr.isLoopbackAddress()) return true;

		// Check if the address is defined on any interface
		try {
			return NetworkInterface.getByInetAddress(addr) != null;
		} catch (SocketException e) {
			return false;
		}
	}
}
