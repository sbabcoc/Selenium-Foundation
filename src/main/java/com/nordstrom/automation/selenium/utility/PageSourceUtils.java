package com.nordstrom.automation.selenium.utility;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;

import com.nordstrom.automation.selenium.core.WebDriverUtils;

/**
 * This utility class contains low-level methods that support page source artifact capture.
 */
public final class PageSourceUtils {
    
    private static final String TAKES_ELEMENT_SCREENSHOT = "takesElementScreenshot";
    
    /**
     * Private constructor to prevent instantiation.
     */
    private PageSourceUtils() {
        throw new AssertionError("PageSourceUtils is a static utility class that cannot be instantiated");
    }
    
    /**
     * Determine if the specified driver is capable of producing page source.
     * 
     * @param optDriver optional web driver object
     * @param logger SLF4J logger object; may be 'null'
     * @return 'true' if driver can produce page source; otherwise 'false
     */
    public static boolean canGetArtifact(final Optional<WebDriver> optDriver, final Logger logger) {
        if (optDriver.isPresent()) {
            WebDriver driver = optDriver.get();
            if (driver instanceof HasCapabilities) {
                Capabilities caps = ((HasCapabilities) driver).getCapabilities();
                // if driver explicitly reports that it cannot produce page source
                if (Boolean.FALSE.equals(caps.getCapability(TAKES_ELEMENT_SCREENSHOT))) {
                    if (logger != null) {
                        logger.warn("This driver is not capable of producing page source.");
                    }
                } else {
                    return true;
                }
            } else {
                if (logger != null) {
                    logger.warn("Unable to determine if this driver can capture page source.");
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
     * @param logger SLF4J logger object; may be 'null'
     * @return page source; if capture fails, an empty string is returned
     */
    public static String getArtifact(
                    final Optional<WebDriver> optDriver, final Throwable reason, final Logger logger) {
        
        if (canGetArtifact(optDriver, logger)) {
            WebDriver driver = optDriver.get();
            String rawSource = driver.getPageSource();
            if (rawSource == null) return null;
            
            StringBuilder sourceBuilder = new StringBuilder(rawSource);
            insertBreakpointInfo(sourceBuilder, reason);
            insertBaseElement(sourceBuilder, getPortableUrl(driver));
            insertOriginalUrl(sourceBuilder, driver);
            return sourceBuilder.toString();
        }
        
        return "";
    }
    
    /**
     * Injects a {@code <base>} element into the page source to facilitate relative asset resolution.
     * <p>
     * This method performs several safety checks before modification:
     * <ul>
     * <li>Verifies the URL is non-null.</li>
     * <li>Ensures a {@code <base>} tag does not already exist to prevent document invalidation.</li>
     * <li>Locates a valid insertion point (the beginning of the {@code <head>} or {@code <html>} block).</li>
     * </ul>
     * <p>
     * If a valid offset is found, the URL is decomposed to its base directory, and the tag is 
     * inserted. This ensures that stylesheets, images, and scripts can be resolved by a browser 
     * when the source is viewed as a local file.
     * 
     * @param sourceBuilder the {@link StringBuilder} containing the HTML page source
     * @param url the portable URL used to derive the base path
     */    
    private static void insertBaseElement(final StringBuilder sourceBuilder, final String url) {
        if (url == null || indexOfIgnoreCase(sourceBuilder, "<base ", 0) != -1) {
            return;
        }

        int offset = getBaseInsertionOffset(sourceBuilder);
        if (offset == -1) return;

        try {
            URI uri = URI.create(url);
            String baseHref = getBaseDirectory(uri);
            
            if (baseHref != null) {
                  sourceBuilder.insert(offset, "\">\n")
                      .insert(offset, baseHref)
                      .insert(offset, "<base href=\"")
                      .insert(offset, "\n\n");
            }
        } catch (Exception e) {
            // nothing to do here
        }
    }
    
    /**
     * Insert exception breakpoint information into page source.
     * 
     * @param sourceBuilder {@link StringBuilder} object used to build page source
     * @param reason impetus for capture request; may be 'null'
     */
    private static void insertBreakpointInfo(final StringBuilder sourceBuilder, final Throwable reason) {
        if (reason == null) return;

        int offset = getBreakpointInsertionOffset(sourceBuilder);
        Throwable cause = WebDriverUtils.getReportableCause(reason);
        StackTraceElement breakpoint = WebDriverUtils.getClientBreakpoint(cause);
        
        if (breakpoint != null) {
            sourceBuilder.insert(offset, " -->\n")
              .insert(offset, breakpoint.getLineNumber())
              .insert(offset, ":")
              .insert(offset, breakpoint.getClassName())
              .insert(offset, "<!-- at ");
        }
        
        String message = (cause.getMessage() != null) ?
                cause.getMessage().replace("--", "-") : "(no message)";

        sourceBuilder.insert(offset, " -->\n")
                .insert(offset, message)
                .insert(offset, ": ")
                .insert(offset, cause.getClass().getSimpleName())
                .insert(offset, "<!-- ");

    }
    
    /**
     * Insert original URL information into page source.
     * 
     * @param sourceBuilder {@link StringBuilder} object used to build page source
     * @param driver web driver object
     */
    private static void insertOriginalUrl(final StringBuilder sourceBuilder, final WebDriver driver) {
        String url = invokeSafely(driver::getCurrentUrl);
        if (url != null) {
            sourceBuilder.append("\n<!-- Original URL: ").append(url).append(" -->");
        }
    }
    
    /**
     * Determine the safe offset for inserting diagnostic header information.
     * <p>
     * This method ensures that comments are not inserted before an XML declaration 
     * (e.g., {@code <?xml ... ?>}) or a DOCTYPE declaration, as doing so can render 
     * the document invalid.
     * 
     * @param sourceBuilder the {@link StringBuilder} containing the page source
     * @return the character index where diagnostic info can be safely inserted
     */
    private static int getBreakpointInsertionOffset(final StringBuilder sourceBuilder) {
        int safeIndex = 0;
        int xmlDeclEnd = sourceBuilder.indexOf("?>");
        if (xmlDeclEnd != -1 && xmlDeclEnd < 100) {
            safeIndex = xmlDeclEnd + 2;
        } else {
            int docTypeIndex = indexOfIgnoreCase(sourceBuilder, "<!DOCTYPE", 0);
            if (docTypeIndex != -1) {
                int docTypeEnd = sourceBuilder.indexOf(">", docTypeIndex);
                if (docTypeEnd != -1) safeIndex = docTypeEnd + 1;
            }
        }
        return safeIndex;
    }
    
    /**
     * Determine the optimal insertion point for a {@code <base>} element.
     * <p>
     * The method prioritizes the {@code <head>} node for standards compliance. 
     * If missing, it falls back to the {@code <html>} node.
     * 
     * @param stringBuilder the {@link StringBuilder} containing the page source
     * @return the character index after the opening tag; -1 if no suitable node is found
     */
    private static int getBaseInsertionOffset(final StringBuilder stringBuilder) {
        int index = indexOfIgnoreCase(stringBuilder, "<head>", 0);
        if (index != -1) return index + 6;

        index = indexOfIgnoreCase(stringBuilder, "<html>", 0);
        if (index != -1) return index + 6;

        return -1;
    }

    /**
     * Extract the base directory from a given URI to facilitate relative asset resolution.
     * <p>
     * This strips the filename and query parameters, ensuring the returned string 
     * represents the parent directory (e.g., "http://ex.com/a/b.html" -> "http://ex.com/a/").
     * 
     * @param uri the {@link URI} to process
     * @return the base directory URL string; {@code null} if the URI is missing scheme/authority
     */
    private static String getBaseDirectory(final URI uri) {
        String scheme = uri.getScheme();
        String authority = uri.getAuthority();
        if (scheme == null || authority == null) return null;

        String path = uri.getPath();
        String rootPath = (path != null && path.contains("/")) 
            ? path.substring(0, path.lastIndexOf('/') + 1) 
            : "/";

        return scheme + "://" + authority + rootPath;
    }
    
    /**
     * Retrieve the current URL from the driver, filtering out non-portable addresses.
     * <p>
     * This method excludes loopback addresses (localhost, 127.0.0.1) and non-web 
     * protocols (file://, data:). URLs that fail portability checks are omitted 
     * to prevent misleading diagnostic snapshots.
     * 
     * @param driver the {@link WebDriver} instance
     * @return a portable Web URL string; {@code null} if the URL is local or invalid
     */
    public static String getPortableUrl(final WebDriver driver) {
        String url = invokeSafely(driver::getCurrentUrl);

        if (url == null || !url.toLowerCase().startsWith("http")) {
            return null;
        }

        try {
            URI uri = URI.create(url);
            String host = uri.getHost();

            if (host == null) {
                return null;
            }

            host = host.toLowerCase();

            if (host.equals("localhost") || 
                host.equals("127.0.0.1") || 
                host.equals("0.0.0.0") || 
                host.equals("::1")) {
                return null;
            }

            return url;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Perform a case-insensitive search within a {@link StringBuilder} without 
     * creating temporary String allocations.
     * 
     * @param stringBuilder the buffer to search
     * @param search the target sequence to find
     * @param fromIndex the index to start the search from
     * @return the starting index of the match; -1 if not found
     */
    private static int indexOfIgnoreCase(final StringBuilder stringBuilder, final String search, final int fromIndex) {
        final int searchLen = search.length();
        final int max = stringBuilder.length() - searchLen;
        if (fromIndex > max) return -1;

        for (int i = fromIndex; i <= max; i++) {
            boolean match = true;
            for (int j = 0; j < searchLen; j++) {
                char c1 = stringBuilder.charAt(i + j);
                char c2 = search.charAt(j);
                if (c1 != c2 && Character.toLowerCase(c1) != Character.toLowerCase(c2)) {
                    match = false;
                    break;
                }
            }
            if (match) return i;
        }
        return -1;
    }
    
    /**
     * Executes a {@link Callable} and returns the result, suppressing all exceptions.
     * <p>
     * This is used for diagnostic "best-effort" data collection where a failure 
     * in metadata retrieval should not crash the primary reporting flow.
     * 
     * @param <T> the type of result
     * @param callable the task to execute
     * @return the result of the task; {@code null} if an exception occurred
     */
    public static <T> T invokeSafely(final Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            return null;
        }
    }
}
