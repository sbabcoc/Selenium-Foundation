package com.nordstrom.automation.selenium.core;

import java.lang.reflect.Type;
import java.util.List;

import org.openqa.selenium.json.JsonInput;
import org.openqa.selenium.json.TypeToken;

public class Nodes {

    public static final String NODE_STATUS = "{\"query\":\"{nodesInfo{nodes{id uri status stereotypes}}}\"}";
    private static final Type NODE_STATUSES_TYPE = new TypeToken<List<NodeStatus>>() {}.getType();
    
    public static List<NodeStatus> fromJson(JsonInput input) {
        input.beginObject();
        if (!"data".equals(input.nextName())) return null;
        
        input.beginObject();
        if (!"nodesInfo".equals(input.nextName())) return null;
        
        input.beginObject();
        if (!"nodes".equals(input.nextName())) return null;
        return input.read(NODE_STATUSES_TYPE);
    }
    
}
