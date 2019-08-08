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

    private final String errorMessage;
    private static final long serialVersionUID = -2969607575378647073L;

    public TransitionErrorException(ComponentContainer container, String errorMessage) {
        super(buildMessage(container, errorMessage));
        this.errorMessage = errorMessage;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }

    private static String buildMessage(ComponentContainer container, String message) {
        StringBuilder builder = new StringBuilder("Transition error detected: ").append(message);
        builder.append("\nContainer: ").append(Enhanceable.getContainerClass(container).getName());
        WebDriver driver = container.getWrappedDriver();
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
