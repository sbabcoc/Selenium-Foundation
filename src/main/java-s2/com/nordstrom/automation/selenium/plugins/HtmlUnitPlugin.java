package com.nordstrom.automation.selenium.plugins;

import java.util.Map;

import com.nordstrom.automation.selenium.interfaces.DriverPlugin;

public class HtmlUnitPlugin implements DriverPlugin {
    
    /**
     * <b>org.openqa.selenium.htmlunit.HtmlUnitDriver</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;net.sourceforge.htmlunit&lt;/groupId&gt;
     *  &lt;artifactId&gt;htmlunit&lt;/artifactId&gt;
     *  &lt;version&gt;2.21&lt;/version&gt;
     *&lt;/dependency&gt;
     *&lt;dependency&gt;
     *  &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *  &lt;artifactId&gt;htmlunit-driver&lt;/artifactId&gt;
     *  &lt;version&gt;2.21&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     */
    private static final String[] DEPENDENCY_CONTEXTS = {
                    "org.openqa.selenium.htmlunit.HtmlUnitDriver",
                    "com.gargoylesoftware.htmlunit.Version",
                    "org.apache.commons.collections.ListUtils", "org.apache.xalan.Version",
                    "org.apache.xml.serializer.Version", "org.apache.commons.lang3.CharSet",
                    "org.apache.http.client.HttpClient", "org.apache.http.entity.mime.MIME",
                    "org.apache.commons.codec.Encoder",
                    "net.sourceforge.htmlunit.corejs.javascript.Icode",
                    "net.sourceforge.htmlunit.cyberneko.LostText",
                    "org.apache.xerces.parsers.XMLParser", "org.apache.xmlcommons.Version",
                    "com.steadystate.css.parser.Token", "org.w3c.css.sac.Parser",
                    "org.apache.commons.io.IOUtils", "org.apache.commons.logging.Log",
                    "org.eclipse.jetty.websocket.client.WebSocketClient",
                    "org.eclipse.jetty.util.IO", "org.eclipse.jetty.io.EndPoint",
                    "org.eclipse.jetty.websocket.common.Parser",
                    "org.eclipse.jetty.websocket.api.Session"};
    
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
    public String getCapabilities() {
        return HtmlUnitCaps.getCapabilities();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getPersonalities() {
        return HtmlUnitCaps.getPersonalities();
    }

}
