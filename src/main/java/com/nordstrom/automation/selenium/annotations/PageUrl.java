package com.nordstrom.automation.selenium.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

@Retention(RUNTIME)
public @interface PageUrl {
	/** @return URL scheme (<b>http</b> or <b>https</b>) */
	String scheme() default "{}";
	
	/** @return URL user info as <b>user:password</b> */
	String userInfo() default "{}";
	
	/** @return URL host name */
	String host() default "{}";
	
	/** @return URL port number */
	String port() default "{}";
	
	/** @return URL path */
	String value() default "{}";
	
	/** @return array of URL query parameters as <b>name=value</b> */
	String[] params() default {};
	
	/** @return regular expression to validate path/parameters */
	String pattern() default "{}";
}
