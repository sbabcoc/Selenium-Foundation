package com.nordstrom.automation.selenium.interfaces;

import org.openqa.selenium.WebDriver;

import com.google.common.base.Function;

public interface WrapsDriver extends org.openqa.selenium.internal.WrapsDriver {
    
    static final Class<org.openqa.selenium.internal.WrapsDriver> TYPE = org.openqa.selenium.internal.WrapsDriver.class;
    
    static final Function<Object, Boolean> isAssignableFrom = new Function<Object, Boolean>() {
        @Override
        public Boolean apply(Object input) {
            return (input instanceof org.openqa.selenium.internal.WrapsDriver);
        }
    };
    
    static final Function<Object, WebDriver> getWrappedDriver = new Function<Object, WebDriver>() {
        @Override
        public WebDriver apply(Object input) {
            return ((org.openqa.selenium.internal.WrapsDriver) input).getWrappedDriver();
        }
    };
    
}
