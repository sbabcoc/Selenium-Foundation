package com.nordstrom.automation.selenium.core;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.nordstrom.automation.selenium.exceptions.DocumentNotReadyTimeoutException;
import com.nordstrom.automation.selenium.model.FirefoxShadowRoot;
import com.nordstrom.automation.selenium.support.Coordinator;
import com.nordstrom.automation.selenium.utility.DataUtils;
import com.nordstrom.common.base.UncheckedThrow;

/**
 * Provides easier access to navigate to a new URL or run JavaScript.
 * <p>
 * Examples: 
 * 
 * <pre><code> import org.openqa.selenium.WebDriver;
 * import org.openqa.selenium.WebElement;
 * import com.nordstrom.automation.selenium.core.JsUtility;
 * 
 * public class JavaScriptExample {
 * 
 *     &#47;**
 *      * This example executes an anonymous function that accepts an argument.&lt;br&gt;
 *      * NOTE: Script file &lt;getMetaTagByName.js&gt; can be found below.
 *      * 
 *      * {@literal @param} driver Selenium driver
 *      * {@literal @param} name name of target meta tag
 *      * {@literal @return} meta element with desired name; 'null' if not found
 *      *&#47;
 *     public static String runAnonymousJavaScriptFunctionWithArgument(WebDriver driver, String name) {
 *         // Get script text from resource file &lt;getMetaTagByName.js&gt;.
 *         String script = JsUtility.getScriptResource("getMetaTagByName.js");
 *         // Execute script as anonymous function, passing specified argument
 *         WebElement response = JsUtility.runAndReturn(driver, script, name);
 *         // If element reference was returned, extract 'content' attribute
 *         return (response == null) ? null : response.getAttribute("content");
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
 */
public final class JsUtility {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(JsUtility.class);
    private static final String JAVA_GLUE_LIB = "javaGlueLib.js";
    private static final String ERROR_MESSAGE_KEY = "errorMessage";
    private static final String CLASS_NAME_KEY = "className";
    private static final String MESSAGE_KEY = "message";
    
    private static final String DOCUMENT_READY = getScriptResource("documentReady.js");
    private static final String CREATE_SCRIPT_NODE = getScriptResource("createScriptNode.js");
    
    private static final List<String> JS_EXCEPTIONS = Arrays.asList(
                    "org.openqa.selenium.WebDriverException",
                    "org.openqa.selenium.JavascriptException");
    
    /**
     * Private constructor to prevent instantiation.
     */
    private JsUtility() {
        throw new AssertionError("JsUtility is a static utility class that cannot be instantiated");
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
    public static void run(final WebDriver driver, final String js, final Object... args) {
        String script = FirefoxShadowRoot.injectShadowArgs(driver, js, args);
        Object result = WebDriverUtils.getExecutor(driver).executeScript(script, args);
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
     * @param args The arguments to the script. May be empty
     * @return The result of the execution
     * @see JavascriptExecutor#executeScript(String, Object...)
     */
    @SuppressWarnings("unchecked") // required because Selenium is not type safe.
    public static <T> T runAndReturn(final WebDriver driver, final String js, final Object... args) {
        String script = FirefoxShadowRoot.injectShadowArgs(driver, js, args);
        return (T) WebDriverUtils.getExecutor(driver).executeScript(script, args);
    }
    
    /**
     * Returns a 'wait' proxy that determines if the current document is in 'ready' state.
     * 
     * @return 'true' if the document is in 'ready' state; otherwise 'false'
     */
    public static Coordinator<Boolean> documentIsReady() {
        return new Coordinator<Boolean>() {
            
            /**
             * {@inheritDoc}
             */
            @Override
            public Boolean apply(final SearchContext context) {
                return (Boolean) WebDriverUtils.getExecutor(context).executeScript(DOCUMENT_READY);
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString() {
                return "document to be ready";
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public TimeoutException differentiateTimeout(TimeoutException e) {
                return new DocumentNotReadyTimeoutException(e.getMessage(), e.getCause());
            }
        };
    }
    
    /**
     * Inject the Java glue code library into the current window
     * 
     * @param driver A handle to the currently running Selenium test window.
     */
    public static void injectGlueLib(final WebDriver driver) {
        JavascriptExecutor executor = WebDriverUtils.getExecutor(driver);
        if ((boolean) executor.executeScript("return (typeof isObject != 'function');")) {
            executor.executeScript(CREATE_SCRIPT_NODE, getScriptResource(JAVA_GLUE_LIB));
        }
    }
    
    /**
     * Get the content of the name resource
     * 
     * @param resource resource filename
     * @return resource file content
     */
    public static String getScriptResource(final String resource) {
        URL url = Resources.getResource(resource);
        try {
            return Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to load JavaScript resource '" + resource + "'", e);
        }
    }
    
    /**
     * Propagate the specified web driver exception, extracting encoded JavaScript exception if present
     * 
     * @param driver A handle to the currently running Selenium test window.
     * @param exception web driver exception to propagate
     * @return nothing (this method always throws the specified exception)
     * @since 17.4.0 
     */
    @SuppressWarnings("deprecation")
    public static RuntimeException propagate(final WebDriver driver, final WebDriverException exception) {
        Throwable thrown = exception;
        // if exception is a WebDriverException (not a sub-class)
        if (JS_EXCEPTIONS.contains(exception.getClass().getName())) {
            // extract serialized exception object from message
            thrown = extractException(exception, exception.getMessage());
            
            // if driver spec'd and no serialized exception found
            if ((driver != null) && (thrown.equals(exception))) {
                // get browser log entries
                LogEntries logEntries = driver.manage().logs().get(LogType.BROWSER);

                // for each log entry
                for (LogEntry logEntry : logEntries.filter(Level.WARNING)) {
                    // extract serialized exception object from message
                    thrown = extractException(exception, logEntry.getMessage());
                    // done if serialized exception found
                    if (!thrown.equals(exception)) break;
                }
            }
        }
        // throw resolved exception as unchecked
        throw UncheckedThrow.throwUnchecked(thrown);
    }

    /**
     * If present, extract JSON-formatted serialized exception object from the specified message.
     * <p>
     * <b>NOTE</b>: If the message contains a serialized exception object, the exception is deserialized with its cause
     * set to the specified {@code WebDriverException}. If no serialized exception is found, the specified exception is
     * returned instead.
     * 
     * @param exception web driver exception
     * @param message message to scan for serialized exception object
     * @return deserialized exception; specified exception is none is found
     */
    private static Throwable extractException(final WebDriverException exception, String message) {
        // only retain the first line
        message = message.split("\n")[0].trim();
        // extract JSON string from message
        message = extractJsonString(message);
        // deserialize encoded exception object if present
        return deserializeException(exception, message);
    }
    
    /**
     * Remove the error prefix from the specified exception message
     * 
     * @param message exception message
     * @return exception message with error prefix removed
     */
    private static String extractJsonString(final String message) {
        int beginIndex = message.indexOf('{');
        int endIndex = message.lastIndexOf('}');
        if ((beginIndex != -1) && (endIndex != -1)) {
            return message.substring(beginIndex, endIndex + 1);
        }
        return message;
    }
    
    /**
     * De-serialize the specified JSON-encoded exception
     * 
     * @param exception web driver exception to propagate
     * @param jsonStr JSON string
     * @return if present, exception decoded from JSON; otherwise, original WebDriverException object
     */
    @SuppressWarnings("unchecked")
    private static Throwable deserializeException(final WebDriverException cause, final String jsonStr) {
        Throwable thrown = cause;
        // if message appears to be an encoded exception object
        if (jsonStr.contains("\"" + CLASS_NAME_KEY + "\"") && jsonStr.contains("\"" + MESSAGE_KEY + "\"")) {
            Map<String, ?> obj = DataUtils.fromString(jsonStr, HashMap.class);
            
            // if successful
            if (obj != null) {
                if (obj.containsKey(ERROR_MESSAGE_KEY)) {
                    obj = (Map<String, String>) obj.get(ERROR_MESSAGE_KEY);
                }
                
                String className = (String) obj.get(CLASS_NAME_KEY);
                String message = (String) obj.get(MESSAGE_KEY);
                
                try {
                    Class<?> clazz = Class.forName(className);
                    Constructor<?> ctor = clazz.getConstructor(String.class, Throwable.class);
                    thrown = (Throwable) ctor.newInstance(message, cause);
                    thrown.setStackTrace(new Throwable().getStackTrace());
                } catch (ClassNotFoundException | NoSuchMethodException | SecurityException
                        | InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException eaten) {
                    LOGGER.warn("Unable to instantiate exception: {}", className, eaten);
                }
            } else {
                LOGGER.warn("Unable to deserialize encoded exception object: {}", jsonStr);
            }
        }
        return thrown;
    }
}
