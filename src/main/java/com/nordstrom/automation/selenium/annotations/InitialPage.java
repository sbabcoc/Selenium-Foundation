package com.nordstrom.automation.selenium.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.nordstrom.automation.selenium.model.Page;

/**
 * This annotation enables you to specify an initial page that should be loaded after instantiating the driver, on
 * either individual test methods or for an entire test class. Note that any page class specified as an initial page
 * must declare its associated URL via the {@link PageUrl} annotation.
 */
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface InitialPage {
    /**
     * Get the class of the initial page.
     *  
     * @return initial page class
     */
    Class<? extends Page> value() default Page.class;
    
    /**
     * Get the URL of the initial page.
     * 
     * @return initial page URL
     */
    PageUrl pageUrl() default @PageUrl();
}
