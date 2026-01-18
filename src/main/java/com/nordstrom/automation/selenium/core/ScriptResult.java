package com.nordstrom.automation.selenium.core;

import java.util.Map;

import com.nordstrom.automation.selenium.exceptions.ScriptExecutionException;

/**
 * Represents the normalized result envelope returned from JavaScript execution.
 *
 * <p>This class provides a stable, driver-agnostic interpretation of values
 * returned from {@link org.openqa.selenium.JavascriptExecutor#executeScript}
 * and {@code executeAsyncScript}. Raw return values are inspected and, if
 * necessary, decoded from a structured result envelope produced by the
 * injected JavaScript runtime.</p>
 *
 * <b>Result formats</b>
 *
 * <p>The JavaScript runtime may return either:</p>
 * <ul>
 *   <li>A raw value (for successful execution with no runtime envelope)</li>
 *   <li>A structured {@link Map} envelope with the following shape:</li>
 * </ul>
 *
 * <pre>
 * {
 *   status: "ok",
 *   value: &lt;any&gt;
 * }
 *
 * {
 *   status: "error",
 *   exception: {
 *     className: "com.example.MyException",
 *     message: "Failure message",
 *     stack: "JavaScript stack trace"
 *   }
 * }
 * </pre>
 *
 * <p>This class normalizes both forms into a consistent Java representation.</p>
 *
 * <b>Error handling</b>
 *
 * <p>If the payload claims to represent an error but does not conform to the
 * expected structure, a {@link ScriptExecutionException} is thrown to signal
 * a protocol or runtime contract violation.</p>
 *
 * <p>This class does <em>not</em> create or throw exceptions itself. It merely
 * captures the decoded result so that higher-level components (such as
 * {@link ExceptionFactory}) can construct and throw appropriate Java
 * exceptions.</p>
 */
public final class ScriptResult {

    /** Indicates whether the result represents an error condition. */
    private final boolean error;

    /** The successful return value, or {@code null} if this result is an error. */
    private final Object value;

    /** Fully qualified name of the requested Java exception class, if any. */
    private final String exceptionClassName;

    /** Exception message provided by the JavaScript runtime, if any. */
    private final String message;

    /** JavaScript stack trace captured at the point of failure, if available. */
    private final String stack;

    /**
     * Creates a new {@code ScriptResult}.
     *
     * @param error
     *        {@code true} if this result represents an error
     * @param value
     *        successful return value, or {@code null} for errors
     * @param exceptionClassName
     *        fully qualified exception class name, or {@code null}
     * @param message
     *        exception message, or {@code null}
     * @param stack
     *        JavaScript stack trace, or {@code null}
     */
    private ScriptResult(boolean error,
                         Object value,
                         String exceptionClassName,
                         String message,
                         String stack) {
        this.error = error;
        this.value = value;
        this.exceptionClassName = exceptionClassName;
        this.message = message;
        this.stack = stack;
    }

    /**
     * Converts a raw value returned from Selenium JavaScript execution into a
     * normalized {@code ScriptResult}.
     *
     * <p>If the value is not a {@link Map}, it is treated as a successful
     * return value and wrapped directly.</p>
     *
     * <p>If the value is a {@link Map}, it is interpreted as a structured
     * runtime envelope. The {@code status} field determines whether the
     * result represents success or failure.</p>
     *
     * @param raw
     *        raw value returned from Selenium JavaScript execution
     * @return normalized {@code ScriptResult}
     * @throws ScriptExecutionException
     *         if the payload claims to represent an error but is malformed
     *         or missing required fields
     */
    @SuppressWarnings("unchecked")
    public static ScriptResult from(Object raw) {
        if (!(raw instanceof Map)) {
            return new ScriptResult(false, raw, null, null, null);
        }

        Map<String, Object> map = (Map<String, Object>) raw;
        Object status = map.get("status");

        if (!"error".equals(status)) {
            return new ScriptResult(false, map.get("value"), null, null, null);
        }

        Object exceptionObj = map.get("exception");
        if (!(exceptionObj instanceof Map)) {
            throw ScriptExecutionException.malformedPayload(
                    "Missing or invalid exception object");
        }

        Map<String, Object> ex = (Map<String, Object>) exceptionObj;

        return new ScriptResult(
                true,
                null,
                string(ex.get("className")),
                string(ex.get("message")),
                string(ex.get("stack"))
        );
    }

    /**
     * Safely converts an arbitrary object to a string.
     *
     * @param o
     *        value to convert
     * @return string representation, or {@code null} if the value is {@code null}
     */
    private static String string(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    /**
     * Indicates whether this result represents an error condition.
     *
     * @return {@code true} if this result represents an error
     */
    public boolean isError() {
        return error;
    }

    /**
     * Returns the successful return value.
     *
     * @return value returned by the JavaScript execution, or {@code null}
     *         if this result represents an error
     */
    public Object getValue() {
        return value;
    }

    /**
     * Returns the fully qualified name of the requested Java exception class.
     *
     * @return exception class name, or {@code null} if not applicable
     */
    public String getExceptionClassName() {
        return exceptionClassName;
    }

    /**
     * Returns the exception message provided by the JavaScript runtime.
     *
     * @return exception message, or {@code null} if not provided
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the JavaScript stack trace captured at the point of failure.
     *
     * @return JavaScript stack trace, or {@code null} if not available
     */
    public String getStack() {
        return stack;
    }
}
