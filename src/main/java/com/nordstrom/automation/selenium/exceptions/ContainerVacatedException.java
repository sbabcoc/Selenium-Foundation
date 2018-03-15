package com.nordstrom.automation.selenium.exceptions;

import java.lang.reflect.Method;

import com.nordstrom.automation.selenium.utility.ReflectUtil;

/**
 * This exception is thrown when a client calls a method of a container object that's no longer valid.
 */
public class ContainerVacatedException extends RuntimeException {

    private static final long serialVersionUID = 2043877560841903084L;
    
    private final transient Method vacater;
    private static final String PREAMBLE = "Container object was vacated by invocation of method: ";
    
    /**
     * Constructs a new {@code container vacated} exception with the specified vacater.
     *
     * @param vacater method that caused the container to be vacated
     */
    public ContainerVacatedException(final Method vacater) {
        super(getMessage(vacater));
        this.vacater = vacater;
    }
    
    /**
     * Get the method that caused the affected container object to be vacated.
     * 
     * @return method that vacated the target object
     */
    public Method getVacater() {
        return vacater;
    }
    
    /**
     * Assemble the message for this exception.
     * 
     * @param method method that vacated the target object.
     * @return message for this exception
     */
    private static String getMessage(final Method method) {
        String className = method.getDeclaringClass().getSimpleName();
        String signature = ReflectUtil.getSignature(method);
        return PREAMBLE + className + ":" + signature;
    }
    
}
