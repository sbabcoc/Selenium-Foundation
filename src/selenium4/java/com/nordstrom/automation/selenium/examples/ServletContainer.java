package com.nordstrom.automation.selenium.examples;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * This class implements a servlet container for hosting local pages. 
 */
public class ServletContainer {
    
    /**
     * Start a servlet container on the specified port with the specified servlets.
     * This method returns the started server without blocking, allowing the caller
     * to manage the server lifecycle.
     *
     * @param port port on which the server should listen
     * @param servletClasses fully-qualified names of servlet classes to register
     * @return started {@link Server} instance
     * @throws Exception if the server fails to start
     * @since 36.0.0
     */
    public static Server start(int port, List<String> servletClasses) throws Exception {
        ServletContextHandler context = new ServletContextHandler();
        for (String servletClassName : servletClasses) {
            addServlet(context, servletClassName);
        }
        Server server = new Server(port);
        server.setHandler(context);
        server.start();
        return server;
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
