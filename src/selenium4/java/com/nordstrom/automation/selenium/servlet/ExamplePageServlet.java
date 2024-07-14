package com.nordstrom.automation.selenium.servlet;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nordstrom.automation.selenium.AbstractSeleniumConfig.SeleniumSettings;

/**
 * This class implements a simple HTTP servlet that provides an example page for the <b>Selenium Foundation</b> unit
 * tests. By default, this servlet is installed on the hub server of the local <b>Selenium Grid</b> instance. This
 * behavior can be overridden via the {@link SeleniumSettings#GRID_EXAMPLES GRID_EXAMPLES} setting.
 */
public class ExamplePageServlet extends HttpServlet {

    private static final long serialVersionUID = -2195313096162880627L;

    protected String pageSource;
    protected String target;
    

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
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try {
            try (InputStream is = classLoader.getResourceAsStream(resource)) {
                return (is != null) ? new String(readAllBytes(is), UTF_8) : null;
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to load servlet resource '" + resource + "'", e);
        }
    }

    public static byte[] readAllBytes(final InputStream inputStream) throws IOException {
        return inputStream.readAllBytes();
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
