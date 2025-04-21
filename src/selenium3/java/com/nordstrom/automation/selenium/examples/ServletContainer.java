package com.nordstrom.automation.selenium.examples;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import org.seleniumhq.jetty9.server.Server;
import org.seleniumhq.jetty9.servlet.ServletContextHandler;
import org.seleniumhq.jetty9.servlet.ServletHolder;

import com.beust.jcommander.JCommander;
import com.nordstrom.automation.selenium.SeleniumConfig;

public class ServletContainer {
    
    private static final String[] DEPENDENCY_CONTEXTS = {
        ServletContainer.class.getName(),
        "javax.servlet.Servlet",
        "org.seleniumhq.jetty9.server.Handler"
    };
    
    /**
     * Get dependency contexts for this driver.
     * 
     * @return driver dependency contexts
     */
    public static String[] getDependencyContexts() {
        SeleniumConfig config = SeleniumConfig.getConfig();
        Set<String> servlets = config.getGridServlets();
        Collections.addAll(servlets, DEPENDENCY_CONTEXTS);
        return servlets.toArray(new String[0]);
    }
    
    /**
     * Get list of <b>--servlet</b> command line arguments from specified Grid servlet classes.
     * 
     * @return list of servlet command line options
     */
    public static List<String> getServletArgs() {
        SeleniumConfig config = SeleniumConfig.getConfig();
       return config.getGridServlets().stream().flatMap(s -> Stream.of("--servlet", s)).collect(Collectors.toList());
    }
    
    /**
     * This is the "main" method of this Java command line utility.
     * 
     * @param args command line arguments
     * @throws Exception if something goes sideways
     */
    public static void main(String[] args) throws Exception {
        ServletFlags flags = new ServletFlags();
        JCommander.newBuilder().addObject(flags).build().parse(args);
        
        ServletContextHandler context = new ServletContextHandler();
        for (String servletClassName : flags.getServlets()) {
            addServlet(context, servletClassName);
        }
        
        Server server = new Server(flags.getPort());
        server.setHandler(context);
        server.start();
        server.join();
    }
    
    /**
     * Add the specified servlet to the indicated context handler.
     * 
     * @param context target {@link ServletContextHandler} 
     * @param servletClassName servlet class name
     */
    private static void addServlet(final ServletContextHandler context, final String servletClassName) {
        Class<?> clazz;
        Object instance;
        HttpServlet servlet;
        ServletHolder holder;
        WebServlet webServlet;
        String[] pathSpecs;
        
        try {
            clazz = Class.forName(servletClassName);
            instance = clazz.getDeclaredConstructor().newInstance();
            servlet = (HttpServlet) instance;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed getting class for name: " + servletClassName, e);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new RuntimeException("Failed instantiating servlet: " + servletClassName, e);
        } catch (ClassCastException e) {
            throw new RuntimeException("Class '" + servletClassName + "' does not extend 'HttpServlet'");
        }
        
        webServlet = Objects.requireNonNull(clazz.getAnnotation(WebServlet.class),
                "Failed getting 'WebServlet' annotation of servlet: " + servletClassName);
        pathSpecs = webServlet.urlPatterns();
        
        if (pathSpecs.length == 0) {
            throw new RuntimeException(
                    "No URL patterns specified in 'WebServlet' annotation of servlet: " + servletClassName);
        }
        
        holder = new ServletHolder(servlet);
        for (String pathSpec : pathSpecs) {
            context.addServlet(holder, pathSpec);
        }
    }
}
