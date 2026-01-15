package com.nordstrom.automation.selenium.exceptions;

/**
 * Lightweight exception used to carry a JavaScript stack trace alongside a
 * Java exception.
 *
 * <p>This type is not intended to represent a failure condition on its own.
 * Instead, it is attached to another {@link Throwable} (typically as a
 * suppressed exception) to preserve JavaScript execution context when
 * propagating errors from the browser into Java.</p>
 *
 * <p>The contained message consists solely of the formatted JavaScript stack
 * trace. No Java stack trace is generated for this exception.</p>
 *
 * <p>This class overrides {@link #fillInStackTrace()} to prevent population of
 * a Java stack trace, minimizing overhead and ensuring that only the JavaScript
 * stack is reported.</p>
 *
 * @see com.nordstrom.automation.selenium.core.ExceptionFactory
 */
public final class JavaScriptStackTrace extends SeleniumFoundationException {

    private static final long serialVersionUID = 4823563816505831048L;

    /**
     * Constructs a new {@code JavaScriptStackTrace} containing the supplied
     * JavaScript stack trace text.
     *
     * @param stack
     *        the JavaScript stack trace to record; must not be {@code null}
     */
    public JavaScriptStackTrace(String stack) {
        super("JavaScript stack trace:\n" + stack);
    }

    /**
     * Suppresses generation of a Java stack trace.
     *
     * <p>This exception is intended only as a carrier for JavaScript stack
     * information and does not represent a point of execution in Java code.</p>
     *
     * @return this exception instance
     */
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
