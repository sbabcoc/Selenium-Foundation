package com.nordstrom.automation.selenium;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static com.nordstrom.automation.selenium.platform.TargetTypeName.SUPPORT_NAME;

import java.net.URI;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.SearchContext;
import org.testng.SkipException;
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
    
    @Test
    public void testBrowserCaps() {
        SeleniumConfig config = SeleniumConfig.getConfig();
        String contextPlatform = config.getContextPlatform();
        if ((contextPlatform == null) || contextPlatform.equals(SUPPORT_NAME)) {
            throw new SkipException("Current target platform doesn't provide browser capabilities");
        }
        
        Capabilities browserCaps = config.getCurrentCapabilities();
        assertNotNull(browserCaps.getBrowserName());
    }
}
