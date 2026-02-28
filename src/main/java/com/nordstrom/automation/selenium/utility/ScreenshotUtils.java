package com.nordstrom.automation.selenium.utility;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.UnsupportedCommandException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;

import com.nordstrom.automation.selenium.core.TestBase;
import com.nordstrom.automation.selenium.core.WebDriverUtils;

/**
 * This utility class contains low-level methods that support screenshot artifact capture.
 */
public final class ScreenshotUtils {
    
    /**
     * Private constructor to prevent instantiation.
     */
    private ScreenshotUtils() {
        throw new AssertionError("ScreenshotUtils is a static utility class that cannot be instantiated");
    }
    
    /**
     * Determine if the specified driver is capable of taking screenshots.
     * 
     * @param optDriver optional web driver object
     * @param logger SLF4J logger object; may be 'null'
     * @return 'true' if driver can take screenshots; otherwise 'false'
     */
    public static boolean canGetArtifact(final Optional<WebDriver> optDriver, final Logger logger) {
        if (!optDriver.isPresent()) return false;
        
        WebDriver driver = optDriver.get();
        if (!(driver instanceof TakesScreenshot)) {
            if (logger != null) {
                logger.warn("Driver does not implement TakesScreenshot; skipping artifact.");
            }
            return false;
        }

        if (driver instanceof HasCapabilities) {
            Capabilities caps = TestBase.invokeSafely(((HasCapabilities) driver)::getCapabilities);
            
            if (caps == null) {
                if (logger != null) {
                    logger.warn("Driver session appears to be dead; cannot capture screenshot.");
                }
                return false;
            }

            Object capability = caps.getCapability("takesScreenshot");
            if (capability instanceof Boolean && !(Boolean) capability) {
                return false; 
            }
        }

        return true;
    }
    
    /**
     * Produce a screenshot from the specified driver.
     * 
     * @param optDriver optional web driver object
     * @param reason impetus for capture request; may be 'null'
     * @param logger SLF4J logger object; may be 'null'
     * @return screenshot artifact; if capture fails, an empty byte array is returned
     */
    public static byte[] getArtifact(
                    final Optional<WebDriver> optDriver, final Throwable reason, final Logger logger) {
        
        if (canGetArtifact(optDriver, logger)) {
            try {
                WebDriver driver = optDriver.get();
                return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            } catch (UnsupportedCommandException e) {
                return proxyArtifact();
            } catch (WebDriverException e) {
                if (WebDriverUtils.isClassCastFailure(e)) {
                    return proxyArtifact();
                } else if (logger != null) {
                    logger.warn("Failed taking a screenshot.", e);
                }
            }
        }
        return new byte[0];
    }
    
    /**
     * Produce a proxy screenshot artifact for incapable remote drivers.
     * 
     * @return proxy screenshot artifact
     */
    private static byte[] proxyArtifact() {
        BufferedImage image = new BufferedImage(155, 45, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = image.getGraphics();
        
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, 155, 45);
            graphics.setColor(Color.BLACK);
            graphics.drawString("This remote driver is not", 10, 20);
            graphics.drawString("able to take screenshots", 10, 35);
        } finally {
            graphics.dispose();
        }

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", bos);
            return bos.toByteArray();
        } catch (IOException e) {
            return new byte[0];
        }
    }
}
