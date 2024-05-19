package com.nordstrom.automation.selenium.examples;

import org.seleniumhq.jetty9.server.Server;
import org.seleniumhq.jetty9.servlet.ServletContextHandler;
import org.seleniumhq.jetty9.servlet.ServletHolder;

import com.nordstrom.automation.selenium.servlet.ExamplePageServlet;
import com.nordstrom.automation.selenium.servlet.ExamplePageServlet.FrameA_Servlet;
import com.nordstrom.automation.selenium.servlet.ExamplePageServlet.FrameB_Servlet;
import com.nordstrom.automation.selenium.servlet.ExamplePageServlet.FrameC_Servlet;
import com.nordstrom.automation.selenium.servlet.ExamplePageServlet.FrameD_Servlet;

public class ServletContainer {
    
    private static final String[] DEPENDENCY_CONTEXTS = {
        ServletContainer.class.getName(),
        "javax.servlet.Servlet",
        "org.seleniumhq.jetty9.server.Handler"
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
