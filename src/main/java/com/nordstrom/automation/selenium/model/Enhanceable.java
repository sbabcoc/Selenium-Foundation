package com.nordstrom.automation.selenium.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nordstrom.automation.selenium.utility.UncheckedThrow;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import static net.bytebuddy.matcher.ElementMatchers.hasMethodName;
import static net.bytebuddy.matcher.ElementMatchers.is;
import static net.bytebuddy.matcher.ElementMatchers.not;

public abstract class Enhanceable<T> {
	
	private static final List<Class<?>> BYPASS = Arrays.asList(Enhanceable.class);
	
	abstract Class<?>[] getArgumentTypes();
	abstract Object[]   getArguments();
	
	List<Class<?>> getBypassClasses() {
		return new ArrayList<>(BYPASS);
	}
	
	List<String> getBypassMethods() {
		return new ArrayList<>();
	}
	
	/**
	 * Create an enhanced instance of the specified container.
	 * 
	 * @param <T> superclass type
	 * @param <C> container type
	 * @param container container object to be enhanced
	 * @return enhanced container object
	 */
	@SuppressWarnings("unchecked")
	<C extends T> C enhanceContainer(C container) {
		if (container instanceof DynamicType) return container;
		
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
					.make()
					.load(containerClass.getClassLoader())
					.getLoaded();
			
			return proxyType.getConstructor(argumentTypes).newInstance(arguments);
			
		} catch (InstantiationException e) {
			throw UncheckedThrow.throwUnchecked(e);
		} catch (IllegalAccessException e) {
			throw UncheckedThrow.throwUnchecked(e);
		} catch (IllegalArgumentException e) {
			throw UncheckedThrow.throwUnchecked(e);
		} catch (InvocationTargetException e) {
			throw UncheckedThrow.throwUnchecked(e);
		} catch (NoSuchMethodException e) {
			throw UncheckedThrow.throwUnchecked(e);
		} catch (SecurityException e) {
			throw UncheckedThrow.throwUnchecked(e);
		}
	}
	
}
