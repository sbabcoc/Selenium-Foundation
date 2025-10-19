package com.nordstrom.automation.selenium.plugins;

import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This utility class contains low-level methods that enable starting specified activities of <b>Android</b>
 * applications. The implementation uses {@code mobile: startActivity} with the <b>Espresso</b> engine and
 * {@code mobile: shell} with the <b>UiAutomator2</b> engine.
 */
public class AndroidActivityLauncher {
    
    /**
     * Private constructor to prevent instantiation.
     */
    private AndroidActivityLauncher() {
        throw new AssertionError("AndroidActivityLauncher is a static utility class that cannot be instantiated");
    }
    
    /**
     * Start the Android activity indicated by the specified URL.
     * <p>
     * <b>NOTE</b>: The URL string implements the following format: <ul>
     *   <li><b>scheme</b>: activity</li>
     *   <li><b>host</b>: Android application package name</li>
     *   <li><b>path</b>: Android application activity name</li>
     *     <li><b>query parameters [optional]</b>: <ul>
     *       <li><b>action</b>: intent action (e.g. - {@code android.intent.action.MAIN})</li>
     *       <li><b>category</b>: intent category (e.g. - {@code android.intent.category.LAUNCHER})</li>
     *       <li><b>(intent arguments)</b>: intent arguments as key/value pairs: <ul>
     *        <li><b>NOTE</b>: Argument keys should be prefixed with one of these type specifiers: <ul>
     *          <li><b>es:</b> = string value</li>
     *          <li><b>ei:</b> = integer value</li>
     *          <li><b>el:</b> = long value</li>
     *          <li><b>ef:</b> = float value</li>
     *          <li><b>ed:</b> = double value</li>
     *          <li><b>ez:</b> = boolean value</li>
     *        </ul></li>
     *        <li><b>EXAMPLE</b>: {"es:name": "Dennis", "ei:age": 37, "ez:is-king", false}</li>
     *      </ul></li>
     *   </ul></li>
     * </ul>
     * <b>EXAMPLE</b>: {@code activity://io.appium.android.apis/.app.SearchInvoke} <br>
     * <b>NOTE</b>: Unqualified (relative) activity names like this are prefixed with the package
     * name to form a fully-qualified name (e.g. - {@code io.appium.android.apis.app.SearchInvoke})
     * 
     * @param driver Android driver
     * @param activityUrl activity specifier encoded as a URL string
     */
    public static void startAndroidActivity(final WebDriver driver, final String activityUrl) {
        URI uri = URI.create(activityUrl);
        if (!"activity".equals(uri.getScheme())) {
            throw new IllegalArgumentException("Unsupported scheme: " + uri.getScheme());
        }

        String authority = uri.getAuthority(); // package
        String path = uri.getPath();           // activity (optional)
        if (path != null && path.startsWith("/")) path = path.substring(1);

        Map<String, List<String>> params = parseQueryParams(uri);
        String action = removeSingleParam(params, "action");
        String category = removeSingleParam(params, "category");
        List<String> intentArgs = getIntentArgs(params);

        // Detect active automation engine
        String engine = getAutomationEngine((HasCapabilities) driver);

        if ("Espresso".equalsIgnoreCase(engine)) {
            startActivityViaScript((JavascriptExecutor) driver, authority, path, action, category, intentArgs);
        } else if ("UiAutomator2".equalsIgnoreCase(engine)) {
            startActivityViaShell((JavascriptExecutor) driver, authority, path, action, category, intentArgs);
        } else {
            throw new UnsupportedOperationException("Unsupported automation engine: " + engine);
        }
    }

    /**
     * Parse the query parameters of the specified URI.
     * <p>
     * <b>NOTE</b>: This method supports parsing of repeated parameters.
     * 
     * @param uri URI from which to parse query parameters
     * @return map of lists of strings
     */
    private static Map<String, List<String>> parseQueryParams(final URI uri) {
        Map<String, List<String>> rawParams = new LinkedHashMap<>();
        String query = uri.getRawQuery();
        if (query == null) return Collections.emptyMap();

        for (String pair : query.split("&")) {
            int idx = pair.indexOf('=');
            String key = idx > 0 ? pair.substring(0, idx) : pair;
            String value = idx > 0 && pair.length() > idx + 1 ? pair.substring(idx + 1) : "";
            key = URLDecoder.decode(key, StandardCharsets.UTF_8);
            value = URLDecoder.decode(value, StandardCharsets.UTF_8);
            rawParams.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }
        
        return rawParams;
    }

    /**
     * Remove/return the specified parameter from the provided map.
     * <p>
     * <b>NOTE</b>: The specified parameter CANNOT define multiple values. <br>
     * <b>NOTE</b>: The parameter is removed from the provided map if found.
     * 
     * @param params map of lists of parameter values
     * @param key name of parameter to be retrieved
     * @return value of specified parameter; {@code null} if undefined
     * @throws IllegalStateException if specified parameter defines multiple values
     */
    private static String removeSingleParam(final Map<String, List<String>> params, final String key) {
        List<String> values = params.remove(key);
        if (values == null) return null;
        if (values.size() != 1)
            throw new IllegalStateException("Expected exactly one value for key: " + key);
        return values.get(0);
    }

    /**
     * Get the automation engine associated with the specified driver.
     * 
     * @param caps Android driver as <b>HasCapabilities</b> object
     * @return Appium automation engine name (Espresso/UiAutomator2)
     */
    private static String getAutomationEngine(final HasCapabilities caps) {
        return Optional
                .ofNullable((String) caps.getCapabilities().getCapability("appium:automationName"))
                .orElse((String) caps.getCapabilities().getCapability("automationName"));
    }

    /**
     * Start the specified activity via the {@code mobile: startActivity} script.
     * 
     * @param driver Android driver as <b>JavascriptExecutor</b>
     * @param appPackage Android application package name
     * @param activity Android application activity name
     * @param action [optional] intent action (e.g. - {@code android.intent.action.MAIN})
     * @param category [optional] intent category (e.g. - {@code android.intent.category.LAUNCHER})
     * @param intentArgs [optional] intent arguments
     */
    private static void startActivityViaScript(final JavascriptExecutor driver, final String appPackage,
            final String activity, final String action, final String category, final List<String> intentArgs) {
        Map<String, Object> args = new HashMap<>();

        args.put("appPackage", appPackage);
        args.put("appActivity", activity);
        
        if (action != null) {
            args.put("intentAction", action);
        }

        if (category != null) {
            args.put("intentCategory", category);
        }
        
        if (!intentArgs.isEmpty()) {
            args.put("optionalIntentArguments", intentArgs);
        }

        driver.executeScript("mobile: startActivity", args);
    }
    
    /**
     * Start the specified activity via the {@code mobile: shell} script.
     * 
     * @param driver Android driver as <b>JavascriptExecutor</b>
     * @param appPackage Android application package name
     * @param activity Android application activity name
     * @param action [optional] intent action (e.g. - {@code android.intent.action.MAIN})
     * @param category [optional] intent category (e.g. - {@code android.intent.category.LAUNCHER})
     * @param intentArgs [optional] intent arguments
     */
    private static void startActivityViaShell(final JavascriptExecutor driver, final String appPackage,
            final String activity, final String action, final String category, final List<String> intentArgs) {

        // Build the am start command
        List<String> cmd = new ArrayList<>();
        cmd.add("start");
        cmd.add("-n");
        cmd.add(appPackage + "/" + activity);

        if (action != null) {
            cmd.add("-a");
            cmd.add(action);
        }

        if (category != null) {
            cmd.add("-c");
            cmd.add(category);
        }

        for (String intentArg : intentArgs) {
            cmd.add(intentArg);
        }

        // Execute via mobile: shell
        Map<String, Object> args = new HashMap<>();
        args.put("command", "am");
        args.put("args", cmd.toArray(new String[0]));

        driver.executeScript("mobile: shell", args);
    }
    
    /**
     * Get application activity intent arguments.
     * 
     * @param params map of lists of parameter values
     * @return list of intent arguments as [type, key, value] triples
     */
    private static List<String> getIntentArgs(final Map<String, List<String>> params) {
        Stream<Entry<String, List<String>>> paramStream = params.entrySet().stream();

        return paramStream.flatMap(entry -> {
            String key = entry.getKey();
            String prefix = key.contains(":") ? key.substring(0, key.indexOf(':')) : "es";
            String cleanKey = key.contains(":") ? key.substring(key.indexOf(':') + 1) : key;

            // Convert multiple values into a single flattened string
            return Stream.of("--" + prefix, cleanKey, String.join(",", entry.getValue()));
        }).collect(Collectors.toList());
    }
}
