package com.nordstrom.automation.selenium.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nordstrom.common.base.UncheckedThrow;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import static net.bytebuddy.matcher.ElementMatchers.hasMethodName;
import static net.bytebuddy.matcher.ElementMatchers.is;
import static net.bytebuddy.matcher.ElementMatchers.not;

/**
 * This is the foundation for all "enhanceable" objects
 * 
 * @param <T> "enhanceable" object base class
 */
public abstract class Enhanceable<T> {
    
    private static final List<Class<?>> BYPASS = Arrays.asList(Enhanceable.class);
    
    /**
     * Get the types of the arguments used to instantiate this object.
     * 
     * @return an array of constructor argument types
     */
    abstract Class<?>[] getArgumentTypes();
    
    /**
     * Get the actual arguments used to instantiate this object.
     * 
     * @return an array of constructor arguments
     */
    abstract Object[]   getArguments();
    
    /**
     * Get the list of classes whose declared methods should not be intercepted
     * 
     * @return list of bypass classes
     */
    protected List<Class<?>> getBypassClasses() {
        return new ArrayList<>(BYPASS);
    }
    
    /**
     * Get the list of named for methods that should not be intercepted
     * 
     * @return list of bypass method names
     */
    protected List<String> getBypassMethods() {
        return new ArrayList<>();
    }
    
    /**
     * Create an enhanced instance of the specified container.
     * 
     * @param <C> container type
     * @param container container object to be enhanced
     * @return enhanced container object
     */
    @SuppressWarnings("unchecked")
    public <C extends T> C enhanceContainer(final C container) {
        if (container instanceof Enhanced) {
            return container;
        }
        
        Class<?> containerClass = container.getClass();
        
        Enhanceable<T> enhanceable = (Enhanceable<T>) container;
        Class<?>[] argumentTypes = enhanceable.getArgumentTypes();
        Object[] arguments = enhanceable.getArguments();
        List<Class<?>> bypassClasses = enhanceable.getBypassClasses();
        List<String> methodNames = enhanceable.getBypassMethods();
        
        ElementMatcher.Junction<MethodDescription> matcher = ElementMatchers.none();
        
        for (Class<?> bypassClass : bypassClasses) {
            for (Method method : bypassClass.getMethods()) {
                matcher = matcher.or(is(method));
            }
        }
        
        for (String methodName : methodNames) {
            matcher = matcher.or(hasMethodName(methodName));
        }
        
        try {
            
            Class<C> proxyType = (Class<C>) new ByteBuddy()
                    .subclass(containerClass)
                    .method(not(matcher))
                    .intercept(MethodDelegation.to(ContainerMethodInterceptor.INSTANCE))
                    .implement(Enhanced.class)
                    .make()
                    .load(containerClass.getClassLoader())
                    .getLoaded();
            
            return proxyType.getConstructor(argumentTypes).newInstance(arguments);
            
        } catch (InvocationTargetException e) { //NOSONAR
            throw UncheckedThrow.throwUnchecked(e.getCause());
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException
                        | NoSuchMethodException | InstantiationException e)
        {
            throw UncheckedThrow.throwUnchecked(e);
        }
    }
    
    /**
     * Get class of specified container object.
     * 
     * @param container container object
     * @return class of container object
     */
    public static Class<?> getContainerClass(final Object container) {
        Class<?> clazz = container.getClass();
        if (container instanceof Enhanced) {
            clazz = clazz.getSuperclass();
        }
        return clazz;
    }

}
