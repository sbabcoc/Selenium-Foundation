package com.nordstrom.automation.selenium.utility;

import java.net.URI;
import java.util.Optional;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import com.nordstrom.automation.selenium.core.WebDriverUtils;

public final class PageSourceUtils {
    
    private static final String TAKES_ELEMENT_SCREENSHOT = "takesElementScreenshot";
    
    private PageSourceUtils() {
        throw new AssertionError("PageSourceUtils is a static utility class that cannot be instantiated");
    }
    
    /**
     * Determine if the specified driver is capable of producing page source.
     * 
     * @param optDriver optional web driver object
     * @param logger SLF4J logger object
     * @return 'true' if driver can produce page source; otherwise 'false
     */
    public static boolean canGetArtifact(Optional<WebDriver> optDriver, Logger logger) {
        if (optDriver.isPresent()) {
            WebDriver driver = optDriver.get();
            if (driver instanceof HasCapabilities) {
                Capabilities caps = ((HasCapabilities) driver).getCapabilities();
                // if driver explicitly reports that it cannot produce page source
                if (Boolean.FALSE.equals(caps.getCapability(TAKES_ELEMENT_SCREENSHOT))) {
                    logger.warn("This driver is not capable of producing page source.");
                } else {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Produce page source from the specified driver.
     * 
     * @param optDriver optional web driver object
     * @param reason impetus for capture request; may be 'null'
     * @param logger SLF4J logger object
     * @return page source; if capture fails, an empty string is returned
     */
    public static String getArtifact(Optional<WebDriver> optDriver, Throwable reason, Logger logger) {
        if (canGetArtifact(optDriver, logger)) {
            try {
                WebDriver driver = optDriver.get();
                StringBuilder sourceBuilder = new StringBuilder(driver.getPageSource());
                insertBaseElement(sourceBuilder, driver);
                insertBreakpointInfo(sourceBuilder, reason);
                insertOriginalUrl(sourceBuilder, driver);
                return sourceBuilder.toString();
            } catch (WebDriverException e) {
                logger.warn("The driver is capable of producing page source, but failed.", e);
            }
        }
        return "";
    }
    
    /**
     * Insert "base" node so that style sheets, images, and other relative resources load properly.
     * 
     * @param sourceBuilder {@link StringBuilder} object used to build page source
     * @param driver web driver object
     * @return the [sourceBuilder] object
     */
    private static StringBuilder insertBaseElement(StringBuilder sourceBuilder, WebDriver driver) {
        int offset = sourceBuilder.indexOf("<head>") + 6;
        
        // if no head found
        if (offset < 6) {
            return sourceBuilder;
        }
        
        int closing = sourceBuilder.indexOf("</head>", offset);
        String substr = sourceBuilder.substring(offset, closing);
        
        // if base already exists
        if (substr.contains("<base ")) {
            return sourceBuilder;
        }
        
        URI uri = URI.create(driver.getCurrentUrl());
        
        String path = uri.getPath();
        int endIndex = path.lastIndexOf('/') + 1;
        String root = path.substring(0, endIndex);
        
        String authority = uri.getAuthority();
        if (authority != null) {
            root = authority + root;
        }
        
        sourceBuilder.insert(offset, "\">\n")
                .insert(offset, root)
                .insert(offset, "://")
                .insert(offset, uri.getScheme())
                .insert(offset, "<base href=\"")
                .insert(offset, "\n<!-- Inserted by Selenium Foundation -->\n");
        
        return sourceBuilder;
    }
    
    /**
     * Insert exception breakpoint information into page source.
     * 
     * @param sourceBuilder {@link StringBuilder} object used to build page source
     * @param reason impetus for capture request; may be 'null'
     * @return the [sourceBuilder] object
     */
    private static StringBuilder insertBreakpointInfo(StringBuilder sourceBuilder, Throwable reason) {
        if (reason != null) {
            Throwable cause = WebDriverUtils.getReportableCause(reason);
            StackTraceElement breakpoint = WebDriverUtils.getClientBreakpoint(cause);
            
            // if breakpoint was identified
            if (breakpoint != null) {
                // insert breakpoint message as comments
                sourceBuilder.insert(0, ") -->\n")
                        .insert(0, breakpoint.getLineNumber())
                        .insert(0, ":")
                        .insert(0, breakpoint.getClassName())
                        .insert(0, "<!-- at ");
            }

            String message = cause.getMessage();
            if (message == null) {
                message = "(no message)";
            }
            
            // insert exception message as comment
            sourceBuilder.insert(0, " -->\n")
                    .insert(0, message)
                    .insert(0, ": ")
                    .insert(0, cause.getClass().getSimpleName())
                    .insert(0, "<!-- ");
        }
        
        return sourceBuilder;
    }
    
    /**
     * Insert original URL information into page source.
     * 
     * @param sourceBuilder {@link StringBuilder} object used to build page source
     * @param driver web driver object
     * @return the [sourceBuilder] object
     */
    private static StringBuilder insertOriginalUrl(StringBuilder sourceBuilder, WebDriver driver) {
        sourceBuilder.insert(0, " -->\n")
                .insert(0, driver.getCurrentUrl())
                .insert(0, "<!-- Original URL: ");
        
        return sourceBuilder;
    }
}
