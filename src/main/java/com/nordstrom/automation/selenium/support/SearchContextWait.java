package com.nordstrom.automation.selenium.support;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.Clock;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Sleeper;
import org.openqa.selenium.support.ui.SystemClock;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.nordstrom.automation.selenium.core.WebDriverUtils;

/**
 * This class extends {@link FluentWait}, specifying {@link SearchContext} as the type parameter. This enables you to
 * specify 'wait' operations within a specific search context. By contrast, the standard {@link WebDriverWait} class
 * always operates within the context of the driver, which encompasses the entire page. 
 */
public class SearchContextWait extends FluentWait<SearchContext> {
    public static final long DEFAULT_SLEEP_TIMEOUT = 500;
    private final SearchContext context;

    /**
     * Wait will ignore instances of NotFoundException that are encountered
     * (thrown) by default in the 'until' condition, and immediately propagate
     * all others. You can add more to the ignore list by calling
     * ignoring(exceptions to add).
     *
     * @param context
     *            The SearchContext instance to pass to the expected conditions
     * @param timeOutInSeconds
     *            The timeout in seconds when an expectation is called
     * @see SearchContextWait#ignoring(java.lang.Class)
     */
    public SearchContextWait(SearchContext context, long timeOutInSeconds) {
        this(context, new SystemClock(), Sleeper.SYSTEM_SLEEPER, timeOutInSeconds, DEFAULT_SLEEP_TIMEOUT);
    }

    /**
     * Wait will ignore instances of NotFoundException that are encountered
     * (thrown) by default in the 'until' condition, and immediately propagate
     * all others. You can add more to the ignore list by calling
     * ignoring(exceptions to add).
     *
     * @param context
     *            The SearchContext instance to pass to the expected conditions
     * @param timeOutInSeconds
     *            The timeout in seconds when an expectation is called
     * @param sleepInMillis
     *            The duration in milliseconds to sleep between polls.
     * @see SearchContextWait#ignoring(java.lang.Class)
     */
    public SearchContextWait(SearchContext context, long timeOutInSeconds, long sleepInMillis) {
        this(context, new SystemClock(), Sleeper.SYSTEM_SLEEPER, timeOutInSeconds, sleepInMillis);
    }

    /**
     * Wait will ignore instances of NotFoundException that are encountered
     * (thrown) by default in the 'until' condition, and immediately propagate
     * all others. You can add more to the ignore list by calling
     * ignoring(exceptions to add).
     * 
     * @param context
     *            The SearchContext instance to pass to the expected conditions
     * @param clock
     *            The clock to use when measuring the timeout
     * @param sleeper
     *            Object used to make the current thread go to sleep.
     * @param timeOutInSeconds
     *            The timeout in seconds when an expectation is
     * @param sleepTimeOut
     *            The timeout used whilst sleeping. Defaults to 500ms called.
     */
    public SearchContextWait(SearchContext context, Clock clock, Sleeper sleeper, long timeOutInSeconds,
            long sleepTimeOut) {
        super(context, clock, sleeper);
        withTimeout(timeOutInSeconds, TimeUnit.SECONDS);
        pollingEvery(sleepTimeOut, TimeUnit.MILLISECONDS);
        ignoring(NotFoundException.class);
        this.context = context;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected RuntimeException timeoutException(String message, Throwable lastException) {
        TimeoutException ex = new TimeoutException(message, lastException);
        ex.addInfo(WebDriverException.DRIVER_INFO, context.getClass().getName());
        WebDriver driver = WebDriverUtils.getDriver(context);
        if (driver instanceof RemoteWebDriver) {
            RemoteWebDriver remote = (RemoteWebDriver) driver;
            if (remote.getSessionId() != null) {
                ex.addInfo(WebDriverException.SESSION_ID, remote.getSessionId().toString());
            }
            if (remote.getCapabilities() != null) {
                ex.addInfo("Capabilities", remote.getCapabilities().toString());
            }
        }
        throw ex;
    }
}
