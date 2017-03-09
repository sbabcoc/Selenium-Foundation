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
import org.openqa.grid.internal.utils.configuration.GridHubConfiguration;
import org.openqa.grid.selenium.GridLauncherV3;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestResult;
import org.testng.Reporter;

import com.nordstrom.automation.selenium.SeleniumConfig;

public class GridUtility {
	
	private GridUtility() {
		throw new AssertionError("GridUtility is a static utility class that cannot be instantiated");
	}
	
	public static boolean isHubActive() throws UnknownHostException, MalformedURLException {
		return isHubActive(Reporter.getCurrentTestResult());
	}
	
	public static boolean isHubActive(ITestResult testResult) throws UnknownHostException, MalformedURLException {
		
		if (testResult == null) throw new NullPointerException("Test result object must be non-null");
		
		boolean isActive = false;
		SeleniumConfig config = SeleniumConfig.getConfig(testResult);
		GridHubConfiguration hubConfig = config.getHubConfig();
		
		HttpHost host = new HttpHost(hubConfig.host, hubConfig.port);
		HttpClient client = HttpClientBuilder.create().build();
		URL sessionURL = new URL("http://" + hubConfig.host + ":" + hubConfig.port + "/grid/api/hub/");
		BasicHttpEntityEnclosingRequest basicHttpEntityEnclosingRequest = 
				new BasicHttpEntityEnclosingRequest("GET", sessionURL.toExternalForm());
		try {
			HttpResponse response = client.execute(host, basicHttpEntityEnclosingRequest);
			isActive = (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
		} catch (IOException e) {
		}
		
		if (!isActive) {
			InetAddress addr = InetAddress.getByName(sessionURL.getHost());
			if (isThisMyIpAddress(addr)) {
				try {
					GridLauncherV3.main(config.getHubArgs());
					GridLauncherV3.main(config.getNodeArgs());
					isActive = true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return isActive;
	}
	
	public static WebDriver getDriver() {
		return getDriver(Reporter.getCurrentTestResult());
	}
	
	public static WebDriver getDriver(ITestResult testResult) {
		if (testResult == null) throw new NullPointerException("Test result object must be non-null");
		
		SeleniumConfig config = SeleniumConfig.getConfig(testResult);
		GridHubConfiguration hubConfig = config.getHubConfig();
		try {
			URL hubUrl = new URL("http://" + hubConfig.host + ":" + hubConfig.port + "/wd/hub");
			if (isHubActive(testResult)) {
				return new RemoteWebDriver(hubUrl, config.getBrowserCaps());
			} else {
				throw new IllegalStateException("No Selenium Grid instance was found at " + hubUrl);
			}
		} catch (UnknownHostException e) {
			throw new RuntimeException("Specified Selenium Grid host '" + hubConfig.host + "' was not found", e);
		} catch (MalformedURLException e) {
			throw new RuntimeException("Selenium Grid host specification '" + hubConfig.host + "' is malformed", e);
		}
	}

	public static boolean isThisMyIpAddress(InetAddress addr) {
		// Check if the address is a valid special local or loop back
		if (addr.isAnyLocalAddress() || addr.isLoopbackAddress())
			return true;

		// Check if the address is defined on any interface
		try {
			return NetworkInterface.getByInetAddress(addr) != null;
		} catch (SocketException e) {
			return false;
		}
	}
}
