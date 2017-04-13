package com.nordstrom.automation.selenium.exceptions;

import java.lang.reflect.Method;

import com.nordstrom.automation.selenium.utility.ReflectUtil;

public class ContainerVacatedException extends RuntimeException {

	private static final long serialVersionUID = 2043877560841903084L;
	
	private Method vacater;
	private static final String PREAMBLE = "Container object was vacated by invocation of method: ";
	
    /**
     * Constructs a new container vacated exception with the specified detail message.
     *
     * @param   vacater   
     */
    public ContainerVacatedException(Method vacater) {
        super(getMessage(vacater));
        this.vacater = vacater;
    }
    
    public Method getVacater() {
    	return vacater;
    }
    
    private static String getMessage(Method method) {
    	String className = method.getDeclaringClass().getSimpleName();
    	String signature = ReflectUtil.getSignature(method);
    	return PREAMBLE + className + ":" + signature;
    }
    
}
