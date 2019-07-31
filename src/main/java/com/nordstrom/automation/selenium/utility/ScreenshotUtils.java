package com.nordstrom.automation.selenium.utility;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.CapabilityType;
import org.slf4j.Logger;

import com.google.common.base.Optional;
import com.google.common.io.BaseEncoding;

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
        if (optDriver.isPresent()) {
            WebDriver driver = optDriver.get();
            if (driver instanceof HasCapabilities) {
                if (((HasCapabilities) driver).getCapabilities().is(CapabilityType.TAKES_SCREENSHOT)) {
                    return true;
                }
            }
            if (driver instanceof TakesScreenshot) {
                return true; // for remote drivers, this may be bogus
            }
            if (logger != null) {
                logger.warn("This driver is not able to take screenshots."); //NOSONAR
            }
        }
        return false;
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
                    final Optional<WebDriver> optDriver, final Throwable reason, final Logger logger) { //NOSONAR
        
        if (canGetArtifact(optDriver, logger)) {
            try {
                WebDriver driver = optDriver.get();
                return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            } catch (WebDriverException e) {
                if (e.getCause() instanceof ClassCastException) {
                    return proxyArtifact();
                } else if (logger != null) {
                    logger.warn("The driver is capable of taking a screenshot, but it failed.", e);
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
    private static byte[] proxyArtifact(){ 
        BufferedImage image = new BufferedImage(155, 45, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = image.getGraphics();
        graphics.drawString("This remote driver is not", 10, 20);
        graphics.drawString("able to take screenshots", 10, 35);
        
        String imageString = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            ImageIO.write(image, "png", bos);
            imageString = BaseEncoding.base64().encode(bos.toByteArray());
            bos.close();
            
            return OutputType.BYTES.convertFromBase64Png(imageString);
        } catch (IOException e) {
            return new byte[0];
        }
    }
}
