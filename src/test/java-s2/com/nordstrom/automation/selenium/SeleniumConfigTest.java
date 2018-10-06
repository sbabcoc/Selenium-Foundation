package com.nordstrom.automation.selenium;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.net.URI;

import org.openqa.grid.common.CommandLineOptionHelper;
import org.openqa.grid.common.GridRole;
import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.internal.utils.GridHubConfiguration;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.SeleniumConfig.SeleniumSettings;
import com.nordstrom.automation.selenium.SeleniumConfig.WaitType;
import com.nordstrom.automation.selenium.support.SearchContextWait;

public class SeleniumConfigTest {
    
    @Test
    public void testBasicBehavior() {
        SeleniumConfig config = SeleniumConfig.getConfig();
        assertNotNull(config);
    }
    
    @Test
    public void testSaveToTestAttribute() {
        SeleniumConfig config1 = SeleniumConfig.getConfig();
        SeleniumConfig config2 = SeleniumConfig.getConfig();
        assertTrue(config2 == config1);
    }
    
    @Test
    public void testWaitInterval() {
        SeleniumConfig config = SeleniumConfig.getConfig();
        long timeout1 = config.getLong(SeleniumSettings.PAGE_LOAD_TIMEOUT.key());
        long timeout2 = WaitType.PAGE_LOAD.getInterval(config);
        assertNotEquals(timeout1, 0L);
        assertEquals(timeout2, timeout1);
    }
    
    @Test
    public void testGetWait() {
        SearchContextWait wait = WaitType.PAGE_LOAD.getWait(mock(SearchContext.class));
        assertNotNull(wait);
    }
    
    @Test
    public void testTargetUri() {
        SeleniumConfig config = SeleniumConfig.getConfig();
        URI targetUri = config.getTargetUri();
        assertNotNull(targetUri);
        assertEquals(targetUri.getScheme(), config.getString(SeleniumSettings.TARGET_SCHEME.key()));
        assertEquals(targetUri.getHost(), config.getString(SeleniumSettings.TARGET_HOST.key()));
        assertEquals(targetUri.getPath(), config.getString(SeleniumSettings.TARGET_PATH.key()));
    }
    
    @Test
    public void testNodeConfig() {
        SeleniumConfig config = SeleniumConfig.getConfig();
        RegistrationRequest nodeConfig = config.getNodeConfig();
        assertNotNull(nodeConfig);
        assertEquals(nodeConfig.getConfiguration().get("role"), "node");
        assertEquals(nodeConfig.getRole(), GridRole.NODE);
        
        boolean hasHtmlUnit = false;
        for (DesiredCapabilities capability : nodeConfig.getCapabilities()) {
            if ("htmlunit".equals(capability.getBrowserName())) {
                hasHtmlUnit = true;
                break;
            }
        }
        assertTrue(hasHtmlUnit);
    }
    
    @Test
    public void testNodeArgs() {
        SeleniumConfig config = SeleniumConfig.getConfig();
        String[] nodeArgs = config.getNodeArgs();
        CommandLineOptionHelper helper = new CommandLineOptionHelper(nodeArgs);
        assertEquals(helper.getParamValue("-role"), "node");
        String path = helper.getParamValue("-nodeConfig");
        assertTrue(path.endsWith("nodeConfig-s2.json"));
    }
    
    @Test
    public void testHubConfig() {
        SeleniumConfig config = SeleniumConfig.getConfig();
        GridHubConfiguration hubConfig = config.getHubConfig();
        assertNotNull(hubConfig);
        assertEquals(hubConfig.getAllParams().get("role"), "hub");
    }
    
    @Test
    public void testHubArgs() {
        SeleniumConfig config = SeleniumConfig.getConfig();
        String[] hubArgs = config.getHubArgs();
        CommandLineOptionHelper helper = new CommandLineOptionHelper(hubArgs);
        assertEquals(helper.getParamValue("-role"), "hub");
        String path = helper.getParamValue("-hubConfig");
        assertTrue(path.endsWith("hubConfig.json"));
    }
    
    @Test
    public void testBrowserCaps() {
        SeleniumConfig config = SeleniumConfig.getConfig();
        Capabilities browserCaps = config.getBrowserCaps();
        assertNotNull(browserCaps.getBrowserName());
    }
}
