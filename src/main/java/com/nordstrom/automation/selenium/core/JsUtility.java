package com.nordstrom.automation.selenium.core;

import static java.nio.charset.StandardCharsets.UTF_8;
import static com.nordstrom.automation.selenium.servlet.ExamplePageServlet.readAllBytes;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
 *         return (response == null) ? null : WebDriverUtils.getDomAttributeOf(response, "content");
 *     }
 * }</code></pre>
 * 
 * This is sample JavaScript file &lt;getMetaTagByName.js&gt;. This file can be stored anywhere on the 
 * class path, typically a 'resources' folder within the project hierarchy.
 * 
 * <pre><code> var found = document.getElementsByTagName("meta");
 * for (var i = 0; i &lt; found.length; i++) {
 *     if (WebDriverUtils.getDomAttributeOf(found[i], "name") == arguments[0]) return found[i];
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
     * <p>Within the script, use <code>document</code> to refer to the current document. Note that local
     * variables will not be available once the script has finished executing, though global variables
     * will persist.
     * 
     * <p>Arguments must be a number, a boolean, a String, WebElement, or a List of any combination of
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
        Object result = runScript(false, driver, js, args);
        if (result != null) {
            LOGGER.warn("The specified synchronous JavaScript returned a non-null result");
        }
    }
    
    /**
     * Executes JavaScript in the context of the currently selected frame or window. The script
     * fragment provided will be executed as the body of an anonymous function.
     * 
     * <p>Within the script, use <code>document</code> to refer to the current document. Note that local
     * variables will not be available once the script has finished executing, though global variables
     * will persist.
     * 
     * <p>If the script has a return value (i.e. if the script contains a <code>return</code> statement),
     * then the following steps will be taken:
     * 
     * <ul>
     *     <li>For an HTML element, this method returns a WebElement.</li>
     *     <li>For a decimal number, a Double is returned.</li>
     *     <li>For a non-decimal number, a Long is returned.</li>
     *     <li>For a boolean, a Boolean is returned.</li>
     *     <li>For all other cases, a String is returned.</li>
     *     <li>For an array, return a List&lt;Object&gt; with each object following the rules above.
     *         We support nested lists.</li>
     *     <li>For a map, return a Map&lt;String, Object&gt; with values following the rules above.</li>
     *     <li>Unless the value is null or there is no return value, in which null is returned.</li>
     * </ul>
     * 
     * <p>Arguments must be a number, a boolean, a String, WebElement, or a List of any combination of
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
        Object result = runScript(false, driver, js, args);
        if (result == null) {
            LOGGER.warn("The specified synchronous JavaScript returned a null result");
        }
        return (T) result;
    }
    
    /**
     * Execute an asynchronous piece of JavaScript in the context of the currently selected frame or
     * window. Unlike executing {@link #run(WebDriver, String, Object...) synchronous JavaScript},
     * scripts executed with this method must explicitly signal they are finished by invoking the
     * provided callback. This callback is always injected into the executed function as the last
     * argument.
     *
     * <p>The first argument passed to the callback function will be used as the script's result. This
     * value will be handled as follows:
     *
     * <ul>
     *     <li>For an HTML element, this method returns a WebElement.</li>
     *     <li>For a decimal number, a Double is returned.</li>
     *     <li>For a non-decimal number, a Long is returned.</li>
     *     <li>For a boolean, a Boolean is returned.</li>
     *     <li>For all other cases, a String is returned.</li>
     *     <li>For an array, return a List&lt;Object&gt; with each object following the rules above.
     *         We support nested lists.</li>
     *     <li>For a map, return a Map&lt;String, Object&gt; with values following the rules above.</li>
     *     <li>Unless the value is null or there is no return value, in which null is returned.</li>
     * </ul>
     *
     * <p>The default timeout for a script to be executed is 0ms. In most cases, including the
     * examples below, one must set the script timeout {@link
     * WebDriver.Timeouts} beforehand to a value sufficiently large
     * enough.
     *
     * <p>Example #1: Performing a sleep in the browser under test.
     *
     * <pre>{@code
     * long start = System.currentTimeMillis();
     * JsUtility.runAsync(driver,
     *     "window.setTimeout(arguments[arguments.length - 1], 500);");
     * System.out.println(
     *     "Elapsed time: " + (System.currentTimeMillis() - start));
     * }</pre>
     *
     * <p>Example #2: Synchronizing a test with an AJAX application:
     *
     * <pre>{@code
     * WebElement composeButton = driver.findElement(By.id("compose-button"));
     * composeButton.click();
     * JsUtility.runAsync(driver,
     *     "var callback = arguments[arguments.length - 1];" +
     *     "mailClient.getComposeWindowWidget().onload(callback);");
     * driver.switchTo().frame("composeWidget");
     * driver.findElement(By.id("to")).sendKeys("bog@example.com");
     * }</pre>
     *
     * <p>Script arguments must be a number, a boolean, a String, WebElement, or a List of any
     * combination of the above. An exception will be thrown if the arguments do not meet these
     * criteria. The arguments will be made available to the JavaScript via the "arguments" variable.
     *
     * @param driver A handle to the currently running Selenium test window.
     * @param js The JavaScript to execute.
     * @param args The arguments to the script. May be empty.
     * @see WebDriver.Timeouts
     * @see JavascriptExecutor#executeAsyncScript(String, Object...)
     */
    public static void runAsync(final WebDriver driver, final String js, final Object... args) {
        Object result = runScript(true, driver, js, args);
        if (result != null) {
            LOGGER.warn("The specified asynchronous JavaScript returned a non-null result");
        }
    }
    
    /**
     * Execute an asynchronous piece of JavaScript in the context of the currently selected frame or
     * window. Unlike executing {@link #runAndReturn(WebDriver, String, Object...) synchronous JavaScript},
     * scripts executed with this method must explicitly signal they are finished by invoking the
     * provided callback. This callback is always injected into the executed function as the last
     * argument.
     *
     * <p>The first argument passed to the callback function will be used as the script's result. This
     * value will be handled as follows:
     *
     * <ul>
     *     <li>For an HTML element, this method returns a WebElement.</li>
     *     <li>For a decimal number, a Double is returned.</li>
     *     <li>For a non-decimal number, a Long is returned.</li>
     *     <li>For a boolean, a Boolean is returned.</li>
     *     <li>For all other cases, a String is returned.</li>
     *     <li>For an array, return a List&lt;Object&gt; with each object following the rules above.
     *         We support nested lists.</li>
     *     <li>For a map, return a Map&lt;String, Object&gt; with values following the rules above.</li>
     *     <li>Unless the value is null or there is no return value, in which null is returned.</li>
     * </ul>
     *
     * <p>The default timeout for a script to be executed is 0ms. In most cases, including the
     * examples below, one must set the script timeout {@link
     * WebDriver.Timeouts} beforehand to a value sufficiently large
     * enough.
     *
     * <p>Example: Injecting a XMLHttpRequest and waiting for the result:
     *
     * <pre>{@code
     * Object response = JsUtility.runAsyncAndReturn(driver,
     *     "var callback = arguments[arguments.length - 1];" +
     *     "var xhr = new XMLHttpRequest();" +
     *     "xhr.open('GET', '/resource/data.json', true);" +
     *     "xhr.onreadystatechange = function() {" +
     *     "  if (xhr.readyState == 4) {" +
     *     "    callback(xhr.responseText);" +
     *     "  }" +
     *     "};" +
     *     "xhr.send();");
     * JsonObject json = new JsonParser().parse((String) response);
     * assertEquals("cheese", json.get("food").getAsString());
     * }</pre>
     *
     * <p>Script arguments must be a number, a boolean, a String, WebElement, or a List of any
     * combination of the above. An exception will be thrown if the arguments do not meet these
     * criteria. The arguments will be made available to the JavaScript via the "arguments" variable.
     *
     * @param <T> return type
     * @param driver A handle to the currently running Selenium test window.
     * @param js The JavaScript to execute.
     * @param args The arguments to the script. May be empty.
     * @return One of Boolean, Long, String, List, Map, WebElement, or null.
     * @see WebDriver.Timeouts
     * @see JavascriptExecutor#executeAsyncScript(String, Object...)
     */
    @SuppressWarnings("unchecked") // required because Selenium is not type safe.
    public static <T> T runAsyncAndReturn(final WebDriver driver, final String js, final Object... args) {
        Object result = runScript(true, driver, js, args);
        if (result == null) {
            LOGGER.warn("The specified asynchronous JavaScript returned a null result");
        }
        return (T) result;
    }

    /**
     * Execute the specified piece of JavaScript in the context of the currently selected frame or
     * window.
     * 
     * <ul>
     *     <li>The caller specifies whether to execute the script synchronously or asynchronously.</li>
     *     <li>If the script execution triggers a {@code WebDriverException}, the exception message is
     *         scanned for a serialized exception. 
     *         <ul>
     *             <li>If a serialized exception is found, this exception is de-serialized and thrown.</li>
     *             <li>If no serialized exception is found, the original exception is thrown.</li>
     *         </ul>
     *     </li>
     *     <li>[Safari] If the script returns a {@code Map} object, this map is scanned for a serialized
     *         exception. 
     *         <ul>
     *             <li>If a serialized exception is found, this exception is de-serialized and thrown.</li>
     *         </ul>
     *     </li>
     * </ul>
     * 
     * @param doAsync {@code true} to execute asynchronously; {@code false} to execute synchronously
     * @param driver A handle to the currently running Selenium test window.
     * @param js The JavaScript to execute.
     * @param args The arguments to the script. May be empty.
     * @return One of Boolean, Long, String, List, Map, WebElement, or null.
     */
    private static Object runScript(final boolean doAsync,
            final WebDriver driver, final String js, final Object... args) {
        
        Object result = null;
        WebDriverException exception = null;
        
        String script = FirefoxShadowRoot.injectShadowArgs(driver, js, args);
        try {
            if (doAsync) {
                result = ((JavascriptExecutor) driver).executeAsyncScript(script, args);
            } else {
                result = ((JavascriptExecutor) driver).executeScript(script, args);
            }
        } catch (WebDriverException e) {
            exception = e;
            result = e.getMessage();
        }
        
        if (result != null) {
            Throwable thrown = null;
            if (result instanceof Map) {
                thrown = extractException(exception, (Map<?, ?>) result);
            } else if (result instanceof String) {
                thrown = extractException(exception, (String) result);
            }
            if (thrown != null) {
                UncheckedThrow.throwUnchecked(thrown);
            }
        }
        return result;
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
                return runAndReturn(WebDriverUtils.getDriver(context), DOCUMENT_READY);
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
     * Inject the Java glue code library into the current window.
     * 
     * @param driver A handle to the currently running Selenium test window.
     */
    public static void injectGlueLib(final WebDriver driver) {
        if ((boolean) runAndReturn(driver, "return (typeof isObject != 'function');")) {
            run(driver, CREATE_SCRIPT_NODE, getScriptResource(JAVA_GLUE_LIB));
        }
    }
    
    /**
     * Get the content of the name resource
     * 
     * @param resource resource filename
     * @return resource file content
     */
    public static String getScriptResource(final String resource) {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try {
            try (InputStream is = classLoader.getResourceAsStream(resource)) {
                return (is != null) ? new String(readAllBytes(is), UTF_8) : null;
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to load JavaScript resource '" + resource + "'", e);
        }
    }
    
    /**
     * Propagate the specified web driver exception, extracting encoded JavaScript exception if present.
     * 
     * @param driver A handle to the currently running Selenium test window.
     * @param exception web driver exception to propagate (may be {@code null})
     * @return nothing (this method always throws the specified exception)
     * @since 17.4.0 
     */
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
                for (LogEntry logEntry : logEntries) {
                    if (Level.WARNING.equals(logEntry.getLevel())) {
                        // extract serialized exception object from message
                        thrown = extractException(exception, logEntry.getMessage());
                        // done if serialized exception found
                        if (!thrown.equals(exception)) break;
                    }
                }
            }
        }
        // throw resolved exception as unchecked
        throw UncheckedThrow.throwUnchecked(thrown);
    }

    /**
     * If present, extract JSON-formatted serialized exception object from the specified message.
     * <p>
     * <b>NOTE</b>: If the message contains a serialized exception object, the exception is de-serialized with its cause
     * set to the specified {@code WebDriverException}. If no serialized exception is found, the specified exception is
     * returned instead.
     * 
     * @param exception web driver exception (may be {@code null})
     * @param message message to scan for serialized exception object
     * @return de-serialized exception; specified exception is none is found
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
     * @param exception web driver exception to propagate (may be {@code null})
     * @param jsonStr JSON string
     * @return if present, exception decoded from JSON; otherwise, original WebDriverException object
     */
    private static Throwable deserializeException(final WebDriverException exception, final String jsonStr) {
        Throwable thrown = exception;
        // if message appears to be an encoded exception object
        if (jsonStr.contains("\"" + CLASS_NAME_KEY + "\"") && jsonStr.contains("\"" + MESSAGE_KEY + "\"")) {
            Map<String, ?> obj = DataUtils.fromString(jsonStr, HashMap.class);
            
            // if successful
            if (obj != null) {
                thrown = extractException(exception, obj);
            } else {
                LOGGER.warn("Unable to deserialize encoded exception object: {}", jsonStr);
            }
        }
        return thrown;
    }

    /**
     * If present, extract serialized exception object from the specified map.
     * <p>
     * <b>NOTE</b>: If the map contains a serialized exception object, the exception is de-serialized with its cause
     * set to the specified {@code WebDriverException}. If no serialized exception is found, the specified exception is
     * returned instead.
     * 
     * @param exception web driver exception (may be {@code null})
     * @param obj map to scan for serialized exception object
     * @return de-serialized exception; specified exception is none is found
     */
    private static Throwable extractException(final WebDriverException exception, Map<?, ?> obj) {
        Throwable thrown = null;
        if (obj.containsKey(ERROR_MESSAGE_KEY)) {
            obj = (Map<?, ?>) obj.get(ERROR_MESSAGE_KEY);
        }
        
        String className = (String) obj.get(CLASS_NAME_KEY);
        String message = (String) obj.get(MESSAGE_KEY);
        
        if (className != null && message != null) {
            try {
                Class<?> clazz = Class.forName(className);
                Constructor<?> ctor = clazz.getConstructor(String.class, Throwable.class);
                thrown = (Throwable) ctor.newInstance(message, exception);
                thrown.setStackTrace(new Throwable().getStackTrace());
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException
                    | InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException eaten) {
                LOGGER.warn("Unable to instantiate exception: {}", className, eaten);
            }
        }
        return thrown;
    }
}
