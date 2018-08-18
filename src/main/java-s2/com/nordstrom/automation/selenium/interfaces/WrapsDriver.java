package com.nordstrom.automation.selenium.interfaces;

import org.openqa.selenium.WebDriver;

public interface WrapsDriver extends org.openqa.selenium.internal.WrapsDriver {
    
    static boolean isAssignableFrom(Object obj) {
        return (obj instanceof org.openqa.selenium.internal.WrapsDriver);
    }
    
    static Class<org.openqa.selenium.internal.WrapsDriver> getType() {
        return org.openqa.selenium.internal.WrapsDriver.class;
    }
    
    static WebDriver getWrappedDriver(Object obj) {
        return ((org.openqa.selenium.internal.WrapsDriver) obj).getWrappedDriver();
    }
    
}
