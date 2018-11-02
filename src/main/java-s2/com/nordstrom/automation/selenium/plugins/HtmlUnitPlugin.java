package com.nordstrom.automation.selenium.plugins;

import com.nordstrom.automation.selenium.DriverPlugin;

public class HtmlUnitPlugin implements DriverPlugin {
    
    /**
     * <b>org.openqa.selenium.htmlunit.HtmlUnitDriver</b>
     * 
     * <pre>&lt;dependency&gt;
     *   &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *   &lt;artifactId&gt;htmlunit-driver&lt;/artifactId&gt;
     *   &lt;version&gt;2.21&lt;/version&gt;
     * &lt;/dependency&gt;</pre>
     * 
     * <b>com.gargoylesoftware.htmlunit.Version</b>
     * 
     * <pre>&lt;dependency&gt;
     *   &lt;groupId&gt;net.sourceforge.htmlunit&lt;/groupId&gt;
     *   &lt;artifactId&gt;htmlunit&lt;/artifactId&gt;
     *   &lt;version&gt;2.21&lt;/version&gt;
     * &lt;/dependency&gt;</pre>
     * 
     * <b>org.apache.commons.collections.ListUtils</b>
     * 
     * <pre>&lt;dependency&gt;
     *   &lt;groupId&gt;commons-collections&lt;/groupId&gt;
     *   &lt;artifactId&gt;commons-collections&lt;/artifactId&gt;
     *   &lt;version&gt;3.2.2&lt;/version&gt;
     * &lt;/dependency&gt;</pre>
     * 
     * <b>org.apache.xalan.Version</b>
     * 
     * <pre>&lt;dependency&gt;
     *   &lt;groupId&gt;xalan&lt;/groupId&gt;
     *   &lt;artifactId&gt;xalan&lt;/artifactId&gt;
     *   &lt;version&gt;2.7.2&lt;/version&gt;
     * &lt;/dependency&gt;</pre>
     * 
     * <b>org.apache.xml.serializer.Version</b>
     * 
     * <pre>&lt;dependency&gt;
     *   &lt;groupId&gt;xalan&lt;/groupId&gt;
     *   &lt;artifactId&gt;serializer&lt;/artifactId&gt;
     *   &lt;version&gt;2.7.2&lt;/version&gt;
     * &lt;/dependency&gt;</pre>
     * 
     * <b>org.apache.commons.lang3.CharSet</b>
     * 
     * <pre>&lt;dependency&gt;
     *   &lt;groupId&gt;org.apache.commons&lt;/groupId&gt;
     *   &lt;artifactId&gt;commons-lang3&lt;/artifactId&gt;
     *   &lt;version&gt;3.4&lt;/version&gt;
     * &lt;/dependency&gt;</pre>
     * 
     * <b>org.apache.http.client.HttpClient</b>
     * 
     * <pre>&lt;dependency&gt;
     *   &lt;groupId&gt;org.apache.httpcomponents&lt;/groupId&gt;
     *   &lt;artifactId&gt;httpclient&lt;/artifactId&gt;
     *   &lt;version&gt;4.5.2&lt;/version&gt;
     * &lt;/dependency&gt;</pre>
     * 
     * <b>org.apache.http.entity.mime.MIME</b>
     * 
     * <pre>&lt;dependency&gt;
     *   &lt;groupId&gt;org.apache.httpcomponents&lt;/groupId&gt;
     *   &lt;artifactId&gt;httpmime&lt;/artifactId&gt;
     *   &lt;version&gt;4.5.2&lt;/version&gt;
     * &lt;/dependency&gt;</pre>
     * 
     * <b>org.apache.commons.codec.Encoder</b>
     * 
     * <pre>&lt;dependency&gt;
     *   &lt;groupId&gt;commons-codec&lt;/groupId&gt;
     *   &lt;artifactId&gt;commons-codec&lt;/artifactId&gt;
     *   &lt;version&gt;1.10&lt;/version&gt;
     * &lt;/dependency&gt;</pre>
     * 
     * <b>net.sourceforge.htmlunit.corejs.javascript.Icode</b>
     * 
     * <pre>&lt;dependency&gt;
     *   &lt;groupId&gt;net.sourceforge.htmlunit&lt;/groupId&gt;
     *   &lt;artifactId&gt;htmlunit-core-js&lt;/artifactId&gt;
     *   &lt;version&gt;2.17&lt;/version&gt;
     * &lt;/dependency&gt;</pre>
     * 
     * <b>net.sourceforge.htmlunit.cyberneko.LostText</b>
     * 
     * <pre>&lt;dependency&gt;
     *   &lt;groupId&gt;net.sourceforge.htmlunit&lt;/groupId&gt;
     *   &lt;artifactId&gt;neko-htmlunit&lt;/artifactId&gt;
     *   &lt;version&gt;2.21&lt;/version&gt;
     * &lt;/dependency&gt;</pre>
     * 
     * <b>org.apache.xerces.parsers.XMLParser</b>
     * 
     * <pre>&lt;dependency&gt;
     *   &lt;groupId&gt;xerces&lt;/groupId&gt;
     *   &lt;artifactId&gt;xercesImpl&lt;/artifactId&gt;
     *   &lt;version&gt;2.11.0&lt;/version&gt;
     * &lt;/dependency&gt;</pre>
     * 
     * <b>org.apache.xmlcommons.Version</b>
     * 
     * <pre>&lt;dependency&gt;
     *   &lt;groupId&gt;xml-apis&lt;/groupId&gt;
     *   &lt;artifactId&gt;xml-apis&lt;/artifactId&gt;
     *   &lt;version&gt;1.4.01&lt;/version&gt;
     * &lt;/dependency&gt;</pre>
     * 
     * <b>com.steadystate.css.parser.Token</b>
     * 
     * <pre>&lt;dependency&gt;
     *   &lt;groupId&gt;net.sourceforge.cssparser&lt;/groupId&gt;
     *   &lt;artifactId&gt;cssparser&lt;/artifactId&gt;
     *   &lt;version&gt;0.9.18&lt;/version&gt;
     * &lt;/dependency&gt;</pre>
     * 
     * <b>org.w3c.css.sac.Parser</b>
     * 
     * <pre>&lt;dependency&gt;
     *   &lt;groupId&gt;org.w3c.css&lt;/groupId&gt;
     *   &lt;artifactId&gt;sac&lt;/artifactId&gt;
     *   &lt;version&gt;1.3&lt;/version&gt;
     * &lt;/dependency&gt;</pre>
     * 
     * <b>org.apache.commons.io.IOUtils</b>
     * 
     * <pre>&lt;dependency&gt;
     *   &lt;groupId&gt;commons-io&lt;/groupId&gt;
     *   &lt;artifactId&gt;commons-io&lt;/artifactId&gt;
     *   &lt;version&gt;2.4&lt;/version&gt;
     * &lt;/dependency&gt;</pre>
     * 
     * <b>org.apache.commons.logging.Log</b>
     * 
     * <pre>&lt;dependency&gt;
     *   &lt;groupId&gt;commons-logging&lt;/groupId&gt;
     *   &lt;artifactId&gt;commons-logging&lt;/artifactId&gt;
     *   &lt;version&gt;1.2&lt;/version&gt;
     * &lt;/dependency&gt;</pre>
     * 
     * <b>org.eclipse.jetty.websocket.client.WebSocketClient</b>
     * 
     * <pre>&lt;dependency&gt;
     *   &lt;groupId&gt;org.eclipse.jetty.websocket&lt;/groupId&gt;
     *   &lt;artifactId&gt;websocket-client&lt;/artifactId&gt;
     *   &lt;version&gt;9.2.15.v20160210&lt;/version&gt;
     * &lt;/dependency&gt;</pre>
     * 
     * <b>org.eclipse.jetty.util.IO</b>
     * 
     * <pre>&lt;dependency&gt;
     *   &lt;groupId&gt;org.eclipse.jetty&lt;/groupId&gt;
     *   &lt;artifactId&gt;jetty-util&lt;/artifactId&gt;
     *   &lt;version&gt;9.2.15.v20160210&lt;/version&gt;
     * &lt;/dependency&gt;</pre>
     * 
     * <b>org.eclipse.jetty.io.EndPoint</b>
     * 
     * <pre>&lt;dependency&gt;
     *   &lt;groupId&gt;org.eclipse.jetty&lt;/groupId&gt;
     *   &lt;artifactId&gt;jetty-io&lt;/artifactId&gt;
     *   &lt;version&gt;9.2.15.v20160210&lt;/version&gt;
     * &lt;/dependency&gt;</pre>
     * 
     * <b>org.eclipse.jetty.websocket.common.Parser</b>
     * 
     * <pre>&lt;dependency&gt;
     *   &lt;groupId&gt;org.eclipse.jetty.websocket&lt;/groupId&gt;
     *   &lt;artifactId&gt;websocket-common&lt;/artifactId&gt;
     *   &lt;version&gt;9.2.15.v20160210&lt;/version&gt;
     * &lt;/dependency&gt;</pre>
     * 
     * <b>org.eclipse.jetty.websocket.api.Session</b>
     * 
     * <pre>&lt;dependency&gt;
     *   &lt;groupId&gt;org.eclipse.jetty.websocket&lt;/groupId&gt;
     *   &lt;artifactId&gt;websocket-api&lt;/artifactId&gt;
     *   &lt;version&gt;9.2.15.v20160210&lt;/version&gt;
     * &lt;/dependency&gt;</pre>
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
                    "org.eclipse.jetty.websocket.api.Session"                    };
    
    private static final String BROWSER_NAME = "htmlunit";

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getDependencyContexts() {
        return DEPENDENCY_CONTEXTS;
    }

    @Override
    public String getBrowserName() {
        return BROWSER_NAME;
    }
}
