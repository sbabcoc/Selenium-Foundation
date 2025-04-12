package com.nordstrom.automation.selenium.servlet;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nordstrom.automation.selenium.AbstractSeleniumConfig.SeleniumSettings;

/**
 * This class implements a simple HTTP servlet that provides an example page for the <b>Selenium Foundation</b> unit
 * tests. By default, this servlet is installed on the hub server of the local <b>Selenium Grid</b> instance. This
 * behavior can be overridden via the {@link SeleniumSettings#GRID_EXAMPLES GRID_EXAMPLES} setting.
 */
@WebServlet(name = "ExamplePageServlet", urlPatterns = {"/grid/admin/ExamplePageServlet"})
public class ExamplePageServlet extends HttpServlet {

    private static final long serialVersionUID = -2195313096162880627L;

    /** page source for this servlet */
    protected String pageSource;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void init() throws ServletException {
        pageSource = getResource("ExamplePage.html");
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * Reads all remaining bytes from the specified input stream.
     * 
     * @param inputStream input stream to read
     * @return array of remaining bytes
     * @throws IOException if an I/O error occurs
     */
    public static byte[] readAllBytes(final InputStream inputStream) throws IOException {
        return inputStream.readAllBytes();
    }

    /**
     * This class implements the HTTP servlet for example frame 'A'.
     */
    @WebServlet(name = "FrameA_Servlet", urlPatterns = {"/grid/admin/FrameA_Servlet"})
    public static class FrameA_Servlet extends ExamplePageServlet {

        private static final long serialVersionUID = 4547909165192240389L;

        /**
         * {@inheritDoc}
         */
        @Override
        public void init() throws ServletException {
            pageSource = getResource("frame_a.html");
        }
    }

    /**
     * This class implements the HTTP servlet for example frame 'B'.
     */
    @WebServlet(name = "FrameB_Servlet", urlPatterns = {"/grid/admin/FrameB_Servlet"})
    public static class FrameB_Servlet extends ExamplePageServlet {

        private static final long serialVersionUID = 5903212244921125263L;

        /**
         * {@inheritDoc}
         */
        @Override
        public void init() throws ServletException {
            pageSource = getResource("frame_b.html");
        }
    }

    /**
     * This class implements the HTTP servlet for example frame 'C'.
     */
    @WebServlet(name = "FrameC_Servlet", urlPatterns = {"/grid/admin/FrameC_Servlet"})
    public static class FrameC_Servlet extends ExamplePageServlet {

        private static final long serialVersionUID = 1448462233121165298L;

        /**
         * {@inheritDoc}
         */
        @Override
        public void init() throws ServletException {
            pageSource = getResource("frame_c.html");
        }
    }

    /**
     * This class implements the HTTP servlet for example frame 'D'.
     */
    @WebServlet(name = "FrameD_Servlet", urlPatterns = {"/grid/admin/FrameD_Servlet"})
    public static class FrameD_Servlet extends ExamplePageServlet {

        private static final long serialVersionUID = 1444648483821114876L;

        /**
         * {@inheritDoc}
         */
        @Override
        public void init() throws ServletException {
            pageSource = getResource("frame_d.html");
        }
    }
}
