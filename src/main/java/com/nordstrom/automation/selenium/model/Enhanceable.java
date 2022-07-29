package com.nordstrom.automation.selenium.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.nordstrom.common.base.UncheckedThrow;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import static net.bytebuddy.matcher.ElementMatchers.hasMethodName;
import static net.bytebuddy.matcher.ElementMatchers.hasSignature;
import static net.bytebuddy.matcher.ElementMatchers.is;
import static net.bytebuddy.matcher.ElementMatchers.not;

/**
 * This is the foundation for all "enhanceable" objects
 * 
 * @param <T> "enhanceable" object base class
 */
public abstract class Enhanceable<T> {
    
    private static final List<Class<?>> BYPASS_CLASSES = Arrays.<Class<?>>asList(Enhanceable.class);
    private static final Map<Class<?>, Class<?>> proxyMap = new HashMap<>();
    
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
        return new ArrayList<>(BYPASS_CLASSES);
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
        
        Class<C> proxyType;
        
        synchronized(Enhanceable.class) {
            if (proxyMap.containsKey(containerClass)) {
                proxyType = (Class<C>) proxyMap.get(containerClass);
            } else {
                List<Class<?>> bypassClasses = enhanceable.getBypassClasses();
                List<String> methodNames = enhanceable.getBypassMethods();
                
                ElementMatcher.Junction<MethodDescription> matcher = ElementMatchers.isDeclaredBy(Object.class);
                
                // iterate over bypass classes/interfaces
                for (Class<?> bypassClass : bypassClasses) {
                    // if bypassing an interface
                    if (bypassClass.isInterface()) {
                        // iterate over interface methods
                        for (Method method : bypassClass.getMethods()) {
                            // match this method's signature
                            matcher = matcher.or(
                                    hasSignature(new MethodDescription.ForLoadedMethod(method)
                                            .asSignatureToken()));
                        }
                    // otherwise (bypassing a class)
                    } else {
                        // iterate over class methods
                        for (Method method : bypassClass.getMethods()) {
                            // match this method exactly
                            matcher = matcher.or(is(method));
                        }
                    }
                }
                
                for (String methodName : methodNames) {
                    matcher = matcher.or(hasMethodName(methodName));
                }
                
                // get 8-digit hexadecimal hash code for the fully-qualified class name
                String hashCode = String.format("%08X", containerClass.getName().hashCode());
                
                proxyType = (Class<C>) new ByteBuddy()
                                .subclass(containerClass)
                                .name(containerClass.getName() + "$$FoundationSynergy$$" + hashCode)
                                .method(not(matcher))
                                .intercept(MethodDelegation.to(ContainerMethodInterceptor.INSTANCE))
                                .implement(Enhanced.class)
                                .make()
                                .load(containerClass.getClassLoader(), getClassLoadingStrategy(containerClass))
                                .getLoaded();
                
                proxyMap.put(containerClass, proxyType);
            }
        }
        
        try {
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
    
    /**
     * Determine <b>Byte Buddy</b> class loading strategy.
     * 
     * @param targetClass target class
     * @return <b>Byte Buddy</b> {@link ClassLoadingStrategy}
     */
    public static ClassLoadingStrategy<ClassLoader> getClassLoadingStrategy(Class<?> targetClass) {
        ClassLoadingStrategy<ClassLoader> strategy;
        if (ClassInjector.UsingLookup.isAvailable()) {
            try {
                Class<?> methodHandles = Class.forName("java.lang.invoke.MethodHandles");
                Object lookup = methodHandles.getMethod("lookup").invoke(null);
                Method privateLookupIn = methodHandles.getMethod("privateLookupIn", Class.class,
                            Class.forName("java.lang.invoke.MethodHandles$Lookup"));
                Object privateLookup = privateLookupIn.invoke(null, targetClass, lookup);
                strategy = ClassLoadingStrategy.UsingLookup.of(privateLookup);
            } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                throw new IllegalStateException("Failed to determine class loading strategy", e);
            }
        } else if (ClassInjector.UsingReflection.isAvailable()) {
            strategy = ClassLoadingStrategy.Default.INJECTION;
        } else {
            throw new IllegalStateException("No code generation strategy available");
        }
        return strategy;
    }

}
