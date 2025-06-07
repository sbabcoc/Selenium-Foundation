package com.nordstrom.automation.selenium.model;

import org.openqa.selenium.Alert;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

import java.time.Duration;
import java.util.Optional;

/**
 * This class is a wrapper for the <b>Selenium</b> {@link Alert} class, providing an interface that treats browser
 * modals as the application-level entities that they are instead of the pseudo-contexts presented by the standard
 * API. 
 */
public abstract class AlertHandler {
    /** driver for this handler */
    protected final WebDriver driver;
    /** alert handler parent page */
    protected final Page parentPage;
    /** alert handler wait timeout */
    protected final Duration timeout;

    /**
     * Constructor for an alert handler with default timeout.
     * 
     * @param parentPage alert parent page
     */
    public AlertHandler(final Page parentPage) {
        this(parentPage, Duration.ofSeconds(5));
    }

    /**
     * Constructor for an alert handler with specified timeout.
     * 
     * @param parentPage alert parent page
     * @param timeout maximum interval to wait for an alert to appear
     */
    public AlertHandler(final Page parentPage, final Duration timeout) {
        this.driver = parentPage.getWrappedDriver();
        this.parentPage = parentPage;
        this.timeout = timeout;
    }
    
    /**
     * Wait for browser alert up to the timeout interval.
     * 
     * @return {@link Alert} object if alert appears; {@code null} if wait is interrupted
     * @throws TimeoutException if alert is still absent when wait interval expires
     */
    protected Alert waitForAlert() {
        long start = System.currentTimeMillis();
        long end = start + timeout.toMillis();

        while (System.currentTimeMillis() < end) {
            try {
                return driver.switchTo().alert();
            } catch (NoAlertPresentException e) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }
        throw new TimeoutException("Alert not present after waiting " + timeout.getSeconds() + " seconds.");
    }

    /**
     * Switch driver focus to browser alert if one is present.
     * 
     * @return {@link Alert} object; {@code null} if alert is absent
     */
    protected Alert switchIfAlertExists() {
        try {
            return driver.switchTo().alert();
        } catch (NoAlertPresentException e) {
            return null;
        }
    }
    
    /**
     * Determine if browser alert is present.
     * 
     * @return {@code true} if alert exists; otherwise {@code false}
     */
    public boolean exists() {
        return (null != switchIfAlertExists());
    }

    /**
     * Get text of browser alert.
     * 
     * @return {@link Alert} text; {@code null} if alert is absent
     */
    public String getText() {
        return Optional.ofNullable(switchIfAlertExists()).map(alert -> alert.getText()).orElse(null);
    }

    /**
     * Wait for browser alert and accept it.
     * 
     * @return alert text; {code null} if wait is interrupted
     * @throws TimeoutException if alert is still absent when wait interval expires
     */
    abstract public Page accept();

    /**
     * Wait for browser alert, then send the specified keys and accept the alert.
     * 
     * @param keys keys to send
     * @return alert text; {code null} if wait is interrupted
     * @throws TimeoutException if alert is still absent when wait interval expires
     */
    abstract public Page sendKeysAndAccept(String keys);
    
    /**
     * Wait for browser alert and dismiss it.
     * 
     * @return alert text; {code null} if wait is interrupted
     * @throws TimeoutException if alert is still absent when wait interval expires
     */
    abstract public Page dismiss();
    
    /**
     * If browser alert is present, accept it.
     * 
     * @return if alert is present, landing page; otherwise parent page
     */
    public Page acceptIfPresent() {
        return exists() ? accept() : parentPage;
    }

    /**
     * If browser alert is present, dismiss it.
     * 
     * @return if alert is present, landing page; otherwise parent page
     */
    public Page dismissIfPresent() {
        return exists() ? dismiss() : parentPage;
    }
}
