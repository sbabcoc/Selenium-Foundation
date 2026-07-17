package com.nordstrom.automation.selenium.grid;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registry of version-specific {@link GridApiProvider} implementations discovered
 * via {@link ServiceLoader}.
 * <p>
 * Providers are keyed by API version, allowing callers to select the appropriate
 * provider for a given grid instance based on its detected API version.
 *
 * @since 36.0.0
 */
public class GridApiProviderRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(GridApiProviderRegistry.class);

    private static final Map<Integer, GridApiProvider> PROVIDERS;

    static {
        Map<Integer, GridApiProvider> map = new HashMap<>();
        for (GridApiProvider provider : ServiceLoader.load(GridApiProvider.class)) {
            int version = provider.getApiVersion();
            map.put(version, provider);
            LOGGER.debug("Registered GridApiProvider for Selenium {}: {}",
                    version, provider.getClass().getName());
        }
        PROVIDERS = Collections.unmodifiableMap(map);
    }

    private GridApiProviderRegistry() {
        throw new AssertionError("GridApiProviderRegistry is a static utility class " +
                "that cannot be instantiated");
    }

    /**
     * Get the {@link GridApiProvider} for the specified API version.
     *
     * @param apiVersion Selenium API version (e.g. 3 or 4)
     * @return {@link GridApiProvider} for the specified version, or {@code null}
     *         if no provider is registered for that version
     */
    public static GridApiProvider forVersion(int apiVersion) {
        return PROVIDERS.get(apiVersion);
    }

    /**
     * Get the {@link GridApiProvider} appropriate for the specified hub URL by
     * detecting its API version.
     *
     * @param hubUrl {@link URL} of Grid hub
     * @return appropriate {@link GridApiProvider}, or {@code null} if version
     *         cannot be determined
     */
    public static GridApiProvider forHub(URL hubUrl) {
        for (GridApiProvider provider : PROVIDERS.values()) {
            if (provider.isHub(hubUrl)) return provider;
        }
        return null;
    }

    /**
     * Get the set of API versions for which providers are registered.
     *
     * @return unmodifiable set of supported API versions
     */
    public static Set<Integer> getSupportedVersions() {
        return PROVIDERS.keySet();
    }
}
