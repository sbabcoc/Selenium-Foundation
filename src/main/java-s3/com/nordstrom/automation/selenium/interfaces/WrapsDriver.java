package com.nordstrom.automation.selenium.interfaces;

import org.openqa.selenium.WebDriver;

public interface WrapsDriver extends org.openqa.selenium.WrapsDriver {
    
    static boolean isAssignableFrom(Object obj) {
        return (obj instanceof org.openqa.selenium.WrapsDriver);
    }
    
    static Class<org.openqa.selenium.WrapsDriver> getType() {
        return org.openqa.selenium.WrapsDriver.class;
    }
    
    static WebDriver getWrappedDriver(Object obj) {
        return ((org.openqa.selenium.WrapsDriver) obj).getWrappedDriver();
    }
    
}
