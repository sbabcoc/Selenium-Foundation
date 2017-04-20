package com.nordstrom.automation.selenium.model;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nordstrom.automation.selenium.utility.UncheckedThrow;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatcher.Junction;
import net.bytebuddy.matcher.ElementMatchers;

import static net.bytebuddy.matcher.ElementMatchers.anyOf;
import static net.bytebuddy.matcher.ElementMatchers.isDeclaredBy;
import static net.bytebuddy.matcher.ElementMatchers.not;

public abstract class Enhanceable<T> {
	
	private static final List<Class<?>> BYPASS = Arrays.asList(Enhanceable.class);
	
	abstract Class<?>[] getArgumentTypes();
	abstract Object[]   getArguments();
//	abstract Callback[] getCallbacks();
	
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
		
		List<Junction<MethodDescription>> bypassMethods = new ArrayList<>();
		for (String methodName : methodNames) {
			bypassMethods.add(ElementMatchers.hasMethodName(methodName));
		}
		
		try {
			Class<C> proxyType = (Class<C>) new ByteBuddy()
					.subclass(containerClass, ConstructorStrategy.Default.IMITATE_SUPER_CLASS)
					.method(not(isDeclaredBy(anyOf(bypassClasses)).or(anyOf(bypassMethods))))
					.intercept(MethodDelegation.to(ContainerMethodInterceptor.INSTANCE))
					.make()
					.load(containerClass.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
					.getLoaded();
			
			
			
			.newInstance();
		} catch (InstantiationException e) {
			throw UncheckedThrow.throwUnchecked(e);
		} catch (IllegalAccessException e) {
			throw UncheckedThrow.throwUnchecked(e);
		}
	}
	
	/**
	 * Determine if the specified method is declared in a class that should be entirely bypassed.
	 * 
	 * @param method method in question
	 * @return 'true' if specified method is declared in bypassed class; otherwise 'false'
	 */
	boolean bypassClassOf(Method method) {
		for (Class<?> clazz : getBypassClasses()) {
			for (Method member : clazz.getMethods()) {
				if (member.getName().endsWith(method.getName())) {
					if (Arrays.equals(member.getGenericParameterTypes(), method.getParameterTypes())) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Determine if the specified method should not be intercepted.
	 * 
	 * @param method method in question
	 * @return 'true' if specified method should be bypassed; otherwise 'false'
	 */
	boolean bypassMethod(Method method) {
		return getBypassMethods().contains(method.getName());
	}
	
}
