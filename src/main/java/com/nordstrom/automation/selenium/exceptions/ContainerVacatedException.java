package com.nordstrom.automation.selenium.exceptions;

import java.lang.reflect.Method;

import com.nordstrom.automation.selenium.utility.ReflectUtil;

public class ContainerVacatedException extends RuntimeException {

    private static final long serialVersionUID = 2043877560841903084L;
    
    private transient Method vacater;
    private static final String PREAMBLE = "Container object was vacated by invocation of method: ";
    
    /**
     * Constructs a new container vacated exception with the specified vacater.
     *
     * @param vacater method that caused the container to be vacated
     */
    public ContainerVacatedException(Method vacater) {
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
    private static String getMessage(Method method) {
        String className = method.getDeclaringClass().getSimpleName();
        String signature = ReflectUtil.getSignature(method);
        return PREAMBLE + className + ":" + signature;
    }
    
}
