package com.nordstrom.automation.selenium.utility;

import java.util.Optional;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;

public final class ScreenshotUtils {
    
    private ScreenshotUtils() {
        throw new AssertionError("ScreenshotUtils is a static utility class that cannot be instantiated");
    }
    
    /**
     * Determine if the specified driver is capable of taking screenshots.
     * 
     * @param optDriver optional web driver object
     * @param logger SLF4J logger object
     * @return 'true' if driver can take screenshots; otherwise 'false
     */
    public static boolean canGetArtifact(Optional<WebDriver> optDriver, Logger logger) {
        if (optDriver.isPresent()) {
            WebDriver driver = optDriver.get();
            if (driver instanceof TakesScreenshot) {
                return true;
            } else {
                String message = "This driver is not capable of taking a screenshot.  If a screenshot is desired, use"
                        + " a WebDriver implementation that supports screenshots. For more information, see https://"
                        + "seleniumhq.github.io/selenium/docs/api/java/org/openqa/selenium/TakesScreenshot.html";
                logger.warn(message);
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
    public static byte[] getArtifact(Optional<WebDriver> optDriver, Throwable reason, Logger logger) {
        if (canGetArtifact(optDriver, logger)) {
            try {
                WebDriver driver = optDriver.get();
                return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            } catch (WebDriverException e) {
                logger.warn("The driver is capable of taking a screenshot, but it failed.", e);
            }
        }
        return new byte[0];
    }
}
