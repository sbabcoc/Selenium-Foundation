package com.nordstrom.automation.selenium.core;

import static org.openqa.selenium.json.Json.LIST_OF_MAPS_TYPE;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.grid.data.Availability;
import org.openqa.selenium.grid.data.NodeId;
import org.openqa.selenium.json.JsonInput;
import com.nordstrom.automation.selenium.utility.DataUtils;

public class NodeStatus {
    
    private final NodeId id;
    private final URI uri;
    private final Availability status;
    private final List<Capabilities> capabilities;
    
    public NodeStatus(NodeId id, URI uri, Availability status, List<Capabilities> capabilities) {
        this.id = id;
        this.uri = uri;
        this.status = status;
        this.capabilities = capabilities;
    }
    
    @SuppressWarnings("unchecked")
    public static NodeStatus fromJson(JsonInput input) {
        NodeId id = null;
        URI uri = null;
        Availability status = null;
        List<Capabilities> capabilities = null;
        
        input.beginObject();
        while (input.hasNext()) {
            switch (input.nextName()) {
            case "id":
                id = input.read(NodeId.class);
                break;
                
            case "uri":
                uri = input.read(URI.class);
                break;
                
                
            case "status":
                status = input.read(Availability.class);
                break;
                
            case "stereotypes":
                String caps = input.read(String.class);
                List<Map<String, Object>> listOfMaps = DataUtils.fromString(caps, LIST_OF_MAPS_TYPE);
                capabilities = listOfMaps.stream()
                .map(item -> item.get("stereotype"))
                .filter(obj -> obj instanceof Map)
                .map(obj -> (Map<String, Object>) obj)
                .map(map -> new MutableCapabilities(map))
                .collect(Collectors.toList());
                break;
                
            default:
                input.skipValue();
            }
        }
        input.endObject();
        
        return new NodeStatus(id, uri, status, capabilities);
    }
    
    public NodeId getId() {
        return id;
    }
    
    public URI getUri() {
        return uri;
    }
    
    public Availability getStatus() {
        return status;
    }
    
    public List<Capabilities> getCapabilities() {
        return capabilities;
    }
}
