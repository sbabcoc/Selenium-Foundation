package com.nordstrom.automation.selenium.core;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.internal.utils.GridHubConfiguration;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;

import com.google.common.base.Function;
import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.SeleniumConfig.WaitType;
import com.nordstrom.automation.selenium.support.HttpHostWait;
import com.nordstrom.common.base.UncheckedThrow;

/**
 * This class provides basic support for interacting with a Selenium Grid instance.
 */
public class GridUtility {
	
	private static final String GRID_HUB = "GridHub";
	private static final String GRID_NODE = "GridNode";
	private static final String HUB_REQUEST = "/grid/api/hub/";
	private static final String NODE_REQUEST = "/wd/hub/status/";
	
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
					HttpHostWait hubWait = getWait(getHubHost(hubConfig), testResult);
					hubWait.until(hostIsActive(HUB_REQUEST));
					testResult.getTestContext().setAttribute(GRID_HUB, gridHub);
					// launch local Selenium Grid node
					Process gridNode = GridProcess.start(testResult, config.getNodeArgs());
					HttpHostWait nodeWait = getWait(getNodeHost(config.getNodeConfig()), testResult);
					nodeWait.until(hostIsActive(NODE_REQUEST));
					testResult.getTestContext().setAttribute(GRID_NODE, gridNode);
					isActive = true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return isActive;
	}

	/**
	 * Determine if the configured Selenium Grid hub is active.
	 * 
	 * @param hubConfig hub configuration object
	 * @return 'true' if configured hub is active; otherwise 'false'
	 * @throws MalformedURLException The configured hub settings produce a malformed URL.
	 */
	static boolean isHubActive(GridHubConfiguration hubConfig) throws MalformedURLException {
		return isHostActive(getHubHost(hubConfig), HUB_REQUEST);
	}
	
	/**
	 * Get an {@link HttpHost} object for the configured Selenium Grid hub.
	 * 
	 * @param hubConfig hub configuration object
	 * @return HttpHost object for configured hub
	 */
	static HttpHost getHubHost(GridHubConfiguration hubConfig) {
		return new HttpHost(hubConfig.getHost(), hubConfig.getPort());
	}

	/**
	 * Determine if the configured Selenium Grid node is active.
	 * 
	 * @param nodeConfig node configuration object
	 * @return 'true' if configured node is active; otherwise 'false'
	 * @throws MalformedURLException The configured node settings produce a malformed URL.
	 */
	static boolean isNodeActive(RegistrationRequest nodeConfig) throws MalformedURLException {
		return isHostActive(getNodeHost(nodeConfig), NODE_REQUEST);
	}
	
	/**
	 * Get an {@link HttpHost} object for the configured Selenium Grid node.
	 * 
	 * @param nodeConfig node configuration object
	 * @return HttpHost object for configured node
	 */
	static HttpHost getNodeHost(RegistrationRequest nodeConfig) {
		Map<String, Object> config = nodeConfig.getConfiguration();
		return new HttpHost((String) config.get("host"), (Integer) config.get("port"));
	}
	
	/**
	 * Determine if the specified Selenium Grid host (hub or node) is active.
	 * 
	 * @param host HTTP host connection to be checked
	 * @param request request path (may include parameters)
	 * @return 'true' if specified host is active; otherwise 'false'
	 * @throws MalformedURLException The specified host settings produce a malformed URL.
	 */
	private static boolean isHostActive(HttpHost host, String request) throws MalformedURLException {
		try {
			HttpResponse response = getHttpResponse(host, request);
			return (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
		} catch (IOException e) {
			if (e instanceof MalformedURLException) throw (MalformedURLException) e;
		}
		return false;
	}
	
	/**
	 * Send the specified GET request to the indicated host.
	 * 
	 * @param host target HTTP host connection
	 * @param request request path (may include parameters)
	 * @return host response for the specified GET request
	 * @throws IOException the request triggered an I/O exception
	 */
	public static HttpResponse getHttpResponse(HttpHost host, String request) throws IOException {
		HttpClient client = HttpClientBuilder.create().build();
		URL sessionURL = new URL(host.toURI() + request);
		BasicHttpEntityEnclosingRequest basicHttpEntityEnclosingRequest = 
				new BasicHttpEntityEnclosingRequest("GET", sessionURL.toExternalForm());
		return client.execute(host, basicHttpEntityEnclosingRequest);
	}
	
	/**
	 * Returns a 'wait' proxy that determines if the context host is active.
	 * 
	 * @param request request path (may include parameters)
	 * @return 'true' if specified host is active; otherwise 'false'
	 */
	public static Function<HttpHost, Boolean> hostIsActive(final String request) {
		return new Function<HttpHost, Boolean>() {

			@Override
			public Boolean apply(HttpHost host) {
				try {
					return Boolean.valueOf(isHostActive(host, request));
				} catch (MalformedURLException e) {
					throw UncheckedThrow.throwUnchecked(e);
				}
			}
			
			@Override
			public String toString() {
				return "Selenium Grid host to be active";
			}
		};
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
	
	/**
	 * Get a {@link HttpHostWait} object for the specified host.
	 * 
	 * @param host wait object context (HTTP host connection)
	 * @param testResult configuration context (TestNG test result object)
	 * @return HttpHostWait object for the specified host
	 */
	public static HttpHostWait getWait(HttpHost host, ITestResult testResult) {
		SeleniumConfig config = SeleniumConfig.getConfig(testResult);
		return getWait(host, WaitType.HOST.getInterval(config));
	}
	
	/**
	 * Get a {@link HttpHostWait} object for the specified host.
	 * 
	 * @param host wait object context (HTTP host connection)
	 * @param timeOutInSeconds timeout in seconds when an expectation is called
	 * @return HttpHostWait object for the specified host
	 */
	public static HttpHostWait getWait(HttpHost host, long timeOutInSeconds) {
		return new HttpHostWait(host, timeOutInSeconds);
	}
}
