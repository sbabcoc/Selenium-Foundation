package com.nordstrom.automation.selenium.exceptions;

public final class JavaScriptStackTrace extends RuntimeException {

    private static final long serialVersionUID = 4823563816505831048L;

    public JavaScriptStackTrace(String stack) {
        super("JavaScript stack trace:\n" + stack);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
