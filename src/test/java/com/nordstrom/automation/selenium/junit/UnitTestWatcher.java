package com.nordstrom.automation.selenium.junit;

import java.lang.reflect.Method;

public class UnitTestWatcher extends DriverWatcher {

    @Override
    public void beforeInvocation(Object obj, Method method, Object[] args) {
        System.out.println("beforeInvocation");
    }

    @Override
    public void afterInvocation(Object obj, Method method, Object[] args) {
        System.out.println("afterInvocation");
    }
    
}