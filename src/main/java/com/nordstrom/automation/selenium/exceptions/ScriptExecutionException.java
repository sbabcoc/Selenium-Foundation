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
 * suppressed {@link JavaScriptStackTrace}.
 */
public class ScriptExecutionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String requestedExceptionClass;

    private ScriptExecutionException(String message, String requestedExceptionClass, Throwable cause) {
        super(message, cause);
        this.requestedExceptionClass = requestedExceptionClass;
    }

    public String getRequestedExceptionClass() {
        return requestedExceptionClass;
    }

    /* ---------- Static factories ---------- */

    public static ScriptExecutionException policyViolation(String requestedClass, String message) {
        return new ScriptExecutionException(message, requestedClass, null);
    }

    public static ScriptExecutionException instantiationFailure(String requestedClass, String message,
            Throwable cause) {
        return new ScriptExecutionException(message, requestedClass, cause);
    }

    public static ScriptExecutionException malformedPayload(String message) {
        return new ScriptExecutionException(message, null, null);
    }

    public static ScriptExecutionException executionFailure(String message, Throwable cause) {
        return new ScriptExecutionException(message, null, cause);
    }
}
