package com.nordstrom.automation.selenium.plugins;

import java.util.Map;

import com.nordstrom.automation.selenium.DriverPlugin;
import com.nordstrom.automation.selenium.SeleniumConfig;

public class HtmlUnitPlugin implements DriverPlugin {
    
    /**
     * <b>org.openqa.selenium.htmlunit.HtmlUnitDriver</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *  &lt;artifactId&gt;htmlunit-driver&lt;/artifactId&gt;
     *  &lt;version&gt;2.33.3&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     */
    private static final String[] DEPENDENCY_CONTEXTS = {
                    "org.openqa.selenium.htmlunit.HtmlUnitDriver", "org.openqa.selenium.By",
                    "org.openqa.selenium.support.FindBy", "com.gargoylesoftware.htmlunit.Version",
                    "org.apache.xalan.Version", "org.apache.xml.serializer.Version",
                    "org.apache.commons.lang3.CharSet", "org.apache.commons.text.WordUtils",
                    "org.apache.http.client.HttpClient", "org.apache.http.HttpHost",
                    "org.apache.http.entity.mime.MIME", "org.apache.commons.codec.Encoder",
                    "org.apache.xerces.parsers.XMLParser", "org.apache.xmlcommons.Version",
                    "org.apache.commons.io.IOUtils", "org.apache.commons.logging.Log",
                    "org.eclipse.jetty.websocket.client.WebSocketClient",
                    "org.eclipse.jetty.util.IO", "org.eclipse.jetty.io.EndPoint",
                    "org.eclipse.jetty.websocket.common.Parser",
                    "org.eclipse.jetty.websocket.api.Session",
                    "net.sourceforge.htmlunit.corejs.javascript.Token",
                    "net.sourceforge.htmlunit.cyberneko.filters.DefaultFilter",
                    "org.apache.xmlcommons.Version", "com.gargoylesoftware.css.parser.CSSParser",
                    "org.apache.commons.net.io.Util", "org.eclipse.jetty.client.Origin",
                    "org.eclipse.jetty.http.Syntax", "org.eclipse.jetty.xml.XmlParser"};
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getDependencyContexts() {
        return DEPENDENCY_CONTEXTS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCapabilities(SeleniumConfig config) {
        return HtmlUnitCaps.getCapabilities();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBrowserName() {
        return HtmlUnitCaps.BROWSER_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getPersonalities() {
        return HtmlUnitCaps.getPersonalities();
    }

}
