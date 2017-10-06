package com.nordstrom.automation.selenium.junit;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UnitTestWatcher implements JUnitMethodWatcher {

    private List<String> m_enterBeforeMethod = Collections.synchronizedList(new ArrayList<>());
    private List<String> m_enterTest = Collections.synchronizedList(new ArrayList<>());
    private List<String> m_enterAfterMethod = Collections.synchronizedList(new ArrayList<>());
    
    private List<String> m_leaveBeforeMethod = Collections.synchronizedList(new ArrayList<>());
    private List<String> m_leaveTest = Collections.synchronizedList(new ArrayList<>());
    private List<String> m_leaveAfterMethod = Collections.synchronizedList(new ArrayList<>());
    
    @Override
    public void beforeInvocation(Object obj, Method method, Object[] args) {
        if (null != method.getAnnotation(Before.class)) {
            m_enterBeforeMethod.add(method.getName());
        } else if (null != method.getAnnotation(Test.class)) {
            m_enterTest.add(method.getName());
        } else if (null != method.getAnnotation(After.class)) {
            m_enterAfterMethod.add(method.getName());
        }
    }

    @Override
    public void afterInvocation(Object obj, Method method, Object[] args) {
        if (null != method.getAnnotation(Before.class)) {
            m_leaveBeforeMethod.add(method.getName());
        } else if (null != method.getAnnotation(Test.class)) {
            m_leaveTest.add(method.getName());
        } else if (null != method.getAnnotation(After.class)) {
            m_leaveAfterMethod.add(method.getName());
        }
    }
    
    public List<String> getEnterBeforeMethod() {
        return m_enterBeforeMethod;
    }
    
    public List<String> getEnterTest() {
        return m_enterTest;
    }
    
    public List<String> getEnterAfterMethod() {
        return m_enterAfterMethod;
    }
    
    public List<String> getLeaveBeforeMethod() {
        return m_leaveBeforeMethod;
    }
    
    public List<String> getLeaveTest() {
        return m_leaveTest;
    }
    
    public List<String> getLeaveAfterMethod() {
        return m_leaveAfterMethod;
    }
    
}