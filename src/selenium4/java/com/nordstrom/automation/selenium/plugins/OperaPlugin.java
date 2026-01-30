package com.nordstrom.automation.selenium.plugins;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.core.GridUtility;
import com.nordstrom.automation.selenium.core.LocalSeleniumGrid;
import com.nordstrom.automation.selenium.core.LocalSeleniumGrid.LocalGridServer;

/**
 * This class is the plug-in for <b>OperaDriver</b>.
 */
public class OperaPlugin extends RemoteWebDriverPlugin {
    
    private static final String[] DEPENDENCY_CONTEXTS = {};
    
    /**
     * Constructor for <b>OperaPlugin</b> objects.
     */
    public OperaPlugin() {
        super(OperaCaps.DRIVER_NAME);
    }

    @Override
    public String[] getDependencyContexts() {
        return DEPENDENCY_CONTEXTS;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getCapabilities(SeleniumConfig config) {
        return OperaCaps.getCapabilities();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getPersonalities() {
        return OperaCaps.getPersonalities();
    }

    @Override
    public String[] getPropertyNames(String capabilities) {
        return OperaCaps.getPropertyNames(capabilities);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public LocalGridServer create(SeleniumConfig config, String launcherClassName, String[] dependencyContexts,
            URL hubUrl, Integer portNum, Path workingPath, Path outputPath) throws IOException {
        
        // create relay server to proxy this new Opera node
        Path relayConfigPath = config.createRelayConfig(getCapabilities(config), hubUrl);
        Path relayOutputPath = (outputPath != null) ? GridUtility.getOutputPath(config, null) : null;
        LocalGridServer relayServer = LocalSeleniumGrid.create(config, launcherClassName,
                dependencyContexts, false, portNum, relayConfigPath, workingPath, relayOutputPath);
        
        // extract target node URL from relay configuration
        URL relayTarget = LocalGridServer.getRelayTarget(relayConfigPath);
        String targetAddr = relayTarget.getHost();
        Integer targetPort = relayTarget.getPort();
        
        List<String> argsList = new ArrayList<>();
        getPropertyNames(OperaCaps.getCapabilities());
        argsList.add(System.getProperty(OperaCaps.DRIVER_PATH));
        argsList.add("--address=" + targetAddr);
        argsList.add("--port=" + targetPort.toString());
        
        ProcessBuilder builder = new ProcessBuilder(argsList);
        
        return new LocalGridServer(targetAddr, targetPort, false, builder, workingPath, outputPath)
                .setRelayServer(relayServer);
    }
}
