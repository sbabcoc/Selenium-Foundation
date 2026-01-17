package com.nordstrom.automation.selenium.interfaces;

public interface ExceptionPolicy {
    boolean isAllowed(Class<?> exceptionType);
}
