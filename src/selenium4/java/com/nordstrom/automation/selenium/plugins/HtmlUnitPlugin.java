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
    
    private static final String[] PROPERTY_NAMES = { 
            "webdriver.htmlunit.webClientVersion", "webdriver.htmlunit.javaScriptEnabled",
            "webdriver.htmlunit.cssEnabled", "webdriver.htmlunit.printContentOnFailingStatusCode",
            "webdriver.htmlunit.throwExceptionOnFailingStatusCode", "webdriver.htmlunit.throwExceptionOnScriptError",
            "webdriver.htmlunit.popupBlockerEnabled", "webdriver.htmlunit.isRedirectEnabled",
            "webdriver.htmlunit.tempFileDirectory", "webdriver.htmlunit.sslClientCertificateStore",
            "webdriver.htmlunit.sslClientCertificateType", "webdriver.htmlunit.sslClientCertificatePassword",
            "webdriver.htmlunit.sslTrustStore", "webdriver.htmlunit.sslTrustStoreType",
            "webdriver.htmlunit.sslTrustStorePassword", "webdriver.htmlunit.sslClientProtocols",
            "webdriver.htmlunit.sslClientCipherSuites", "webdriver.htmlunit.geolocationEnabled",
            "webdriver.htmlunit.doNotTrackEnabled", "webdriver.htmlunit.homePage", "webdriver.htmlunit.proxyConfig",
            "webdriver.htmlunit.timeout", "webdriver.htmlunit.connectionTimeToLive",
            "webdriver.htmlunit.useInsecureSSL", "webdriver.htmlunit.sslInsecureProtocol",
            "webdriver.htmlunit.maxInMemory", "webdriver.htmlunit.historySizeLimit",
            "webdriver.htmlunit.historyPageCacheLimit", "webdriver.htmlunit.localAddress",
            "webdriver.htmlunit.downloadImages", "webdriver.htmlunit.screenWidth", "webdriver.htmlunit.screenHeight",
            "webdriver.htmlunit.webSocketEnabled", "webdriver.htmlunit.webSocketMaxTextMessageSize",
            "webdriver.htmlunit.webSocketMaxTextMessageBufferSize", "webdriver.htmlunit.webSocketMaxBinaryMessageSize",
            "webdriver.htmlunit.webSocketMaxBinaryMessageBufferSize", "webdriver.htmlunit.fetchPolyfillEnabled",
            "webdriver.htmlunit.numericCode", "webdriver.htmlunit.nickname", "webdriver.htmlunit.applicationVersion",
            "webdriver.htmlunit.userAgent", "webdriver.htmlunit.applicationName",
            "webdriver.htmlunit.applicationCodeName", "webdriver.htmlunit.applicationMinorVersion",
            "webdriver.htmlunit.vendor", "webdriver.htmlunit.browserLanguage", "webdriver.htmlunit.isOnline",
            "webdriver.htmlunit.platform", "webdriver.htmlunit.systemTimezone",
            "webdriver.htmlunit.acceptEncodingHeader", "webdriver.htmlunit.acceptLanguageHeader",
            "webdriver.htmlunit.htmlAcceptHeader", "webdriver.htmlunit.imgAcceptHeader",
            "webdriver.htmlunit.cssAcceptHeader", "webdriver.htmlunit.scriptAcceptHeader",
            "webdriver.htmlunit.xmlHttpRequestAcceptHeader", "webdriver.htmlunit.secClientHintUserAgentHeader",
            "webdriver.htmlunit.secClientHintUserAgentPlatformHeader" };
    
    /**
     * <b>org.openqa.selenium.htmlunit.HtmlUnitDriver</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *  &lt;artifactId&gt;htmlunit-driver&lt;/artifactId&gt;
     *  &lt;version&gt;4.11.0.0&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     */
    private static final String[] DEPENDENCY_CONTEXTS = { "org.openqa.selenium.htmlunit.remote.HtmlUnitDriverInfo",
            "org.openqa.selenium.htmlunit.HtmlUnitDriver", "org.openqa.selenium.By",
            "org.openqa.selenium.support.FindBy", "org.htmlunit.Version", "org.apache.commons.lang3.CharSet",
            "org.apache.commons.text.WordUtils", "org.apache.http.client.HttpClient", "org.apache.http.HttpHost",
            "org.apache.http.entity.mime.MIME", "org.apache.commons.codec.Encoder", "org.apache.commons.io.IOUtils",
            "org.apache.commons.logging.Log", "org.htmlunit.jetty.websocket.client.WebSocketClient",
            "org.eclipse.jetty.util.IO", "org.eclipse.jetty.io.EndPoint", "org.htmlunit.jetty.websocket.common.Parser",
            "org.htmlunit.jetty.websocket.api.Session", "org.apache.commons.net.io.Util",
            "org.htmlunit.jetty.client.Origin", "org.eclipse.jetty.http.Syntax", "org.brotli.dec.Utils",
            "net.bytebuddy.matcher.ElementMatcher", "org.htmlunit.corejs.javascript.Symbol",
            "org.htmlunit.cssparser.parser.CSSErrorHandler", "org.htmlunit.cyberneko.xerces.xni.XNIException",
            "org.htmlunit.xpath.xml.utils.PrefixResolver", "org.htmlunit.WebClientOptions" };
    
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
        return PROPERTY_NAMES;
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
