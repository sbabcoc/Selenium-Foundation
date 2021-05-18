package com.nordstrom.automation.selenium.plugins;

import java.lang.reflect.Constructor;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.nordstrom.automation.selenium.SeleniumConfig;

import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodCall;

public class HtmlUnitPlugin extends RemoteWebDriverPlugin {
    
    public HtmlUnitPlugin() {
        super(HtmlUnitCaps.DRIVER_NAME);
    }
    
    /**
     * <b>org.openqa.selenium.htmlunit.HtmlUnitDriver</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *  &lt;artifactId&gt;htmlunit-driver&lt;/artifactId&gt;
     *  &lt;version&gt;2.40.0&lt;/version&gt;
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
                    "org.eclipse.jetty.http.Syntax", "org.brotli.dec.Utils"};
    
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
    public String[] getPropertyNames() {
        return HtmlUnitCaps.getPropertyNames();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Implementation getWebElementCtor(WebDriver driver, Class<? extends WebElement> refClass) {
        if (refClass.getName().equals(WEB_ELEMENT_CLASS_NAME)) {
            try {
                Constructor<?> ctor = refClass.getConstructors()[0];
                return MethodCall.invoke(ctor).onSuper().with(driver).with(Integer.valueOf(0)).with((Object) null);
            } catch (SecurityException e) {
                // nothing to do here
            }
        }
        return null;
    }

}
