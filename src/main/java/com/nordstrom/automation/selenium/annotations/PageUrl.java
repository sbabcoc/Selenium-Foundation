package com.nordstrom.automation.selenium.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.model.ComponentContainer;

/**
 * This annotation can be applied to page class declarations to enable landing page verification and direct navigation.
 * <p>
 * <b>LANDING PAGE VERIFICATION</b>
 * <p>
 * The {@link ComponentContainer#verifyLandingPage} method is used to verify actual landing page against elements
 * of the {@link PageUrl} annotation of the specified page object.
 * <p>
 * <b>NOTES</b>: <ul>
 *     <li>The values and patterns used to verify the actual landing page URL are provided by the {@link PageUrl}
 *         annotation of the specified page object combined with the configured {@link SeleniumConfig#getTargetUri
 *         target URI}.</li>
 *     <li>Expected path can be specified by either explicit value or pattern. If the {@code pattern} element of
 *         the {@link PageUrl} annotation is specified, its value provides a template to verify the actual path.
 *         Otherwise, the actual path must match the path component of the specified {@code value} element of the
 *         {@link PageUrl} annotation.</li>
 *     <li>Expected parameters can be specified by either explicit query or a collection of name/pattern pairs.
 *         If the {@code params} element of the {@link PageUrl} annotation is specified, its value provides the
 *         collection of name/pattern pairs used to verify the actual parameters. Otherwise, the actual query
 *         parameters must include all of the name/value pairs in the query component of the specified {@code
 *         value} element of the {@link PageUrl} annotation.</li>
 * </ul>
 * <p>
 * <b>DIRECT PAGE NAVIGATION</b>
 * <p>
 * The core of the implementation for direct page navigation uses {@link ComponentContainer#getPageUrl} to get
 * the URL defined by the specified {@link PageUrl} annotation. 
 * <p>
 * <b>NOTES</b>: <ul>
 *     <li>If the {@code pageUrl} argument is {@code null} or the {@code value} element of the specified
 *         {@link PageUrl} annotation is unspecified, this method returns {@code null}.
 *     <li>If {@code scheme} of the specified {@code pageUrl} argument is unspecified or set to {@code http/https},
 *         the specified {@code targetUri} is overlaid by the elements of the {@link PageUrl} annotation to
 *         produce the fully-qualified <b>HTTP</b> target page URL.</li>
 *     <li>For <b>HTTP</b> URLs that require query parameters, these parameters must be included in the
 *         {@code value} element of the specified {@link PageUrl} annotation. The {@code params} element of the
 *         annotation is only used for pattern-based landing page verification.</li>
 *     <li>If {@code scheme} of the specified {@code pageUrl} is set to {@code file}, the value of the
 *         {@code targetUri} argument is ignored. The only element of the {@link PageUrl} annotation that
 *         is used to produce the fully-qualified <b>FILE</b> target page URL is {@code value}. The value of the
 *         {@code value} element specifies the relative path of a file within your project's resources, which is
 *         resolved via {@link ClassLoader#getResource}.</li>
 * </ul>
 */
@Retention(RUNTIME)
@Target({TYPE})
public @interface PageUrl {
    /**
     * Get the page URL scheme.
     * 
     * @return URL scheme (<b>http</b>, <b>https</b>, or <b>file</b>)
     */
    String scheme() default "{}";
    
    /**
     * Get the page URL user info.
     * 
     * @return URL user info as <b>user:password</b>
     */
    String userInfo() default "{}";
    
    /**
     * Get the page URL host name.
     * 
     * @return URL host name
     */
    String host() default "{}";
    
    /**
     * Get the page URL port number.
     * 
     * @return URL port number
     */
    String port() default "{}";
    
    /**
     * Get the page URL path.
     * 
     * @return URL path
     */
    String value() default "{}";
    
    /**
     * Get the page URL query parameters.
     * 
     * @return array of URL query parameters as <b>name=value</b>
     */
    String[] params() default {};
    
    /**
     * Get the regular expression to validate path/parameters.
     * 
     * @return regular expression to validate path/parameters
     */
    String pattern() default "{}";
}
