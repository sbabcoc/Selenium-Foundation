package com.nordstrom.automation.selenium.grid;

import static org.openqa.selenium.json.Json.LIST_OF_MAPS_TYPE;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.json.Json;
import org.openqa.selenium.json.JsonInput;

import com.nordstrom.automation.selenium.utility.DataUtils;

/**
 * This class is used to extract the information from a node status GraphQL query.
 *
 * @see NodesV4#NODES_INFO_QUERY
 * @since 36.0.0
 */
public class NodeStatusV4 {

    private final String id;
    private final URI uri;
    private final String status;
    private final List<Capabilities> capabilities;

    /**
     * Constructor for node status object with specified parameters.
     *
     * @param id node ID string
     * @param uri node {@link URI}
     * @param status node availability status string (e.g. {@code "UP"},
     *        {@code "DRAINING"}, {@code "DOWN"})
     * @param capabilities list of node {@link Capabilities}
     */
    public NodeStatusV4(String id, URI uri, String status, List<Capabilities> capabilities) {
        this.id = id;
        this.uri = uri;
        this.status = status;
        this.capabilities = capabilities;
    }

    /**
     * Extract node status from the specified JSON input.
     *
     * @param input {@link JsonInput} object
     * @return extracted {@link NodeStatusV4} object
     */
    @SuppressWarnings("unchecked")
    public static NodeStatusV4 fromJson(JsonInput input) {
        String id = null;
        URI uri = null;
        String status = null;
        List<Capabilities> capabilities = null;

        input.beginObject();
        while (input.hasNext()) {
            switch (input.nextName()) {
            case "id":
                id = input.read(String.class);
                break;
            case "uri":
                uri = input.read(URI.class);
                break;
            case "status":
                status = input.read(String.class);
                break;
            case "stereotypes":
                String caps = input.read(String.class);
                List<Map<String, Object>> listOfMaps = DataUtils.fromString(caps, LIST_OF_MAPS_TYPE);
                capabilities = listOfMaps.stream()
                        .map(item -> item.get("stereotype"))
                        .filter(obj -> obj instanceof Map)
                        .map(obj -> (Map<String, Object>) obj)
                        .map(MutableCapabilities::new)
                        .collect(Collectors.toList());
                break;
            default:
                input.skipValue();
            }
        }
        input.endObject();

        return new NodeStatusV4(id, uri, status, capabilities);
    }

    /**
     * Get the node ID.
     *
     * @return node ID string
     */
    public String getId() { return id; }

    /**
     * Get the node URI.
     *
     * @return node {@link URI}
     */
    public URI getUri() { return uri; }

    /**
     * Get the node availability status.
     *
     * @return node availability status string (e.g. {@code "UP"},
     *         {@code "DRAINING"}, {@code "DOWN"})
     */
    public String getStatus() { return status; }

    /**
     * Get list of node capabilities.
     *
     * @return list of {@link Capabilities}
     */
    public List<Capabilities> getCapabilities() { return capabilities; }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new Json().toJson(this);
    }
}
