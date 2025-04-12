package com.nordstrom.automation.selenium.plugins;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.nordstrom.automation.selenium.DriverPlugin;

/**
 * This static utility class contains support methods for <b>Local Selenium Grid</b> plug-ins.
 */
public class PluginUtils {
    
    private static final String PLUGIN_PACKAGE_NAME = PluginUtils.class.getPackage().getName();
    private static final Logger LOGGER = LoggerFactory.getLogger(PluginUtils.class);
    
    private PluginUtils() {
        throw new AssertionError("PluginUtils is a static constants class that cannot be instantiated");
    }

    /**
     * Get "personalities" from the plug-in that supports the specified browser.
     * <p>
     * <b>NOTE</b>: This method uses the {@link ClassLoader} of the current thread to search for candidate classes,
     *              and only plug-in classes in the <i>com.nordstrom.automation.selenium.plugins</i> package are
     *              considered.
     * 
     * @param browserName browser name
     * @return map: "personality" &rarr; desired capabilities (JSON); empty if plug-in for browser not found
     */
    public static Map<String, String> getPersonalitiesForBrowser(String browserName) {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            ClassPath classPath = ClassPath.from(classLoader);
            for (ClassInfo classInfo : classPath.getTopLevelClasses(PLUGIN_PACKAGE_NAME)) {
                DriverPlugin driverPlugin = pluginForName(classInfo.getName());
                if ((driverPlugin != null) && (driverPlugin.getBrowserName().equals(browserName))) {
                    return driverPlugin.getPersonalities();
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed building class path model", e);
        }
        LOGGER.warn("No plug-in for browser '{}' found in package: {}", browserName, PLUGIN_PACKAGE_NAME);
        return Collections.emptyMap();
    }
    
    /**
     * Create an instance of the specified plug-in class.
     * 
     * @param candidateName candidate class name
     * @return {@link DriverPlugin} object; {@code null} if instantiation attempt fails
     */
    private static DriverPlugin pluginForName(String candidateName) {
        try {
            Class<?> clazz = Class.forName(candidateName);
            if (DriverPlugin.class.isAssignableFrom(clazz)) {
                return (DriverPlugin) ConstructorUtils.invokeConstructor(clazz);
            }
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                InvocationTargetException | InstantiationException eaten) {
            // nothing to do here
        }
        return null;
    }
    
}
