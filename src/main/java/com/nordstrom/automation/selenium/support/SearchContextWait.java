package com.nordstrom.automation.selenium.support;

import java.time.Duration;
import java.util.Optional;

import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Function;
import com.nordstrom.automation.selenium.core.TestBase;
import com.nordstrom.automation.selenium.core.WebDriverUtils;

/**
 * This class extends {@link FluentWait}, specifying {@link SearchContext} as the type parameter. This enables you to
 * specify 'wait' operations within a specific search context. By contrast, the standard {@link WebDriverWait} class
 * always operates within the context of the driver, which encompasses the entire page. 
 */
public class SearchContextWait extends FluentWait<SearchContext> {
    /** default sleep timeout (500 milliseconds) */
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
    public SearchContextWait(final SearchContext context, final long timeOutInSeconds) {
        this(context, timeOutInSeconds, DEFAULT_SLEEP_TIMEOUT);
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
    public SearchContextWait(final SearchContext context, final long timeOutInSeconds, final long sleepInMillis) {
        super(context);
        withTimeout(Duration.ofSeconds(timeOutInSeconds));
        pollingEvery(Duration.ofMillis(sleepInMillis));
        ignoring(NotFoundException.class);
        this.context = context;
    }

    /**
     * Wait until the specified condition is satisfied.
     * <p>
     * This overload accepts an {@link ExpectedCondition}, bridging the gap between
     * {@link FluentWait}&lt;{@link SearchContext}&gt; and the {@link WebDriver}-based
     * {@code ExpectedCondition} interface. The search context is cast to {@link WebDriver}
     * for compatibility with standard {@code ExpectedConditions} factory methods.
     *
     * @param <V> return type of the condition
     * @param condition {@link ExpectedCondition} to evaluate
     * @return the condition's return value if it is not {@code null} or {@code false}
     * @throws org.openqa.selenium.TimeoutException if the condition is not satisfied
     *         within the configured timeout
     */
    public <V> V until(ExpectedCondition<V> condition) {
        return until((Function<SearchContext, V>) d -> condition.apply((WebDriver) d));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected RuntimeException timeoutException(final String message, final Throwable lastException) {
        TimeoutException ex = new TimeoutException(message, lastException);
        ex.addInfo(WebDriverException.DRIVER_INFO, context.getClass().getName());
        Optional.ofNullable(WebDriverUtils.getDriver(context))
            .filter(RemoteWebDriver.class::isInstance)
            .map(RemoteWebDriver.class::cast)
            .ifPresent(remote -> {
                Optional.ofNullable(remote.getSessionId())
                    .map(Object::toString)
                    .ifPresent(sid -> ex.addInfo(WebDriverException.SESSION_ID, sid));

                TestBase.invokeSafely(remote::getCapabilities)
                    .map(Object::toString)
                    .ifPresent(caps -> ex.addInfo("Capabilities", caps));
            });
        throw ex;
    }
    
    /**
     * Get the search context for which this 'wait' proxy was created.
     * 
     * @return subject {@link SearchContext}
     */
    public SearchContext getContext() {
        return context;
    }
}
