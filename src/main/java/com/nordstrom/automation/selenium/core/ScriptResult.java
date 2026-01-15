package com.nordstrom.automation.selenium.core;

import java.util.Map;

import com.nordstrom.automation.selenium.exceptions.ScriptExecutionException;

/**
 * Represents the normalized result envelope returned
 * from JavaScript execution.
 */
public final class ScriptResult {

    private final boolean error;
    private final Object value;

    private final String exceptionClassName;
    private final String message;
    private final String stack;

    private ScriptResult(boolean error, Object value, String exceptionClassName, String message, String stack) {
        this.error = error;
        this.value = value;
        this.exceptionClassName = exceptionClassName;
        this.message = message;
        this.stack = stack;
    }

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
            throw ScriptExecutionException.malformedPayload("Missing or invalid exception object");
        }

        Map<String, Object> ex = (Map<String, Object>) exceptionObj;

        return new ScriptResult(true, null, string(ex.get("className")),
                string(ex.get("message")), string(ex.get("stack")));
    }

    private static String string(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    public boolean isError() {
        return error;
    }

    public Object getValue() {
        return value;
    }

    public String getExceptionClassName() {
        return exceptionClassName;
    }

    public String getMessage() {
        return message;
    }

    public String getStack() {
        return stack;
    }
}
