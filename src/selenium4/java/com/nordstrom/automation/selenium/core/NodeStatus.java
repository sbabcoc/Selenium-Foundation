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
import org.openqa.selenium.json.Json;
import org.openqa.selenium.json.JsonInput;
import com.nordstrom.automation.selenium.utility.DataUtils;

/**
 * This class is used to extract the information from a node status GraphQL query.
 * 
 * @see Nodes#NODE_STATUS
 */
public class NodeStatus {
    
    private final NodeId id;
    private final URI uri;
    private final Availability status;
    private final List<Capabilities> capabilities;
    
    /**
     * Constructor for node status object with specified parameters.
     * 
     * @param id node ID
     * @param uri node {@link URI}
     * @param status node {@link Availability}
     * @param capabilities list of node {@link Capabilities}
     */
    public NodeStatus(NodeId id, URI uri, Availability status, List<Capabilities> capabilities) {
        this.id = id;
        this.uri = uri;
        this.status = status;
        this.capabilities = capabilities;
    }
    
    /**
     * Extract node status from the specified JSON input.
     * 
     * @param input {@link JsonInput} object
     * @return extracted {@link NodeStatus} object
     */
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
    
    /**
     * Get the node ID.
     * 
     * @return node ID
     */
    public NodeId getId() {
        return id;
    }
    
    /**
     * Get the node URI.
     * 
     * @return node {@link URI}
     */
    public URI getUri() {
        return uri;
    }
    
    /**
     * Get the node status.
     * 
     * @return node {@link Availability}
     */
    public Availability getStatus() {
        return status;
    }
    
    /**
     * Get list of node capabilities.
     * 
     * @return list of {@link Capabilities}
     */
    public List<Capabilities> getCapabilities() {
        return capabilities;
    }
    
    /**
     * Get the string representation of this node status object.
     * 
     * @return node status as JSON string
     */
    @Override
    public String toString() {
        return new Json().toJson(this);
    }
}
