package com.nordstrom.automation.selenium.examples;

import com.beust.jcommander.JCommander;
import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.servlet.ExamplePageServlet;
import com.nordstrom.automation.selenium.servlet.ExamplePageServlet.FrameA_Servlet;
import com.nordstrom.automation.selenium.servlet.ExamplePageServlet.FrameB_Servlet;
import com.nordstrom.automation.selenium.servlet.ExamplePageServlet.FrameC_Servlet;
import com.nordstrom.automation.selenium.servlet.ExamplePageServlet.FrameD_Servlet;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class ServletContainer {
    
    private static final String[] DEPENDENCY_CONTEXTS = {
        ServletContainer.class.getName(),
        "javax.servlet.Servlet",
        "org.eclipse.jetty.http.HttpField",
        "org.eclipse.jetty.io.ByteBufferPool",
        "org.eclipse.jetty.security.SecurityHandler",
        "org.eclipse.jetty.server.Server",
        "org.eclipse.jetty.servlet.ServletHolder",
        "org.eclipse.jetty.util.component.LifeCycle",
        "com.beust.jcommander.JCommander"
    };
    
    public static String[] getDependencyContexts() {
        SeleniumConfig config = SeleniumConfig.getConfig();
        Set<String> servlets = config.getGridServlets();
        Collections.addAll(servlets, DEPENDENCY_CONTEXTS);
        return servlets.toArray(new String[0]);
    }
    
    public static void main(String[] args) throws Exception {
        ServletFlags flags = new ServletFlags();
        JCommander comamnder = JCommander.newBuilder().addObject(flags).build();
        
        int port = 8080;
        
        Server server = new Server(port);

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        
        
        
        context.addServlet(new ServletHolder(new ExamplePageServlet()), "/grid/admin/ExamplePageServlet");
        context.addServlet(new ServletHolder(new FrameA_Servlet()), "/grid/admin/FrameA_Servlet");
        context.addServlet(new ServletHolder(new FrameB_Servlet()), "/grid/admin/FrameB_Servlet");
        context.addServlet(new ServletHolder(new FrameC_Servlet()), "/grid/admin/FrameC_Servlet");
        context.addServlet(new ServletHolder(new FrameD_Servlet()), "/grid/admin/FrameD_Servlet");
        
        server.setHandler(context);
        server.start();
        server.join();
    }
}
