package com.nordstrom.automation.selenium;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Field;
import java.net.URI;

import org.openqa.grid.internal.cli.GridHubCliOptions;
import org.openqa.grid.internal.cli.GridNodeCliOptions;
import org.openqa.grid.internal.utils.configuration.GridHubConfiguration;
import org.openqa.grid.internal.utils.configuration.GridNodeConfiguration;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.SearchContext;
import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.AbstractSeleniumConfig.SeleniumSettings;
import com.nordstrom.automation.selenium.AbstractSeleniumConfig.WaitType;
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
    
//    @Test
//    public void testNodeConfig() {
//        SeleniumConfig config = SeleniumConfig.getConfig();
//        GridNodeConfiguration nodeConfig = config.getNodeConfig();
//        assertNotNull(nodeConfig);
//        assertEquals(nodeConfig.role, "node");
//        
//        boolean hasHtmlUnit = false;
//        for (MutableCapabilities capability : nodeConfig.capabilities) {
//            if ("htmlunit".equals(capability.getBrowserName())) {
//                hasHtmlUnit = true;
//                break;
//            }
//        }
//        assertTrue(hasHtmlUnit);
//    }
    
//    @Test
//    public void testNodeArgs() throws NoSuchFieldException, IllegalAccessException {
//        SeleniumConfig config = SeleniumConfig.getConfig();
//        String[] nodeArgs = config.getNodeArgs();
//        GridNodeCliOptions cliOptions = new GridNodeCliOptions().parse(nodeArgs);
//        GridNodeConfiguration nodeConfig = cliOptions.toConfiguration();
//        assertEquals(nodeConfig.role, "node");
//        
//        Field configFile = GridNodeCliOptions.class.getDeclaredField("configFile");
//        configFile.setAccessible(true);
//        String path = (String) configFile.get(cliOptions);
//        assertTrue(path.endsWith("nodeConfig-s3.json"));
//    }
    
    @Test
    public void testHubConfig() {
        SeleniumConfig config = SeleniumConfig.getConfig();
        GridHubConfiguration hubConfig = config.getHubConfig();
        assertNotNull(hubConfig);
        assertEquals(hubConfig.role, "hub");
    }
    
//    @Test
//    public void testHubArgs() throws NoSuchFieldException, IllegalAccessException {
//        SeleniumConfig config = SeleniumConfig.getConfig();
//        String[] hubArgs = config.getHubArgs();
//        GridHubCliOptions cliOptions = new GridHubCliOptions().parse(hubArgs);
//        GridHubConfiguration hubConfig = cliOptions.toConfiguration();
//        assertEquals(hubConfig.role, "hub");
//        
//        Field configFile = GridHubCliOptions.class.getDeclaredField("configFile");
//        configFile.setAccessible(true);
//        String path = (String) configFile.get(cliOptions);
//        assertTrue(path.endsWith("hubConfig.json"));
//    }
    
//    @Test
//    public void testBrowserCaps() {
//        SeleniumConfig config = SeleniumConfig.getConfig();
//        Capabilities browserCaps = config.getCurrentCapabilities();
//        assertNotNull(browserCaps.getBrowserName());
//    }
}
