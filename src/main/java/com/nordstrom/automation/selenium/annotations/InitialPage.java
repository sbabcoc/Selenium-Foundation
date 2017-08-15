package com.nordstrom.automation.selenium.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.nordstrom.automation.selenium.model.Page;

@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface InitialPage {
    Class<? extends Page> value() default Page.class;
    PageUrl pageUrl() default @PageUrl();
}
