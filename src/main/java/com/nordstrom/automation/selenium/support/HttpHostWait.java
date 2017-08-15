package com.nordstrom.automation.selenium.support;

import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.openqa.selenium.support.ui.Clock;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Sleeper;
import org.openqa.selenium.support.ui.SystemClock;

public class HttpHostWait extends FluentWait<HttpHost> {
    public final static long DEFAULT_SLEEP_TIMEOUT = 500;

    /**
     *
     * @param host
     *            The HttpHost to pass to the expected conditions
     * @param timeOutInSeconds
     *            The timeout in seconds when an expectation is called
     */
    public HttpHostWait(HttpHost host, long timeOutInSeconds) {
        this(host, new SystemClock(), Sleeper.SYSTEM_SLEEPER, timeOutInSeconds, DEFAULT_SLEEP_TIMEOUT);
    }

    /**
     *
     * @param host
     *            The HttpHost to pass to the expected conditions
     * @param timeOutInSeconds
     *            The timeout in seconds when an expectation is called
     * @param sleepInMillis
     *            The duration in milliseconds to sleep between polls.
     */
    public HttpHostWait(HttpHost host, long timeOutInSeconds, long sleepInMillis) {
        this(host, new SystemClock(), Sleeper.SYSTEM_SLEEPER, timeOutInSeconds, sleepInMillis);
    }

    /**
     * 
     * @param host
     *            The HttpHost to pass to the expected conditions
     * @param clock
     *            The clock to use when measuring the timeout
     * @param sleeper
     *            Object used to make the current thread go to sleep.
     * @param timeOutInSeconds
     *            The timeout in seconds when an expectation is
     * @param sleepTimeOut
     *            The timeout used whilst sleeping. Defaults to 500ms called.
     */
    public HttpHostWait(HttpHost host, Clock clock, Sleeper sleeper, long timeOutInSeconds, long sleepTimeOut) {
        super(host, clock, sleeper);
        withTimeout(timeOutInSeconds, TimeUnit.SECONDS);
        pollingEvery(sleepTimeOut, TimeUnit.MILLISECONDS);
    }
}