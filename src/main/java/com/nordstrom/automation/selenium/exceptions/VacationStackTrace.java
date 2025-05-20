package com.nordstrom.automation.selenium.exceptions;

import java.lang.reflect.Method;

import com.nordstrom.automation.selenium.utility.ReflectUtils;
import com.nordstrom.common.base.StackTrace;

/**
 * This exception is used to record the execution stack trace for the point at which the
 * associated container became invalid.
 * 
 * @see ContainerVacatedException
 */
public class VacationStackTrace extends StackTrace {

    private static final long serialVersionUID = -512001372372827847L;
    
    private final transient Method vacater;
    private final transient String reason;
    private static final String PREAMBLE = "Container object was vacated by invocation of method: ";
    
    /**
     * Constructs a new {@code container vacated} exception with the specified vacater.
     *
     * @param vacater method that caused the container to be vacated
     */
    public VacationStackTrace(final Method vacater) {
        this(vacater, null);
    }
    
    /**
     * Constructs a new {@code container vacated} exception with the specified vacater.
     * 
     * @param vacater method that caused the container to be vacated
     * @param reason for vacating the target object
     */
    public VacationStackTrace(final Method vacater, final String reason) {
        super(getMessage(vacater, reason));
        this.vacater = vacater;
        this.reason = reason;
    }
    
    /**
     * Get the reason that the affected container object to be vacated.
     * 
     * @return reason for vacating the target object
     */
    public String getReason() {
        return reason;
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
     * @param reason for vacating the target object
     * @return message for this exception
     */
    private static String getMessage(final Method method, final String reason) {
        String className = method.getDeclaringClass().getSimpleName();
        String signature = ReflectUtils.getSignature(method);
        String suffix = (reason != null) ? "\n" + reason : "";
        return PREAMBLE + className + ":" + signature + suffix;
    }
    
}
