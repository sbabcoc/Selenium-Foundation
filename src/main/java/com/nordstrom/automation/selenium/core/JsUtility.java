package com.nordstrom.automation.selenium.core;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.BrowserType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.JsonObject;
import com.nordstrom.automation.selenium.model.Page;
import com.nordstrom.automation.selenium.utility.DataUtils;
import com.nordstrom.common.base.UncheckedThrow;

/**
 * Provides easier access to navigate to a new URL or run JavaScript.
 * <p>
 * Examples: 
 * 
 * <pre><code> import org.junit.Test;
 * import org.openqa.selenium.WebElement;
 * import com.nordstrom.automation.selenium.core.JsUtility;
 * 
 * public class JavaScriptExamples {
 * 
 *     &#47;**
 *      * This example executes an anonymous function that accepts an argument.
 *      * NOTE: Script file &lt;getMetaTagByName.js&gt; can be found below.
 *      *&#47;
 *     {@literal @Test}
 *     public String runAnonymousJavaScriptFunctionWithArgument(String name) {
 *         // Get script text from resource file &lt;getMetaTagByName.js&gt;.
 *         String script = JsUtility.getScriptResource("getMetaTagByName.js");
 *         // Execute script as anonymous function, passing specified argument
 *         WebElement response = JsUtility.runAndReturn(driver, script, WebElement.class, name);
 *         // If element reference was returned, extract 'content' attribute
 *         return (response == null) ? null : response.getAttribute("content");
 *     }
 * 
 *     &#47;**
 *      * This example injects a script node and executes a function that accepts an argument
 *      * NOTE: Script file &lt;testNode.js&gt; can be found below.
 *      *&#47;
 *     {@literal @Test}
 *     public void injectAndRunJavaScriptFunctionWithArgument() {
 *         // Get script text from resource file &lt;testNode.js&gt;.
 *         String script = JsUtility.getScriptResource("testNode.js");
 *         // Create HTML 'script' node, which is appended to the 'body' tag
 *         WebElement scriptNode = JsUtility.createScriptNode(driver, script);
 *         // NOTE: The script string copies local array 'arguments' to global array 'args'.
 *         // This exposes the arguments passed by WebDriver to the 'testNode' function.
 *         JsUtility.run(driver, "args = arguments; testNode();", "This is a test... Booga!");
 *     }
 * }</code></pre>
 * 
 * This is sample JavaScript file &lt;getMetaTagByName.js&gt;. This file can be stored anywhere on the 
 * class path, typically a 'resources' folder within the project hierarchy.
 * 
 * <pre><code> var found = document.getElementsByTagName("meta");
 * for (var i = 0; i &lt; found.length; i++) {
 *     if (found[i].getAttribute("name") == arguments[0]) return found[i];
 * }
 * return null;</code></pre>
 * 
 * This is sample JavaScript file &lt;testNode.js&gt;. This file can be stored anywhere on the class path,
 * typically a 'resources' folder within the project hierarchy.
 * 
 * <pre><code> function testNode() {
 *   var s = document.createElement('div');
 *   try {
 *       var n = document.createTextNode(args[0]);
 *       s.appendChild(n);
 *   } catch (e) {
 *       s.text = args[0];
 *   }
 *   document.body.appendChild(s);
 * }</code></pre>
 * 
 */
public final class JsUtility {
    
    private static final List<String> SPECIAL = Arrays.asList(BrowserType.IE, BrowserType.IPHONE, BrowserType.IPAD, null);
    private static final Logger LOGGER = LoggerFactory.getLogger(JsUtility.class);
    private static final String ASSIGN_NEW_URL = "assignNewUrl.js";
    private static final String JAVA_GLUE_LIB = "javaGlueLib.js";
    private static final String ERROR_PREFIX = "unknown error";
    private static final String CLASS_NAME_KEY = "className";
    private static final String MESSAGE_KEY = "message";
    
	private JsUtility() {
		throw new AssertionError("JsUtility is a static utility class that cannot be instantiated");
	}
	
    /**
     * Uses JavaScript to move the current Selenium test window to new location.
     * 
     * @param driver A handle to the currently running Selenium test window.
     * @param url The new location.
     */
    public static void newAddressBarLocation(WebDriver driver, String url) {
        String browserName = WebDriverUtils.getBrowserName(driver);
        
        if (SPECIAL.contains(browserName)) {
            driver.get(url);
        } else {
            String assign = getScriptResource(ASSIGN_NEW_URL);
            run(driver, assign, url);
        }
    }
    
    /**
     * Uses JavaScript to move the current Selenium test window to new location.
     * 
     * @param <T> The type of the resulting page.
     * @param driver A handle to the currently running Selenium test window.
     * @param url The new location.
     * @param pageType The type of the resulting page.
     * @return A page of the specified type.
     */
    public static <T extends Page> T pageForNewLocation(WebDriver driver, String url, Class<T> pageType) {
        newAddressBarLocation(driver, url);
        return Page.newPage(pageType, driver);
    }
    
    /**
     * Executes JavaScript in the context of the currently selected frame or window. The script
     * fragment provided will be executed as the body of an anonymous function.
     * 
     * <p>
     * Within the script, use <code>document</code> to refer to the current document. Note that local
     * variables will not be available once the script has finished executing, though global variables
     * will persist.
     * 
     * <p>
     * Arguments must be a number, a boolean, a String, WebElement, or a List of any combination of
     * the above. An exception will be thrown if the arguments do not meet these criteria. The
     * arguments will be made available to the JavaScript via the "arguments" magic variable, as if
     * the function were called via "Function.apply"
     * 
     * @param driver A handle to the currently running Selenium test window.
     * @param js The JavaScript to execute
     * @param args The arguments to the script. May be empty
     * @see JavascriptExecutor#executeScript(String, Object...)
     */
    public static void run(WebDriver driver, String js, Object... args) {
		Object result = WebDriverUtils.getExecutor(driver).executeScript(js, args);
		if (result != null) {
		    LOGGER.warn("The specified JavaScript returned a non-null result");
		}
    }
    
    /**
     * Executes JavaScript in the context of the currently selected frame or window. The script
     * fragment provided will be executed as the body of an anonymous function.
     * 
     * <p>
     * Within the script, use <code>document</code> to refer to the current document. Note that local
     * variables will not be available once the script has finished executing, though global variables
     * will persist.
     * 
     * <p>
     * If the script has a return value (i.e. if the script contains a <code>return</code> statement),
     * then the following steps will be taken:
     * 
     * <ul>
     * <li>For an HTML element, this method returns a WebElement</li>
     * <li>For a decimal, a Double is returned</li>
     * <li>For a non-decimal number, a Long is returned</li>
     * <li>For a boolean, a Boolean is returned</li>
     * <li>For all other cases, a String is returned.</li>
     * <li>For an array, return a List&lt;Object&gt; with each object following the rules above. We
     * support nested lists.</li>
     * <li>Unless the value is null or there is no return value, in which null is returned</li>
     * </ul>
     * 
     * <p>
     * Arguments must be a number, a boolean, a String, WebElement, or a List of any combination of
     * the above. An exception will be thrown if the arguments do not meet these criteria. The
     * arguments will be made available to the JavaScript via the "arguments" magic variable, as if
     * the function were called via "Function.apply"
     * 
     * @param <T> return type
     * @param driver A handle to the currently running Selenium test window.
     * @param js The JavaScript to execute
     * @param resultType Data type returned by the script
     * @param args The arguments to the script. May be empty
     * @return The result of the execution
     * @see JavascriptExecutor#executeScript(String, Object...)
     */
    @SuppressWarnings("unchecked") // required because Selenium is not type safe.
    public static <T> T runAndReturn(WebDriver driver, String js, Class<T> resultType, Object... args) {
		return (T) WebDriverUtils.getExecutor(driver).executeScript(js, args);
    }
    
    /**
     * Inject the Java glue code library into the current window
     * 
     * @param driver A handle to the currently running Selenium test window.
     */
    public static void injectGlueLib(WebDriver driver) {
    	JavascriptExecutor executor = WebDriverUtils.getExecutor(driver);
    	if ((boolean) executor.executeScript("return (typeof isObject != 'function');")) {
    		executor.executeScript(getScriptResource(JsUtility.JAVA_GLUE_LIB));
    	}
    }
    
    /**
     * Get the content of the name resource
     * 
     * @param resource resource filename
     * @return resource file content
     */
    public static String getScriptResource(String resource) {
        URL url = Resources.getResource(resource);
        try {
            return Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to load JavaScript resource '" + resource + "'");
        }
    }
    
    /**
     * Propagate the specified web driver exception, extracting encoded JavaScript exception if present
     * 
     * @param exception web driver exception to propagate
     */
	public static void propagate(WebDriverException exception) {
		Throwable thrown = exception;
		if (exception.getClass().equals(WebDriverException.class)) { 
			String message = exception.getMessage();
			int index = message.indexOf(':');
			if ((index != -1) && ERROR_PREFIX.equals(message.substring(0, index))) {
				String encodedException = message.substring(index + 1).trim();
				if (encodedException.contains("\"" + CLASS_NAME_KEY + "\"")
						&& encodedException.contains("\"" + MESSAGE_KEY + "\"")) {
					JsonObject obj = DataUtils.deserializeObject(encodedException);
					if (obj != null) {
						try {
							Class<?> clazz = Class.forName(obj.get(CLASS_NAME_KEY).toString());
							Constructor<?> ctor = clazz.getConstructor(String.class, Throwable.class);
							thrown = (Throwable) ctor.newInstance(obj.get(MESSAGE_KEY).toString(), exception);
							thrown.setStackTrace(new Throwable().getStackTrace());
						} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
								| IllegalAccessException | IllegalArgumentException | InvocationTargetException eaten) {
						}
					}
				}
			}
		}
		throw UncheckedThrow.throwUnchecked(thrown);
	}
	
	
	

}