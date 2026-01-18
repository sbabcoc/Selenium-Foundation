package com.nordstrom.automation.selenium.core;

import static java.nio.charset.StandardCharsets.UTF_8;
import static com.nordstrom.automation.selenium.servlet.ExamplePageServlet.readAllBytes;

import java.io.IOException;
import java.io.InputStream;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.exceptions.DocumentNotReadyTimeoutException;
import com.nordstrom.automation.selenium.model.ShadowDomBridge;
import com.nordstrom.automation.selenium.support.Coordinator;
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
    
    private static final String DOCUMENT_READY = getScriptResource("documentReady.js");
    private static final String ENSURE_INSTALLED = getScriptResource("ensureInstalled.js");
    private static final String EXEC_RUNTIME = getScriptResource("scriptExecRuntime.js");
    private static final String RUNTIME_CHECK = getScriptResource("runtimeCheck.js");
    private static final String SYNC_TEMPLATE = getScriptResource("exec-sync.format");
    private static final String ASYNC_TEMPLATE = getScriptResource("exec-async.format");
    
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
        Object result = runScript(false, driver, js, 0, args);
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
        Object result = runScript(false, driver, js, 0, args);
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
     * examples below, one must set the script timeout {@link WebDriver.Timeouts scriptTimeout()}
     * beforehand to a value sufficiently large enough.
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
     * @param timeout asynchronous script timeout in milliseconds
     * @param args The arguments to the script. May be empty.
     * @see WebDriver.Timeouts scriptTimeout()
     * @see JavascriptExecutor#executeAsyncScript(String, Object...)
     */
    public static void runAsync(final WebDriver driver, final String js, long timeout, final Object... args) {
        Object result = runScript(true, driver, js, timeout, args);
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
     * examples below, one must set the script timeout {@link WebDriver.Timeouts scriptTimeout()}
     * beforehand to a value sufficiently large enough.
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
     * @param timeout asynchronous script timeout in milliseconds
     * @param args The arguments to the script. May be empty.
     * @return One of Boolean, Long, String, List, Map, WebElement, or null.
     * @see WebDriver.Timeouts scriptTimeout()
     * @see JavascriptExecutor#executeAsyncScript(String, Object...)
     */
    @SuppressWarnings("unchecked") // required because Selenium is not type safe.
    public static <T> T runAsyncAndReturn(final WebDriver driver, final String js, long timeout,
            final Object... args) {
        Object result = runScript(true, driver, js, timeout, args);
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
     * @param timeout asynchronous script timeout in milliseconds
     * @param args The arguments to the script. May be empty.
     * @return One of Boolean, Long, String, List, Map, WebElement, or null.
     */
    private static Object runScript(final boolean doAsync, final WebDriver driver, final String js, long timeout,
            final Object... args) {

        Object result = null;

        injectRuntime(driver);
        String userScript = ShadowDomBridge.injectShadowArgs(driver, js, args);

        if (doAsync) {
            String script = ASYNC_TEMPLATE
                    .replace("{{USER_SCRIPT}}", userScript)
                    .replace("{{TIMEOUT}}", String.valueOf(timeout));

            result = ((JavascriptExecutor) driver).executeAsyncScript(script, args);
        } else {
            String script = SYNC_TEMPLATE
                    .replace("{{USER_SCRIPT}}", userScript);

            result = ((JavascriptExecutor) driver).executeScript(script, args);
        }

        ScriptResult scriptResult = ScriptResult.from(result);

        if (scriptResult.isError()) {
            Throwable scriptException = SeleniumConfig.getConfig().getExceptionFactory()
                    .create(scriptResult.getExceptionClassName(), scriptResult.getMessage(), scriptResult.getStack());
            throw UncheckedThrow.throwUnchecked(scriptException);
        }

        return scriptResult.getValue();
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
     * Inject the script execution runtime into the current window if not already present.
     *
     * @param driver A handle to the currently running Selenium test window.
     */
    public static void injectRuntime(WebDriver driver) {
        Boolean installed = (Boolean) ((JavascriptExecutor) driver).executeScript(
                ENSURE_INSTALLED, EXEC_RUNTIME, RUNTIME_CHECK);
        
        if (!Boolean.TRUE.equals(installed)) {
            throw new IllegalStateException("Failed to install JavaScript execution runtime");
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
}
