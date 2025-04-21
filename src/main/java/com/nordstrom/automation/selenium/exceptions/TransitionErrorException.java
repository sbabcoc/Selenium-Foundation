package com.nordstrom.automation.selenium.exceptions;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;

import com.nordstrom.automation.selenium.interfaces.TransitionErrorDetector;
import com.nordstrom.automation.selenium.model.ComponentContainer;
import com.nordstrom.automation.selenium.model.ContainerMethodInterceptor;
import com.nordstrom.automation.selenium.model.Enhanceable;

/**
 * This exception is thrown by {@link ContainerMethodInterceptor#scanForErrors(SearchContext)} when a registered
 * {@link TransitionErrorDetector} service provider detects an error.
 */
public class TransitionErrorException extends IllegalStateException {

    /** exception error message */
    private final String errorMessage;
    private static final long serialVersionUID = -2969607575378647073L;

    /**
     * Constructor for {@code transition error} exception.
     * 
     * @param context container context in which the error was detected
     * @param errorMessage error message
     */
    public TransitionErrorException(ComponentContainer context, String errorMessage) {
        super(buildMessage(context, errorMessage));
        this.errorMessage = errorMessage;
    }
    
    /**
     * Get message for this transition error.
     * 
     * @return transition error message
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Build the message for this transition error exception.
     * 
     * @param context container context in which the error was detected
     * @param errorMessage error message
     * @return transition error exception message
     */
    private static String buildMessage(ComponentContainer context, String errorMessage) {
        StringBuilder builder = new StringBuilder("Transition error detected: ").append(errorMessage);
        builder.append("\nContainer: ").append(Enhanceable.getContainerClass(context).getName());
        WebDriver driver = context.getWrappedDriver();
        if (driver != null) {
            String pageUrl = driver.getCurrentUrl();
            if (pageUrl != null) {
                builder.append("\nPage URL: ").append(pageUrl);
            }
            String pageTitle = driver.getTitle();
            if (pageTitle != null) {
                builder.append("\nPage title: ").append(pageTitle);
            }
        }
        return builder.toString();
    }
}
