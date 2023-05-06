package com.nordstrom.automation.selenium.examples;

import com.nordstrom.automation.selenium.servlet.ExamplePageServlet;
import com.nordstrom.automation.selenium.servlet.ExamplePageServlet.FrameA_Servlet;
import com.nordstrom.automation.selenium.servlet.ExamplePageServlet.FrameB_Servlet;
import com.nordstrom.automation.selenium.servlet.ExamplePageServlet.FrameC_Servlet;
import com.nordstrom.automation.selenium.servlet.ExamplePageServlet.FrameD_Servlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class ServletContainer {
    
    private static final String[] DEPENDENCY_CONTEXTS = {
        ServletContainer.class.getName(),
        "javax.servlet.Servlet",
        "com.google.common.io.Resources",
        "org.eclipse.jetty.http.HttpField",
        "org.eclipse.jetty.io.ByteBufferPool",
        "org.eclipse.jetty.security.SecurityHandler",
        "org.eclipse.jetty.server.Server",
        "org.eclipse.jetty.servlet.ServletHolder",
        "org.eclipse.jetty.util.component.LifeCycle"
    };
    
    public static String[] getDependencyContexts() {
        return DEPENDENCY_CONTEXTS;
    }
    
    public static void main(String[] args) throws Exception {
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
