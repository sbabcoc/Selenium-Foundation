package com.nordstrom.automation.selenium.junit;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;

/**
 * This enumeration implements the method interceptor for <b>JUnitBase</b> test class objects.
 * This interceptor is implemented as a standard Java enumeration singleton and performs the following tasks:
 * <ul>
 * </ul>
 */
public enum JUnitMethodInterceptor {
    INSTANCE;
    
    private Set<Class<?>> markedClasses = Collections.synchronizedSet(new HashSet<>());
    private Set<Class<? extends JUnitMethodWatcher>> watcherSet = 
                    Collections.synchronizedSet(new HashSet<>());
    
    private List<JUnitMethodWatcher> watchers = new ArrayList<>();
    
    /**
     * This is the method that intercepts annotated methods in "enhanced" JUnit test class objects.
     * 
     * @param obj "enhanced" object upon which the method was invoked
     * @param method {@link Method} object for the invoked method
     * @param args method invocation arguments
     * @param proxy call-able proxy for the intercepted method
     * @return {@code anything} (the result of invoking the intercepted method)
     * @throws Exception {@code anything} (exception thrown by the intercepted method)
     */
    @RuntimeType
    public Object intercept(@This Object obj, @Origin Method method, @AllArguments Object[] args,
                    @SuperCall Callable<?> proxy) throws Exception
    {
        Object result;
        
        synchronized(watchers) {
            for (JUnitMethodWatcher watcher : watchers) {
                watcher.beforeInvocation(obj, method, args);
            }
        }
        
        try {
            result = proxy.call();
        } finally {
            synchronized(watchers) {
                for (JUnitMethodWatcher watcher : watchers) {
                    watcher.afterInvocation(obj, method, args);
                }
            }
        }
        
        return result;
    }
    
    public Optional<JUnitMethodWatcher> getAttachedWatcher(Class<? extends JUnitMethodWatcher> watcherType) {
        Objects.requireNonNull(watcherType, "[watcherType] must be non-null");
        for (JUnitMethodWatcher watcher : watchers) {
            if (watcher.getClass() == watcherType) {
                return Optional.of(watcher);
            }
        }
        return Optional.empty();
    }
    
    /**
     * Attach watchers that are active on the specified test class.
     * 
     * @param testClass test class
     */
    void attachWatchers(Class<?> testClass) {
        JUnitMethodWatchers annotation = testClass.getAnnotation(JUnitMethodWatchers.class);
        if (null != annotation) {
            Class<?> markedClass = testClass;
            while (null == markedClass.getDeclaredAnnotation(JUnitMethodWatchers.class)) {
                markedClass = markedClass.getSuperclass();
            }
            if ( ! markedClasses.contains(markedClass)) {
                markedClasses.add(markedClass);
                for (Class<? extends JUnitMethodWatcher> watcher : annotation.value()) {
                    attachWatcher(watcher);
                }
            }
        }
    }
    
    /**
     * Wrap the current watcher chain with an instance of the specified watcher class.<br>
     * <b>NOTE</b>: The order in which watcher methods are invoked is determined by the
     * order in which watcher objects are added to the chain. Listener <i>before</i> methods
     * are invoked in last-added-first-called order. Listener <i>after</i> methods are invoked
     * in first-added-first-called order.<br>
     * <b>NOTE</b>: Only one instance of any given watcher class will be included in the chain.
     * 
     * @param watcher watcher class to add to the chain
     */
    private void attachWatcher(Class<? extends JUnitMethodWatcher> watcher) {
        if ( ! watcherSet.contains(watcher)) {
            watcherSet.add(watcher);
            try {
                JUnitMethodWatcher watcherObj = watcher.newInstance();
                
                synchronized(watchers) {
                    watchers.add(watcherObj);
                }
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Unable to instantiate watcher: " + watcher.getName(), e);
            }
        }
    }
}
