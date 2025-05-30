package com.nordstrom.automation.selenium.plugins;

import java.lang.reflect.Constructor;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.nordstrom.automation.selenium.SeleniumConfig;

import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodCall;

/**
 * This class is the plug-in for <b>HtmlUnitDriver</b>.
 */
public class HtmlUnitPlugin extends RemoteWebDriverPlugin {
    
    /**
     * Constructor for <b>HtmlUnitPlugin</b> objects.
     */
    public HtmlUnitPlugin() {
        super(HtmlUnitCaps.DRIVER_NAME);
    }
    
    /**
     * <b>org.openqa.selenium.htmlunit.HtmlUnitDriver</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *  &lt;artifactId&gt;htmlunit-driver&lt;/artifactId&gt;
     *  &lt;version&gt;2.70.0&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.openqa.selenium.By</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *  &lt;artifactId&gt;selenium-api&lt;/artifactId&gt;
     *  &lt;version&gt;3.14.159&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.openqa.selenium.support.FindBy</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *  &lt;artifactId&gt;selenium-support&lt;/artifactId&gt;
     *  &lt;version&gt;3.14.159&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>com.gargoylesoftware.htmlunit.Version</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;net.sourceforge.htmlunit&lt;/groupId&gt;
     *  &lt;artifactId&gt;htmlunit&lt;/artifactId&gt;
     *  &lt;version&gt;2.70.0&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.apache.commons.lang3.CharSet</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.apache.commons&lt;/groupId&gt;
     *  &lt;artifactId&gt;commons-lang3&lt;/artifactId&gt;
     *  &lt;version&gt;3.17.0&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.apache.commons.text.WordUtils</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.apache.commons&lt;/groupId&gt;
     *  &lt;artifactId&gt;commons-text&lt;/artifactId&gt;
     *  &lt;version&gt;1.13.0&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.apache.http.client.HttpClient</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.apache.httpcomponents&lt;/groupId&gt;
     *  &lt;artifactId&gt;httpclient&lt;/artifactId&gt;
     *  &lt;version&gt;4.5.14&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.apache.http.HttpHost</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.apache.httpcomponents.core5&lt;/groupId&gt;
     *  &lt;artifactId&gt;httpcore5&lt;/artifactId&gt;
     *  &lt;version&gt;5.2&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.apache.http.entity.mime.MIME</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.apache.httpcomponents&lt;/groupId&gt;
     *  &lt;artifactId&gt;httpmime&lt;/artifactId&gt;
     *  &lt;version&gt;4.5.14&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.apache.commons.codec.Encoder</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;commons-codec&lt;/groupId&gt;
     *  &lt;artifactId&gt;commons-codec&lt;/artifactId&gt;
     *  &lt;version&gt;1.17.1&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.apache.commons.io.IOUtils</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;commons-io&lt;/groupId&gt;
     *  &lt;artifactId&gt;commons-io&lt;/artifactId&gt;
     *  &lt;version&gt;2.16.1&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.apache.commons.logging.Log</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;commons-logging&lt;/groupId&gt;
     *  &lt;artifactId&gt;commons-logging&lt;/artifactId&gt;
     *  &lt;version&gt;1.3.5&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.eclipse.jetty.websocket.client.WebSocketClient</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.eclipse.jetty.websocket&lt;/groupId&gt;
     *  &lt;artifactId&gt;websocket-client&lt;/artifactId&gt;
     *  &lt;version&gt;9.4.57.v20241219&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.eclipse.jetty.util.IO</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.eclipse.jetty&lt;/groupId&gt;
     *  &lt;artifactId&gt;jetty-util&lt;/artifactId&gt;
     *  &lt;version&gt;9.4.57.v20241219&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.eclipse.jetty.io.EndPoint</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.eclipse.jetty&lt;/groupId&gt;
     *  &lt;artifactId&gt;jetty-io&lt;/artifactId&gt;
     *  &lt;version&gt;9.4.57.v20241219&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.eclipse.jetty.websocket.common.Parser</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.eclipse.jetty.websocket&lt;/groupId&gt;
     *  &lt;artifactId&gt;websocket-common&lt;/artifactId&gt;
     *  &lt;version&gt;9.4.57.v20241219&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.eclipse.jetty.websocket.api.Session</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.eclipse.jetty.websocket&lt;/groupId&gt;
     *  &lt;artifactId&gt;websocket-api&lt;/artifactId&gt;
     *  &lt;version&gt;9.4.57.v20241219&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>net.sourceforge.htmlunit.xpath.XPath</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;net.sourceforge.htmlunit&lt;/groupId&gt;
     *  &lt;artifactId&gt;htmlunit-xpath&lt;/artifactId&gt;
     *  &lt;version&gt;2.70.0&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>net.sourceforge.htmlunit.corejs.javascript.Token</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;net.sourceforge.htmlunit&lt;/groupId&gt;
     *  &lt;artifactId&gt;htmlunit-core-js&lt;/artifactId&gt;
     *  &lt;version&gt;2.70.0&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>net.sourceforge.htmlunit.cyberneko.filters.DefaultFilter</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;net.sourceforge.htmlunit&lt;/groupId&gt;
     *  &lt;artifactId&gt;neko-htmlunit&lt;/artifactId&gt;
     *  &lt;version&gt;2.70.0&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>com.gargoylesoftware.css.util.LangUtils</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;net.sourceforge.htmlunit&lt;/groupId&gt;
     *  &lt;artifactId&gt;htmlunit-cssparser&lt;/artifactId&gt;
     *  &lt;version&gt;1.14.0&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.apache.commons.net.io.Util</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;commons-net&lt;/groupId&gt;
     *  &lt;artifactId&gt;commons-net&lt;/artifactId&gt;
     *  &lt;version&gt;3.9.0&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.eclipse.jetty.client.Origin</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.eclipse.jetty&lt;/groupId&gt;
     *  &lt;artifactId&gt;jetty-client&lt;/artifactId&gt;
     *  &lt;version&gt;9.4.57.v20241219&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.eclipse.jetty.http.Syntax</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.eclipse.jetty&lt;/groupId&gt;
     *  &lt;artifactId&gt;jetty-http&lt;/artifactId&gt;
     *  &lt;version&gt;9.4.57.v20241219&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.brotli.dec.Utils</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.brotli&lt;/groupId&gt;
     *  &lt;artifactId&gt;dec&lt;/artifactId&gt;
     *  &lt;version&gt;0.1.2&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     */
    private static final String[] DEPENDENCY_CONTEXTS = {
        "org.openqa.selenium.htmlunit.HtmlUnitDriver", "org.openqa.selenium.By",
        "org.openqa.selenium.support.FindBy", "com.gargoylesoftware.htmlunit.Version",
        "org.apache.commons.lang3.CharSet", "org.apache.commons.text.WordUtils",
        "org.apache.http.client.HttpClient", "org.apache.http.HttpHost",
        "org.apache.http.entity.mime.MIME", "org.apache.commons.codec.Encoder",
        "org.apache.commons.io.IOUtils", "org.apache.commons.logging.Log",
        "org.eclipse.jetty.websocket.client.WebSocketClient",
        "org.eclipse.jetty.util.IO", "org.eclipse.jetty.io.EndPoint",
        "org.eclipse.jetty.websocket.common.Parser",
        "org.eclipse.jetty.websocket.api.Session",
        "net.sourceforge.htmlunit.xpath.XPath",
        "net.sourceforge.htmlunit.corejs.javascript.Token",
        "net.sourceforge.htmlunit.cyberneko.filters.DefaultFilter",
        "com.gargoylesoftware.css.util.LangUtils",
        "org.apache.commons.net.io.Util", "org.eclipse.jetty.client.Origin",
        "org.eclipse.jetty.http.Syntax", "org.brotli.dec.Utils"
    };
    
    private static final String WEB_ELEMENT_CLASS_NAME =
            "org.openqa.selenium.htmlunit.HtmlUnitWebElement";
    
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
    public Map<String, String> getPersonalities() {
        return HtmlUnitCaps.getPersonalities();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getPropertyNames(String capabilities) {
        return HtmlUnitCaps.getPropertyNames(capabilities);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Implementation getWebElementCtor(WebDriver driver, Class<? extends WebElement> refClass) {
        if (refClass.getName().equals(WEB_ELEMENT_CLASS_NAME)) {
            try {
                Constructor<?> ctor = refClass.getConstructors()[0];
                return MethodCall.invoke(ctor).onSuper().with(driver).with(0).with((Object) null);
            } catch (SecurityException eaten) {
                // nothing to do here
            }
        }
        return null;
    }

}
