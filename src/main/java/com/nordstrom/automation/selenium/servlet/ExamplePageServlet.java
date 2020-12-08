package com.nordstrom.automation.selenium.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class ExamplePageServlet extends HttpServlet {

    private static final long serialVersionUID = -2195313096162880627L;

    protected String pageSource;

    @Override
    public void init() throws ServletException {
        pageSource = getResource("ExamplePage.html");
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Set response content type
        response.setContentType("text/html");

        // Actual logic goes here.
        PrintWriter out = response.getWriter();
        out.print(pageSource);
    }

    /**
     * Get the content of the name resource
     * 
     * @param resource resource filename
     * @return resource file content
     */
    public static String getResource(final String resource) {
        URL url = Resources.getResource(resource);
        try {
            return Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to load JavaScript resource '" + resource + "'", e);
        }
    }

    public static class FrameA_Servlet extends ExamplePageServlet {

        private static final long serialVersionUID = 4547909165192240389L;

        @Override
        public void init() throws ServletException {
            pageSource = getResource("frame_a.html");
        }
    }

    public static class FrameB_Servlet extends ExamplePageServlet {

        private static final long serialVersionUID = 5903212244921125263L;

        @Override
        public void init() throws ServletException {
            pageSource = getResource("frame_b.html");
        }
    }

    public static class FrameC_Servlet extends ExamplePageServlet {

        private static final long serialVersionUID = 1448462233121165298L;

        @Override
        public void init() throws ServletException {
            pageSource = getResource("frame_c.html");
        }
    }

    public static class FrameD_Servlet extends ExamplePageServlet {

        private static final long serialVersionUID = 1444648483821114876L;

        @Override
        public void init() throws ServletException {
            pageSource = getResource("frame_d.html");
        }
    }
}
