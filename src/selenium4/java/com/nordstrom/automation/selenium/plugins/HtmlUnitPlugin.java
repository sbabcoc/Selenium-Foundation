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
    
    /** web client version system property */
    public static final String WEB_CLIENT_VERSION = "webdriver.htmlunit.webClientVersion";
    /** "JavaScript enabled" system property */
    public static final String JAVASCRIPT_ENABLED = "webdriver.htmlunit.javaScriptEnabled";
    /** "CSS enabled" system property */
    public static final String CSS_ENABLED = "webdriver.htmlunit.cssEnabled";
    /** "print content on failing status code" system property */
    public static final String PRINT_CONTENT_ON_FAILING_STATUS_CODE = "webdriver.htmlunit.printContentOnFailingStatusCode";
    /** "throw exception on failing status code" system property */
    public static final String THROW_EXCEPTION_ON_FAILING_STATUS_CODE = "webdriver.htmlunit.throwExceptionOnFailingStatusCode";
    /** "throw exception on script error" system property */
    public static final String THROW_EXCEPTION_ON_SCRIPT_ERROR = "webdriver.htmlunit.throwExceptionOnScriptError";
    /** "popup blocker enabled" system property */
    public static final String POPUP_BLOCKER_ENABLED = "webdriver.htmlunit.popupBlockerEnabled";
    /** "is redirect enabled" system property */
    public static final String IS_REDIRECT_ENABLED = "webdriver.htmlunit.isRedirectEnabled";
    /** temp file directory system property */
    public static final String TEMP_FILE_DIRECTORY = "webdriver.htmlunit.tempFileDirectory";
    /** SSL client certificate store system property */
    public static final String SSL_CLIENT_CERTIFICATE_STORE = "webdriver.htmlunit.sslClientCertificateStore";
    /** SSL client certificate type system property */
    public static final String SSL_CLIENT_CERTIFICATE_TYPE = "webdriver.htmlunit.sslClientCertificateType";
    /** SSL client certificate password system property */
    public static final String SSL_CLIENT_CERTIFICATE_PASSWORD = "webdriver.htmlunit.sslClientCertificatePassword";
    /** SSL trust store system property */
    public static final String SSL_TRUST_STORE = "webdriver.htmlunit.sslTrustStore";
    /** SSL trust store type system property */
    public static final String SSL_TRUST_STORE_TYPE = "webdriver.htmlunit.sslTrustStoreType";
    /** SSL trust store password system property */
    public static final String SSL_TRUST_STORE_PASSWORD = "webdriver.htmlunit.sslTrustStorePassword";
    /** SSL client protocols system property */
    public static final String SSL_CLIENT_PROTOCOLS = "webdriver.htmlunit.sslClientProtocols";
    /** SSL client cipher suites system property */
    public static final String SSL_CLIENT_CIPHER_SUITES = "webdriver.htmlunit.sslClientCipherSuites";
    /** "geo-location enabled" system property */
    public static final String GEO_LOCATION_ENABLED = "webdriver.htmlunit.geolocationEnabled";
    /** "'do not track' enabled" system property */
    public static final String DO_NOT_TRACK_ENABLED = "webdriver.htmlunit.doNotTrackEnabled";
    /** home page system property */
    public static final String HOME_PAGE = "webdriver.htmlunit.homePage";
    /** proxy config system property */
    public static final String PROXY_CONFIG = "webdriver.htmlunit.proxyConfig";
    /** timeout system property */
    public static final String TIMEOUT = "webdriver.htmlunit.timeout";
    /** connection time-to-live system property */
    public static final String CONNECTION_TIME_TO_LIVE = "webdriver.htmlunit.connectionTimeToLive";
    /** "use insecure SSL" system property */
    public static final String USE_INSECURE_SSL = "webdriver.htmlunit.useInsecureSSL";
    /** SSL insecure protocol system property */
    public static final String SSL_INSECURE_PROTOCOL = "webdriver.htmlunit.sslInsecureProtocol";
    /** max in memory system property */
    public static final String MAX_IN_MEMORY = "webdriver.htmlunit.maxInMemory";
    /** history size limit system property */
    public static final String HITORY_SIZE_LIMIT = "webdriver.htmlunit.historySizeLimit";
    /** history page cache limit system property */
    public static final String HISTORY_PAGE_CACHE_LIMIT = "webdriver.htmlunit.historyPageCacheLimit";
    /** local address system property */
    public static final String LOCAL_ADDRESS = "webdriver.htmlunit.localAddress";
    /** download images system property */
    public static final String DOWNLOAD_IMAGES = "webdriver.htmlunit.downloadImages";
    /** screen width system property */
    public static final String SCREEN_WIDTH = "webdriver.htmlunit.screenWidth";
    /** screen height system property */
    public static final String SCREEN_HEIGHT = "webdriver.htmlunit.screenHeight";
    /** "web socket enabled" system property */
    public static final String WEB_SOCKET_ENABLED = "webdriver.htmlunit.webSocketEnabled";
    /** web socket max text message size system property */
    public static final String WEB_SOCKET_MAX_TEXT_MESSAGE_SIZE = "webdriver.htmlunit.webSocketMaxTextMessageSize";
    /** web socket max text message buffer size system property */
    public static final String WEB_SOCKET_MAX_TEXT_MESSAGE_BUFFER_SIZE = "webdriver.htmlunit.webSocketMaxTextMessageBufferSize";
    /** web socket max binary message size system property */
    public static final String WEB_SOCKET_MAX_BINARY_MESSAGE_SIZE = "webdriver.htmlunit.webSocketMaxBinaryMessageSize";
    /** web socket max binary message buffer size system property */
    public static final String WEB_SOCKET_MAX_BINARY_MESSAGE_BUFFER_SIZE = "webdriver.htmlunit.webSocketMaxBinaryMessageBufferSize";
    /** "fetch polyfill enabled" system property */
    public static final String FETCH_POLYFILL_ENABLED = "webdriver.htmlunit.fetchPolyfillEnabled";
    /** numeric code system property */
    public static final String NUMERIC_CODE = "webdriver.htmlunit.numericCode";
    /** nickname system property */
    public static final String NICKNAME = "webdriver.htmlunit.nickname";
    /** application version system property */
    public static final String APPLICATION_VERSION = "webdriver.htmlunit.applicationVersion";
    /** user agent system property */
    public static final String USER_AGENT = "webdriver.htmlunit.userAgent";
    /** application name system property */
    public static final String APPLICATION_NAME = "webdriver.htmlunit.applicationName";
    /** application code name system property */
    public static final String APPLICATION_CODE_NAME = "webdriver.htmlunit.applicationCodeName";
    /** application minor version system property */
    public static final String APPLICATION_MINOR_VERSION = "webdriver.htmlunit.applicationMinorVersion";
    /** vendor system property */
    public static final String VENDOR = "webdriver.htmlunit.vendor";
    /** browser language system property */
    public static final String BROWSER_LANGUAGE = "webdriver.htmlunit.browserLanguage";
    /** "is online" system property */
    public static final String IS_ONLINE = "webdriver.htmlunit.isOnline";
    /** platform system property */
    public static final String PLATFORM = "webdriver.htmlunit.platform";
    /** system time zone system property */
    public static final String SYSTEM_TIME_ZONE = "webdriver.htmlunit.systemTimezone";
    /** <b>Accept-Encoding</b> header system property */
    public static final String ACCEPT_ENCODING_HEADER = "webdriver.htmlunit.acceptEncodingHeader";
    /** <b>Accept-Language</b> header system property */
    public static final String ACCEPT_LANGUAGE = "webdriver.htmlunit.acceptLanguageHeader";
    /** <b>Accept</b> header system property (for page requests) */
    public static final String HTML_ACCEPT_HEADER = "webdriver.htmlunit.htmlAcceptHeader";
    /** <b>Accept</b> header system property (for image requests) */
    public static final String IMG_ACCEPT_HEADER = "webdriver.htmlunit.imgAcceptHeader";
    /** <b>Accept</b> header system property (for CSS requests) */
    public static final String CSS_ACCEPT_HEADER = "webdriver.htmlunit.cssAcceptHeader";
    /** <b>Accept</b> header system property (for script requests) */
    public static final String SCRIPT_ACCEPT_HEADER = "webdriver.htmlunit.scriptAcceptHeader";
    /** <b>Accept</b> header system property (for <b>XMLHttpRequest</b>) */
    public static final String XML_HTTP_REQUEST_ACCEPT_HEADER = "webdriver.htmlunit.xmlHttpRequestAcceptHeader";
    /** <b>Sec-CH-UA</b> header system property */
    public static final String SEC_CLIENT_HINT_USER_AGENT_HEADER = "webdriver.htmlunit.secClientHintUserAgentHeader";
    /** <b>Sec-CH-UA-Platform</b> header system property */
    public static final String SEC_CLIENT_HINT_USER_AGENT_PLATFORM_HEADER = "webdriver.htmlunit.secClientHintUserAgentPlatformHeader";
    
    private static final String[] PROPERTY_NAMES = {
            WEB_CLIENT_VERSION, JAVASCRIPT_ENABLED, CSS_ENABLED, PRINT_CONTENT_ON_FAILING_STATUS_CODE,
            THROW_EXCEPTION_ON_FAILING_STATUS_CODE, THROW_EXCEPTION_ON_SCRIPT_ERROR, POPUP_BLOCKER_ENABLED,
            IS_REDIRECT_ENABLED, TEMP_FILE_DIRECTORY, SSL_CLIENT_CERTIFICATE_STORE, SSL_CLIENT_CERTIFICATE_TYPE,
            SSL_CLIENT_CERTIFICATE_PASSWORD, SSL_TRUST_STORE, SSL_TRUST_STORE_TYPE, SSL_TRUST_STORE_PASSWORD,
            SSL_CLIENT_PROTOCOLS, SSL_CLIENT_CIPHER_SUITES, GEO_LOCATION_ENABLED, DO_NOT_TRACK_ENABLED, HOME_PAGE,
            PROXY_CONFIG, TIMEOUT, CONNECTION_TIME_TO_LIVE, USE_INSECURE_SSL, SSL_INSECURE_PROTOCOL, MAX_IN_MEMORY,
            HITORY_SIZE_LIMIT, HISTORY_PAGE_CACHE_LIMIT, LOCAL_ADDRESS, DOWNLOAD_IMAGES, SCREEN_WIDTH, SCREEN_HEIGHT,
            WEB_SOCKET_ENABLED, WEB_SOCKET_MAX_TEXT_MESSAGE_SIZE, WEB_SOCKET_MAX_TEXT_MESSAGE_BUFFER_SIZE,
            WEB_SOCKET_MAX_BINARY_MESSAGE_SIZE, WEB_SOCKET_MAX_BINARY_MESSAGE_BUFFER_SIZE, FETCH_POLYFILL_ENABLED,
            NUMERIC_CODE, NICKNAME, APPLICATION_VERSION, USER_AGENT, APPLICATION_NAME, APPLICATION_CODE_NAME,
            APPLICATION_MINOR_VERSION, VENDOR, BROWSER_LANGUAGE, IS_ONLINE, PLATFORM, SYSTEM_TIME_ZONE,
            ACCEPT_ENCODING_HEADER, ACCEPT_LANGUAGE, HTML_ACCEPT_HEADER, IMG_ACCEPT_HEADER, CSS_ACCEPT_HEADER,
            SCRIPT_ACCEPT_HEADER, XML_HTTP_REQUEST_ACCEPT_HEADER, SEC_CLIENT_HINT_USER_AGENT_HEADER,
            SEC_CLIENT_HINT_USER_AGENT_PLATFORM_HEADER };
    
    /**
     * <b>org.openqa.selenium.htmlunit.HtmlUnitDriver</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *  &lt;artifactId&gt;htmlunit-driver&lt;/artifactId&gt;
     *  &lt;version&gt;4.30.0.0&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.openqa.selenium.htmlunit.remote.HtmlUnitDriverInfo</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;com.nordstrom.ui-tools&lt;/groupId&gt;
     *  &lt;artifactId&gt;htmlunit-remote&lt;/artifactId&gt;
     *  &lt;version&gt;4.30.0.0&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.openqa.selenium.By</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *  &lt;artifactId&gt;selenium-api&lt;/artifactId&gt;
     *  &lt;version&gt;4.30.0.0&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.openqa.selenium.support.FindBy</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.seleniumhq.selenium&lt;/groupId&gt;
     *  &lt;artifactId&gt;selenium-support&lt;/artifactId&gt;
     *  &lt;version&gt;4.30.0.0&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.htmlunit.Version</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.htmlunit&lt;/groupId&gt;
     *  &lt;artifactId&gt;htmlunit&lt;/artifactId&gt;
     *  &lt;version&gt;4.11.1&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.apache.commons.lang3.CharSet</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.apache.commons&lt;/groupId&gt;
     *  &lt;artifactId&gt;commons-lang3&lt;/artifactId&gt;
     *  &lt;version&gt;3.17.1&lt;/version&gt;
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
     *  &lt;groupId&gt;org.apache.httpcomponents&lt;/groupId&gt;
     *  &lt;artifactId&gt;httpcore&lt;/artifactId&gt;
     *  &lt;version&gt;4.4.16&lt;/version&gt;
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
     *  &lt;version&gt;1.11&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.apache.commons.io.IOUtils</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;commons-io&lt;/groupId&gt;
     *  &lt;artifactId&gt;commons-io&lt;/artifactId&gt;
     *  &lt;version&gt;2.18.0&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.apache.commons.logging.Log</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;commons-logging&lt;/groupId&gt;
     *  &lt;artifactId&gt;commons-logging&lt;/artifactId&gt;
     *  &lt;version&gt;1.3.4&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.htmlunit.jetty.websocket.client.WebSocketClient</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.htmlunit&lt;/groupId&gt;
     *  &lt;artifactId&gt;htmlunit-websocket-client&lt;/artifactId&gt;
     *  &lt;version&gt;4.11.0&lt;/version&gt;
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
     * <b>org.eclipse.jetty.http.Syntax</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.eclipse.jetty&lt;/groupId&gt;
     *  &lt;artifactId&gt;jetty-http&lt;/artifactId&gt;
     *  &lt;version&gt;9.4.57.v20241219&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>net.bytebuddy.matcher.ElementMatcher</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;net.bytebuddy&lt;/groupId&gt;
     *  &lt;artifactId&gt;byte-buddy&lt;/artifactId&gt;
     *  &lt;version&gt;1.17.5&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.htmlunit.corejs.javascript.Symbol</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.htmlunit&lt;/groupId&gt;
     *  &lt;artifactId&gt;htmlunit-core-js&lt;/artifactId&gt;
     *  &lt;version&gt;4.11.0&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.htmlunit.cssparser.parser.CSSErrorHandler</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.htmlunit&lt;/groupId&gt;
     *  &lt;artifactId&gt;htmlunit-cssparser&lt;/artifactId&gt;
     *  &lt;version&gt;4.11.0&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.htmlunit.cyberneko.xerces.xni.XNIException</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.htmlunit&lt;/groupId&gt;
     *  &lt;artifactId&gt;neko-htmlunit&lt;/artifactId&gt;
     *  &lt;version&gt;4.11.0&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     * 
     * <b>org.htmlunit.xpath.xml.utils.PrefixResolver</b>
     * 
     * <pre>&lt;dependency&gt;
     *  &lt;groupId&gt;org.htmlunit&lt;/groupId&gt;
     *  &lt;artifactId&gt;htmlunit-xpath&lt;/artifactId&gt;
     *  &lt;version&gt;4.11.0&lt;/version&gt;
     *&lt;/dependency&gt;</pre>
     */
    private static final String[] DEPENDENCY_CONTEXTS = { "org.openqa.selenium.htmlunit.HtmlUnitDriver",
            "org.openqa.selenium.htmlunit.remote.HtmlUnitDriverInfo", "org.openqa.selenium.By",
            "org.openqa.selenium.support.FindBy", "org.htmlunit.Version", "org.apache.commons.lang3.CharSet",
            "org.apache.commons.text.WordUtils", "org.apache.http.client.HttpClient", "org.apache.http.HttpHost",
            "org.apache.http.entity.mime.MIME", "org.apache.commons.codec.Encoder", "org.apache.commons.io.IOUtils",
            "org.apache.commons.logging.Log", "org.htmlunit.jetty.websocket.client.WebSocketClient",
            "org.eclipse.jetty.util.IO", "org.eclipse.jetty.io.EndPoint", "org.eclipse.jetty.http.Syntax",
            "net.bytebuddy.matcher.ElementMatcher", "org.htmlunit.corejs.javascript.Symbol",
            "org.htmlunit.cssparser.parser.CSSErrorHandler", "org.htmlunit.cyberneko.xerces.xni.XNIException",
            "org.htmlunit.xpath.xml.utils.PrefixResolver" };
    
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
