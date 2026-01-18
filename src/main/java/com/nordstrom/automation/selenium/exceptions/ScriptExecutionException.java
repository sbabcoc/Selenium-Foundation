package com.nordstrom.automation.selenium.exceptions;

/**
 * Thrown when execution of a JavaScript fragment via Selenium fails
 * for reasons other than a user-requested exception.
 *
 * <p>This exception represents:
 * <ul>
 *   <li>JavaScript runtime errors</li>
 *   <li>Marshaling or protocol failures</li>
 *   <li>Disallowed or malformed user exception requests</li>
 *   <li>Infrastructure or driver-level execution failures</li>
 * </ul>
 *
 * <p>If available, the JavaScript stack trace is attached as a
 * suppressed {@link JavaScriptStackTrace} to preserve browser-side
 * execution context.</p>
 *
 * <p>This exception is used internally by the JavaScript runtime bridge
 * and exception factory to report failures that cannot or should not be
 * translated into a user-requested Java exception.</p>
 */
public class ScriptExecutionException extends SeleniumFoundationException {

    private static final long serialVersionUID = 1L;

    /**
     * The fully qualified class name of the exception requested by the
     * JavaScript runtime, if applicable.
     *
     * <p>This value is {@code null} when the failure is not associated
     * with a specific user-requested exception class (for example,
     * protocol failures or runtime errors).</p>
     */
    private final String requestedExceptionClass;

    /**
     * Constructs a new {@code ScriptExecutionException}.
     *
     * @param message
     *        human-readable description of the failure
     * @param requestedExceptionClass
     *        fully qualified class name of the requested exception,
     *        or {@code null} if not applicable
     * @param cause
     *        underlying cause of the failure, or {@code null} if none
     */
    private ScriptExecutionException(String message,
                                     String requestedExceptionClass,
                                     Throwable cause) {
        super(message, cause);
        this.requestedExceptionClass = requestedExceptionClass;
    }

    /**
     * Returns the fully qualified class name of the exception that the
     * JavaScript runtime attempted to create, if any.
     *
     * @return requested exception class name, or {@code null} if this
     *         failure was not associated with a user-requested exception
     */
    public String getRequestedExceptionClass() {
        return requestedExceptionClass;
    }

    /* ---------- Static factories ---------- */

    /**
     * Creates an exception indicating that a requested exception class
     * violated the configured exception policy.
     *
     * @param requestedClass
     *        fully qualified name of the disallowed exception class
     * @param message
     *        description of the policy violation
     * @return a new {@code ScriptExecutionException}
     */
    public static ScriptExecutionException policyViolation(
            String requestedClass,
            String message) {
        return new ScriptExecutionException(message, requestedClass, null);
    }

    /**
     * Creates an exception indicating that instantiation of a requested
     * exception class failed.
     *
     * <p>This typically indicates a missing constructor, reflective
     * access failure, or other instantiation error.</p>
     *
     * @param requestedClass
     *        fully qualified name of the requested exception class
     * @param message
     *        description of the instantiation failure
     * @param cause
     *        underlying reflective or construction failure
     * @return a new {@code ScriptExecutionException}
     */
    public static ScriptExecutionException instantiationFailure(
            String requestedClass,
            String message,
            Throwable cause) {
        return new ScriptExecutionException(message, requestedClass, cause);
    }

    /**
     * Creates an exception indicating that the JavaScript runtime returned
     * a malformed or unexpected payload.
     *
     * <p>This typically indicates a protocol mismatch, version skew, or
     * corruption of the runtime result structure.</p>
     *
     * @param message
     *        description of the payload error
     * @return a new {@code ScriptExecutionException}
     */
    public static ScriptExecutionException malformedPayload(String message) {
        return new ScriptExecutionException(message, null, null);
    }

    /**
     * Creates an exception indicating a general JavaScript execution
     * failure unrelated to a user-requested exception.
     *
     * @param message
     *        description of the execution failure
     * @param cause
     *        underlying cause, such as a driver or transport error
     * @return a new {@code ScriptExecutionException}
     */
    public static ScriptExecutionException executionFailure(
            String message,
            Throwable cause) {
        return new ScriptExecutionException(message, null, cause);
    }
}
