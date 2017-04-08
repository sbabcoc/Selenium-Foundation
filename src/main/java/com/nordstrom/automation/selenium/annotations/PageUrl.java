package com.nordstrom.automation.selenium.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

@Retention(RUNTIME)
public @interface PageUrl {
	/** URL scheme (<b>http</b> or <b>https</b>) */
	String scheme() default "";
	
	/** URL user info as <b>user:password</b> */
	String userInfo() default "";
	
	/** URL host name */
	String host() default "";
	
	/** URL port number */
	String port() default "";
	
	/** URL path */
	String value() default "";
	
	/** array of URL query parameters as <b>name=value</b> */
	String[] params() default {};
	
	/** regular expression to validate path/parameters */
	String pattern() default "";
}
