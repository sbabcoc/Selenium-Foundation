package com.nordstrom.automation.selenium.grid;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.json.Json;
import org.openqa.selenium.json.JsonInput;

import com.nordstrom.automation.selenium.core.GridUtility;

/**
 * {@link GridApiProvider} implementation for Selenium 4 Grid instances.
 * <p>
 * Uses the Selenium 4 GraphQL API for grid interaction.
 *
 * @since [next-major]
 */
public class GridApiProviderV4 implements GridApiProvider {

    /**
     * {@inheritDoc}
     */
    @Override
    public int getApiVersion() {
        return 4;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isHub(URL hubUrl) {
        return GridUtility.isSelenium4Hub(hubUrl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<URL> getNodeUrls(URL hubUrl) throws IOException {
        List<URL> nodeList = new ArrayList<>();
        for (NodeStatusV4 node : getStatusOfNodes(hubUrl)) {
            nodeList.add(node.getUri().toURL());
        }
        return nodeList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNodeRegistered(URL hubUrl, URL nodeUrl) {
        try {
            URI nodeUri = URI.create(nodeUrl.getProtocol() + "://" + nodeUrl.getAuthority());
            return getStatusOfNodes(hubUrl).stream()
                    .filter(node -> node.getUri().equals(nodeUri)
                            && "UP".equals(node.getStatus()))
                    .findFirst()
                    .isPresent();
        } catch (NullPointerException | ClassCastException eaten) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Capabilities> getNodeCapabilities(URL hubUrl, URL nodeUrl) throws IOException {
        URI nodeUri = URI.create(nodeUrl.getProtocol() + "://" + nodeUrl.getAuthority());
        return getStatusOfNodes(hubUrl).stream()
                .filter(node -> node.getUri().equals(nodeUri))
                .map(NodeStatusV4::getCapabilities)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * Get status of the nodes registered with the specified Selenium 4 Grid hub.
     *
     * @param hubUrl {@link URL} of Grid hub
     * @return list of {@link NodeStatusV4} objects; empty list on error
     */
    private static List<NodeStatusV4> getStatusOfNodes(URL hubUrl) {
        try {
            HttpResponse response = GridUtility.callGraphQLService(hubUrl, NodesV4.NODES_INFO_QUERY);
            String json = EntityUtils.toString(response.getEntity());
            JsonInput input = new Json().newInput(new StringReader(json));
            List<NodeStatusV4> nodes = NodesV4.fromJson(input);
            return nodes != null ? nodes : Collections.emptyList();
        } catch (IOException eaten) {
            return Collections.emptyList();
        }
    }
}
