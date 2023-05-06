package com.nordstrom.automation.selenium.api;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;

public class GridProxyResponse {
    
    @SuppressWarnings("unchecked")
    public static List<Capabilities> fromJson(Object obj) {
        try {
            Map<String, ?> input = (Map<String, ?>) obj;
            Map<String, ?> request = (Map<String, ?>) input.get("request");
            Map<String, ?> configuration = (Map<String, ?>) request.get("configuration");
            List<Map<String, String>> capabilities = (List<Map<String, String>>) configuration.get("capabilities");
            return capabilities.stream().map(MutableCapabilities::new).collect(Collectors.toList());
        } catch (NullPointerException | ClassCastException eaten) {
            // nothing to do here
        }
        
        return Collections.emptyList();
    }
}
