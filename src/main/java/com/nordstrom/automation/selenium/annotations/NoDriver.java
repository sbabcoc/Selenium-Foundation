package com.nordstrom.automation.selenium.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation enables you to decline automatic driver instantiation for an individual test method.
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface NoDriver {

}
