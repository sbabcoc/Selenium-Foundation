package com.nordstrom.automation.selenium.grid;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;

/**
 * This class extracts node capabilities from a Selenium 3 Grid proxy status response.
 *
 * @since [next-major]
 */
public class GridProxyResponseV3 {

    /**
     * Extract node capabilities from the specified proxy status response object.
     *
     * @param obj proxy status response object
     * @return list of {@link Capabilities} objects; empty list if parsing fails
     */
    @SuppressWarnings("unchecked")
    public static List<Capabilities> fromJson(Object obj) {
        try {
            Map<String, ?> input = (Map<String, ?>) obj;
            Map<String, ?> request = (Map<String, ?>) input.get("request");
            Map<String, ?> configuration = (Map<String, ?>) request.get("configuration");
            List<Map<String, String>> capabilities =
                    (List<Map<String, String>>) configuration.get("capabilities");
            return capabilities.stream()
                    .map(MutableCapabilities::new)
                    .collect(Collectors.toList());
        } catch (NullPointerException | ClassCastException eaten) {
            // nothing to do here
        }
        return Collections.emptyList();
    }
}
