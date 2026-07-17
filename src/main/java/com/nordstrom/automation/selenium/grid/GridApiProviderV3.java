package com.nordstrom.automation.selenium.grid;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.json.Json;

import com.nordstrom.automation.selenium.core.GridUtility;
import com.nordstrom.automation.selenium.utility.DataUtils;

/**
 * {@link GridApiProvider} implementation for Selenium 3 Grid instances.
 * <p>
 * Uses the Selenium 3 Grid proxy API and console scraping for grid interaction.
 * When {@code ProxyListServlet} is available (managed grids launched by
 * {@code selenium-grid-manager}), it is preferred over console scraping for
 * node enumeration.
 *
 * @since [next-major]
 */
public class GridApiProviderV3 implements GridApiProvider {

    /**
     * {@inheritDoc}
     */
    @Override
    public int getApiVersion() {
        return 3;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isHub(URL hubUrl) {
        return GridUtility.isSelenium3Hub(hubUrl);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Prefers {@code ProxyListServlet} for managed grids launched by
     * {@code selenium-grid-manager}; falls back to console scraping for
     * off-the-shelf Selenium 3 grids.
     */
    @Override
    public List<URL> getNodeUrls(URL hubUrl) throws IOException {
     // prefer ProxyListServlet if available (managed grids)
        try {
            HttpResponse r = GridUtility.getRawHttpResponse(
                    baseUrl(hubUrl) + "/grid/admin/ProxyListServlet");
            if (r.getStatusLine().getStatusCode() == 200) {
                String json = EntityUtils.toString(r.getEntity(), StandardCharsets.UTF_8);
                List<?> proxies = DataUtils.fromString(json, Json.LIST_OF_MAPS_TYPE);
                List<URL> nodeList = new ArrayList<>();
                if (proxies != null) {
                    for (Object proxy : proxies) {
                        Map<?, ?> proxyMap = (Map<?, ?>) proxy;
                        String id = (String) proxyMap.get("id");
                        if (id != null) nodeList.add(URI.create(id).toURL());
                    }
                    return nodeList;
                }
            }
        } catch (Exception ignored) {}

        // fall back to console scraping for off-the-shelf Selenium 3 grids
        String url = baseUrl(hubUrl) + "/grid/console";
        Document doc = Jsoup.connect(url).get();
        Elements proxyIds = doc.select("p.proxyid");
        List<URL> nodeList = new ArrayList<>();
        for (Element proxyId : proxyIds) {
            String text = proxyId.text();
            int beginIndex = text.indexOf("http");
            int endIndex = text.indexOf(',');
            if (beginIndex >= 0 && endIndex > beginIndex) {
                nodeList.add(URI.create(text.substring(beginIndex, endIndex)).toURL());
            }
        }
        return nodeList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNodeRegistered(URL hubUrl, URL nodeUrl) {
        try {
            String json = getStatusOfNode(hubUrl, nodeUrl);
            Map<String, Object> body = DataUtils.fromString(json, Json.MAP_TYPE);
            return body != null && Boolean.TRUE.equals(body.get("success"));
        } catch (Exception ignored) {}
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Capabilities> getNodeCapabilities(URL hubUrl, URL nodeUrl) throws IOException {
        String json = getStatusOfNode(hubUrl, nodeUrl);
        Map<String, Object> body = DataUtils.fromString(json, Json.MAP_TYPE);
        return GridProxyResponseV3.fromJson(body);
    }

    /**
     * Get the status of the specified node registered with the specified hub.
     *
     * @param hubUrl {@link URL} of Grid hub
     * @param nodeUrl {@link URL} of Grid node
     * @return JSON status string
     * @throws IOException if an I/O error occurs
     */
    private static String getStatusOfNode(URL hubUrl, URL nodeUrl) throws IOException {
        String nodeEndpoint = nodeUrl.getProtocol() + "://" + nodeUrl.getAuthority();
        return getStatusOfNode(hubUrl, "/grid/api/proxy?id=" + nodeEndpoint);
    }

    /**
     * Get the response body from the specified path on the specified hub.
     *
     * @param hubUrl {@link URL} of Grid hub
     * @param path path component including any query string
     * @return response body as a string
     * @throws IOException if an I/O error occurs
     */
    private static String getStatusOfNode(URL hubUrl, String path) throws IOException {
        HttpResponse response = GridUtility.getRawHttpResponse(baseUrl(hubUrl) + path);
        return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
    }

    /**
     * Get the base URL (scheme + authority) of the specified URL.
     *
     * @param url {@link URL} to extract base from
     * @return base URL string
     */
    private static String baseUrl(URL url) {
        return url.getProtocol() + "://" + url.getAuthority();
    }
}
