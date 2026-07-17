package com.nordstrom.automation.selenium.grid;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.json.JsonInput;

/**
 * This class is used to query the status of nodes attached to a Selenium 4 Grid hub.
 *
 * @since [next-major]
 */
public class NodesV4 {

    /**
     * GraphQL query to acquire node status information for a Selenium 4 Grid hub.
     */
    public static final String NODES_INFO_QUERY =
            "{\"query\":\"{nodesInfo{nodes{id uri status stereotypes}}}\"}";

    /**
     * Convert the specified JSON input to a list of node status objects.
     *
     * @param input {@link JsonInput} object
     * @return list of {@link NodeStatusV4} objects; {@code null} if response
     *         structure is unexpected
     */
    public static List<NodeStatusV4> fromJson(JsonInput input) {
        input.beginObject();
        if (!"data".equals(input.nextName())) return null;

        input.beginObject();
        if (!"nodesInfo".equals(input.nextName())) return null;

        input.beginObject();
        if (!"nodes".equals(input.nextName())) return null;

        List<NodeStatusV4> nodes = new ArrayList<>();
        input.beginArray();
        while (input.hasNext()) {
            nodes.add(NodeStatusV4.fromJson(input));
        }
        input.endArray();
        return nodes;
    }
}
