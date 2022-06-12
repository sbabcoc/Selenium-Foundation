package com.nordstrom.automation.selenium.utility;

import java.lang.reflect.Type;

import org.openqa.selenium.json.Json;
import org.openqa.selenium.json.JsonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This static utility class contains methods related to representational transformation.
 */
public final class DataUtils {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DataUtils.class);
    
    /**
     * Private constructor to prevent instantiation.
     */
    private DataUtils() {
        throw new AssertionError("DataUtils is a static utility class that cannot be instantiated");
    }
    
    /**
     * Transform the specified JSON string into the specified type.
     * <p>
     * <b>NOTE</b>: For this method to work correctly, the specified type must conform to the
     * <a href=https://stackoverflow.com/a/3295517>JavaBeans</a> specification. For a simple example, see the
     * {@link com.nordstrom.automation.selenium.listeners.PlatformInterceptor.PlatformIdentity PlatformIdentity}
     * class.
     * 
     * @param <T> desired object type
     * @param json JSON object string
     * @param type target object type
     * @return new instance of the specified type
     */
    public static <T> T fromString(final String json, final Type type) {
        try {
            return new Json().toType(json, type);
        } catch (JsonException e) {
            LOGGER.debug("Failed to deserialize JSON object string: {}" + json, e.getMessage());
            return null;
        }
    }

    /**
     * Transform the specified Java object into its JSON string representation.
     * <p>
     * <b>NOTE</b>: For this method to work correctly, the specified object must conform to the
     * <a href=https://stackoverflow.com/a/3295517>JavaBeans</a> specification. For a simple example, see the
     * {@link com.nordstrom.automation.selenium.listeners.PlatformInterceptor.PlatformIdentity PlatformIdentity}
     * class.
     * 
     * @param object Java object to be transformed
     * @return JSON representation of {@code object}
     */
    public static String toString(final Object object) {
        return new Json().toJson(object);
    }
    
}
