package com.nordstrom.automation.selenium;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.net.URI;

import org.openqa.grid.internal.utils.configuration.GridHubConfiguration;
import org.openqa.grid.internal.utils.configuration.GridNodeConfiguration;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.SearchContext;
import org.testng.annotations.Test;

import com.beust.jcommander.JCommander;
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
        GridNodeConfiguration nodeConfig = config.getNodeConfig();
        assertNotNull(nodeConfig);
        assertEquals(nodeConfig.role, "node");
        
        boolean hasPhantomJS = false;
        for (MutableCapabilities capability : nodeConfig.capabilities) {
            if ("phantomjs".equals(capability.getBrowserName())) {
                hasPhantomJS = true;
                break;
            }
        }
        assertTrue(hasPhantomJS);
    }
    
    @Test
    public void testNodeArgs() {
        SeleniumConfig config = SeleniumConfig.getConfig();
        GridNodeConfiguration nodeConfig = new GridNodeConfiguration();
        String[] nodeArgs = config.getNodeArgs();
        new JCommander(nodeConfig, nodeArgs);
        assertEquals(nodeConfig.role, "node");
        String path = nodeConfig.nodeConfigFile;
        assertTrue(path.endsWith("nodeConfig.json"));
    }
    
    @Test
    public void testHubConfig() {
        SeleniumConfig config = SeleniumConfig.getConfig();
        GridHubConfiguration hubConfig = config.getHubConfig();
        assertNotNull(hubConfig);
        assertEquals(hubConfig.role, "hub");
    }
    
    @Test
    public void testHubArgs() {
        SeleniumConfig config = SeleniumConfig.getConfig();
        GridHubConfiguration hubConfig = new GridHubConfiguration();
        String[] hubArgs = config.getHubArgs();
        new JCommander(hubConfig, hubArgs);
        assertEquals(hubConfig.role, "hub");
        String path = hubConfig.hubConfig;
        assertTrue(path.endsWith("hubConfig.json"));
    }
    
    @Test
    public void testBrowserCaps() {
        SeleniumConfig config = SeleniumConfig.getConfig();
        Capabilities browserCaps = config.getBrowserCaps();
        assertNotNull(browserCaps.getBrowserName());
    }
}
