package com.nordstrom.automation.selenium.exceptions;

import org.jspecify.annotations.Nullable;
import org.openqa.selenium.WebDriverException;

/**
 * Base class for all Selenium Foundation–specific runtime exceptions.
 *
 * <p>This class serves as the common root for exceptions thrown by the
 * Selenium Foundation framework itself, as distinct from:</p>
 *
 * <ul>
 *   <li>Native Selenium exceptions</li>
 *   <li>User-requested exceptions originating from JavaScript</li>
 *   <li>Low-level WebDriver protocol or transport failures</li>
 * </ul>
 *
 * <p>By extending {@link WebDriverException}, all Selenium Foundation
 * exceptions integrate seamlessly with Selenium’s existing exception
 * handling, reporting, and retry semantics.</p>
 *
 * <p>This class does not add new behavior beyond its superclass; it exists
 * to provide a clear type boundary and a stable anchor point for:</p>
 *
 * <ul>
 *   <li>Exception whitelisting and policy enforcement</li>
 *   <li>Framework-specific error classification</li>
 *   <li>Consistent exception handling across Java and JavaScript execution</li>
 * </ul>
 *
 * <p>All subclasses are expected to represent non-recoverable runtime
 * failures within the Selenium Foundation infrastructure.</p>
 */
public class SeleniumFoundationException extends WebDriverException {

    private static final long serialVersionUID = 4265508571189882832L;

    /**
     * Constructs a new Selenium Foundation exception with no detail message
     * or cause.
     */
    public SeleniumFoundationException() {
        super();
    }

    /**
     * Constructs a new Selenium Foundation exception with the specified
     * detail message.
     *
     * @param message
     *        the detail message, or {@code null}
     */
    public SeleniumFoundationException(@Nullable String message) {
        super(message);
    }

    /**
     * Constructs a new Selenium Foundation exception with the specified cause.
     *
     * @param cause
     *        the underlying cause of this exception, or {@code null}
     */
    public SeleniumFoundationException(@Nullable Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new Selenium Foundation exception with the specified
     * detail message and cause.
     *
     * @param message
     *        the detail message, or {@code null}
     * @param cause
     *        the underlying cause of this exception, or {@code null}
     */
    public SeleniumFoundationException(@Nullable String message,
                                       @Nullable Throwable cause) {
        super(message, cause);
    }
}
