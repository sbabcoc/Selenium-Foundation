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
import org.openqa.grid.internal.utils.configuration.GridNodeConfiguration;
import org.openqa.grid.selenium.GridLauncherV3;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.nordstrom.automation.selenium.SeleniumConfig;

public class GridUtility {
	
	private GridUtility() {
		throw new AssertionError("GridUtility is a static utility class that cannot be instantiated");
	}
	
	public static boolean isHubActive(String hubHost, int hubPort) 
			throws UnknownHostException, MalformedURLException {
		
		boolean isActive = false;
		
		HttpHost host = new HttpHost(hubHost, hubPort);
		HttpClient client = HttpClientBuilder.create().build();
		URL sessionURL = new URL("http://" + hubHost + ":" + hubPort + "/grid/api/hub/");
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
				String[] args;
				try {
					args = new String[] {"-host", hubHost, "-port", Integer.toString(hubPort), "-role", "hub"};
					GridLauncherV3.main(args);
					String hub = "http://" + hubHost + ":" + hubPort + "/grid/register/";
					args = new String[] {"-role", "node", "-hub", hub};
					GridLauncherV3.main(args);
					isActive = true;
				} catch (Exception e) {
				}
			}
		}
		
		return isActive;
	}
	
	public static WebDriver getDriver(String hubHost, int hubPort) {
		try {
			if (isHubActive(hubHost, hubPort)) {
				return new RemoteWebDriver(SeleniumConfig.getConfig().getBrowserCaps());
			} else {
				throw new IllegalStateException("No Selenium Grid instance was found at http://" + hubHost + ":" + hubPort);
			}
		} catch (UnknownHostException e) {
			throw new RuntimeException("Specified Selenium Grid host '" + hubHost + "' was not found", e);
		} catch (MalformedURLException e) {
			throw new RuntimeException("Selenium Grid host specification '" + hubHost + "' is malformed", e);
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
