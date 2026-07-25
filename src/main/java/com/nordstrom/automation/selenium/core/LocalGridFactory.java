package com.nordstrom.automation.selenium.core;

import java.net.URL;

import com.nordstrom.automation.selenium.SeleniumConfig;

/**
 * Factory for creating and activating local {@link SeleniumGrid} instances.
 * <p>
 * Registered via {@link SeleniumGrid#registerLocalGridFactory(LocalGridFactory)},
 * normally by a {@link GridManagerPlugin} implementation's static initializer.
 * <p>
 * Implementations must:
 * <ul>
 *     <li>Create the local Grid instance for the specified hub URL;</li>
 *     <li>Fully activate it (hub and node servers started and confirmed ready)
 *         before returning — callers do not separately call {@code activate()};</li>
 *     <li>Wrap any checked exceptions in an unchecked exception before returning
 *         or throwing, since this functional interface declares none;</li>
 *     <li>Never return {@code null} — throw instead if the Grid cannot be created.</li>
 * </ul>
 *
 * @since [next-major]
 */
@FunctionalInterface
public interface LocalGridFactory {
    /**
     * Create and activate a local Grid instance for the specified hub URL.
     *
     * @param config {@link SeleniumConfig} object
     * @param hubUrl {@link URL} for the hub host to create
     * @return activated {@link SeleniumGrid} instance; never {@code null}
     */
    SeleniumGrid create(SeleniumConfig config, URL hubUrl);
}
