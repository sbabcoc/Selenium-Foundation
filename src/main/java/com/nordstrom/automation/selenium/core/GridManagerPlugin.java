package com.nordstrom.automation.selenium.core;

/**
 * This interface defines the service provider contract for Grid manager implementations.
 * <p>
 * Implementations register their local Grid factory with {@link SeleniumGrid} via a
 * static initializer. The {@link java.util.ServiceLoader} mechanism ensures the
 * implementation is loaded and the factory registered before any Grid operations occur.
 * <p>
 * To register an implementation, create a provider configuration file at:
 * <pre>META-INF/services/com.nordstrom.automation.selenium.core.GridManagerPlugin</pre>
 */
public interface GridManagerPlugin {
    // marker interface - implementations register Grid factory in static initializer
}
