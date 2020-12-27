package com.nordstrom.automation.selenium.core;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.openqa.grid.common.GridRole;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.net.UrlChecker;
import org.openqa.selenium.os.CommandLine;
import org.openqa.selenium.remote.service.DriverService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.nordstrom.automation.selenium.servlet.ExamplePageServlet;
import com.nordstrom.automation.selenium.servlet.ExamplePageServlet.FrameA_Servlet;
import com.nordstrom.automation.selenium.servlet.ExamplePageServlet.FrameB_Servlet;
import com.nordstrom.automation.selenium.servlet.ExamplePageServlet.FrameC_Servlet;
import com.nordstrom.automation.selenium.servlet.ExamplePageServlet.FrameD_Servlet;
import com.nordstrom.common.base.UncheckedThrow;
import com.nordstrom.common.jar.JarUtils;

public class LocalGridDriverService extends DriverService {
    
    private String host;

    protected LocalGridDriverService(File executable, int port, ImmutableList<String> args,
            ImmutableMap<String, String> environment) throws IOException {
        super(executable, port, args, environment);
        
        int index = args.indexOf("-host");
        host = args.get(index + 1);
        
        try {
            Field url = DriverService.class.getDeclaredField("url");
            url.setAccessible(true);
            
            int mod = url.getModifiers();
            if (Modifier.isFinal(mod)) {
                Field modifiers = Field.class.getDeclaredField("modifiers");
                modifiers.setAccessible(true);
                modifiers.setInt(url, mod & ~Modifier.FINAL);
            }
            
            url.set(this, new URL(String.format("http://%s:%d", host, port)));
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            // nothing to do here
        }
    }
    
    @Override
    protected void waitUntilAvailable() throws MalformedURLException {
        try {
            URL status = new URL(getUrl().toString() + "/status");
            SeleniumGrid.waitUntilAvailable(20, SECONDS, status);
        } catch (UrlChecker.TimeoutException e) {
            getProcess().checkForError();
            throw new WebDriverException("Timed out waiting for driver server to start.", e);
        }
    }
    
    private CommandLine getProcess() {
        try {
            Field process = DriverService.class.getDeclaredField("process");
            process.setAccessible(true);
            return (CommandLine) process.get(this);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw UncheckedThrow.throwUnchecked(e);
        }
    }

    private static String requireNotBlank(String input, String message) {
        if (StringUtils.isNotBlank(input)) {
            return input;
        }
        throw new IllegalArgumentException(message);
    }
    
    public static class Builder extends DriverService.Builder<LocalGridDriverService, LocalGridDriverService.Builder> {
        
        private static final String OPT_ROLE = "-role";
        private static final String OPT_HOST = "-host";
        private static final String OPT_PORT = "-port";
        private static final String OPT_SERVLETS = "-servlets";
        
        private static final String servlets = ExamplePageServlet.class.getName() + ',' +
                FrameA_Servlet.class.getName() + ',' + FrameB_Servlet.class.getName() + ',' +
                FrameC_Servlet.class.getName() + ',' + FrameD_Servlet.class.getName();
        
        private String launcherClassName;
        private String[] dependencyContexts;
        private GridRole gridRole;
        private Path configPath;
        private String[] propertyNames;
        private String host;
        
        public Builder(final String launcherClassName, String[] dependencyContexts, GridRole gridRole, Path configPath, String... propertyNames) {
            this.launcherClassName = requireNotBlank(launcherClassName, "[launcherClassName] must be non-null");
            
            if (dependencyContexts != null) {
                this.dependencyContexts = dependencyContexts;
            }
            
            this.gridRole = Objects.requireNonNull(gridRole, "[gridRole] must be non-null");
            this.configPath = Objects.requireNonNull(configPath, "[configPath] must be non-null");
            this.propertyNames = propertyNames;
            this.host = GridUtility.getLocalHost();
        }
        
        @Override
        protected File findDefaultExecutable() {
            return new File(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
        }

        @Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        protected ImmutableList createArgs() {
            String role = gridRole.toString().toLowerCase();
            List<String> argsList = new ArrayList<>();
            
            // specify server role
            argsList.add(OPT_ROLE);
            argsList.add(role);
            
            // if starting a Grid hub
            if (gridRole == GridRole.HUB) {
                argsList.add(OPT_SERVLETS);
                argsList.add(servlets);
            }
            
            // specify server host
            argsList.add(OPT_HOST);
            argsList.add(host);
            
            // specify server port
            argsList.add(OPT_PORT);
            argsList.add(Integer.toString(getPort()));
            
            // specify server configuration file
            argsList.add("-" + role + "Config");
            argsList.add(configPath.toString());
            
            // specify Grid launcher class name
            argsList.add(0, launcherClassName);
            
            // propagate Java System properties
            for (String name : propertyNames) {
                String value = System.getProperty(name);
                if (value != null) {
                    argsList.add(0, "-D" + name + "=" + value);
                }
            }
            
            // get assembled classpath string
            String classPath = JarUtils.getClasspath(dependencyContexts);
            // split on Java agent list separator
            String[] pathBits = classPath.split("\n");
            // if agent(s) specified
            if (pathBits.length > 1) {
                // extract classpath
                classPath = pathBits[0];
                // for each specified agent...
                for (String agentPath : pathBits[1].split("\t")) {
                    // ... specify a 'javaagent' argument
                    argsList.add(0, "-javaagent:" + agentPath);
                }
            }
            
            // specify Java class path
            argsList.add(0, classPath);
            argsList.add(0, "-cp");
            
            return ImmutableList.copyOf(argsList);
        }

        public int score(Capabilities capabilites) {
            return 0;
        }

        @Override
        protected LocalGridDriverService createDriverService(File exe, int port, ImmutableList<String> args,
                ImmutableMap<String, String> environment) {
            try {
                return new LocalGridDriverService(exe, port, args, environment);
            } catch (IOException e) {
                throw new WebDriverException(e);
            }
        }
        
        public GridRole getRole() {
            return gridRole;
        }
        
        public String getHost() {
            return host;
        }
        
        public int getPort() {
            return super.getPort();
        }
        
    }
    
}
