package com.nordstrom.automation.selenium.junit;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation is assigned to test classes and enables you to attach one or more method watcher class,
 * which implement the {@link JUnitMethodWatcher} interface. To activate this feature, run with the {@link
 * HookInstallingRunner}.
 */
@Retention(RUNTIME)
@Target({TYPE})
@Inherited
public @interface JUnitMethodWatchers {
    /**
     * Get specified method watcher(s).
     * 
     * @return array of method watcher classes
     */
    Class<? extends JUnitMethodWatcher>[] value();
}
