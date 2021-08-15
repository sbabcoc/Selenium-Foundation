package com.nordstrom.automation.selenium.model;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.nordstrom.automation.selenium.DriverPlugin;
import com.nordstrom.automation.selenium.interfaces.WrapsContext;
import com.nordstrom.common.base.UncheckedThrow;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.MethodNameEqualityResolver;
import net.bytebuddy.implementation.bind.annotation.BindingPriority;
import net.bytebuddy.implementation.bind.annotation.TargetMethodAnnotationDrivenBinder;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * This class contains the classes, methods, and interfaces used to wrap {@link WebElement} objects in a
 * reference-refreshing shell.
 */
public final class RobustElementFactory {
    
    private static final Map<String, InstanceCreator> creatorMap = new HashMap<>();
    
    /**
     * Private constructor to prevent instantiation.
     */
    private RobustElementFactory() {
        throw new AssertionError("RobustElementFactory is a static utility class that cannot be instantiated");
    }
    
    /**
     * Basic robust web element builder.
     * 
     * @param context element search context
     * @param locator element locator
     * @return robust web element
     */
    public static WebElement makeRobustElement(final WrapsContext context, final By locator) {
        return makeRobustElement(null, context, locator, RobustElementWrapper.CARDINAL);
    }
    
    /**
     * Builder for wrapping an existing element reference.
     * 
     * @param element element reference to be wrapped
     * @param context element search context
     * @param locator element locator
     * @return robust web element
     */
    public static WebElement makeRobustElement(
                    final WebElement element, final WrapsContext context, final By locator) {
        
        return makeRobustElement(element, context, locator, RobustElementWrapper.CARDINAL);
    }
    
    /**
     * Main robust web element builder.
     * 
     * @param element element reference to be wrapped (may be 'null')
     * @param context element search context
     * @param locator element locator
     * @param index element index
     * @return robust web element
     */
    public static WebElement makeRobustElement(
                    final WebElement element, final WrapsContext context, final By locator, final int index) {
        
        InstanceCreator creator = getCreator(context);
        RobustElementWrapper interceptor = new RobustElementWrapper(element, context, locator, index);
        WebElement robust = (WebElement) creator.makeInstance();
        ((InterceptionAccessor) robust).setInterceptor(interceptor);
        return robust;
    }
    
    /**
     * Get robust web element factory for this context.
     * 
     * @param context target context
     * @return robust web element factory
     */
    private static synchronized InstanceCreator getCreator(final WrapsContext context) {
        WebDriver driver = context.getWrappedDriver();
        String driverName = driver.getClass().getName();
        if (creatorMap.containsKey(driverName)) {
            return creatorMap.get(driverName);
        }
        
        WebElement reference = driver.findElement(By.cssSelector("*"));
        Class<? extends WebElement> refClass = reference.getClass();
        
        Builder<? extends WebElement> builder = new ByteBuddy()
                .subclass(refClass)
                .name(refClass.getPackage().getName() + ".Robust" + refClass.getSimpleName());

        for (DriverPlugin driverPlugin : ServiceLoader.load(DriverPlugin.class)) {
            Implementation ctorImpl = driverPlugin.getWebElementCtor(driver, refClass);
            if (ctorImpl != null) {
                builder = builder.defineConstructor(Visibility.PUBLIC).intercept(ctorImpl);
                break;
            }
        }
        
        Class<? extends WebElement> wrapperClass = builder
                .method(not(isDeclaredBy(Object.class)))
                .intercept(MethodDelegation.withEmptyConfiguration()
                        .withBinders(TargetMethodAnnotationDrivenBinder.ParameterBinder.DEFAULTS)
                        .withResolvers(MethodNameEqualityResolver.INSTANCE, BindingPriority.Resolver.INSTANCE)
                        .filter(not(isDeclaredBy(Object.class)))
                        .toField("interceptor"))
                .implement(RobustWebElement.class)
                .defineField("interceptor", RobustElementWrapper.class, Visibility.PRIVATE)
                .implement(InterceptionAccessor.class).intercept(FieldAccessor.ofBeanProperty())
                .make()
                .load(refClass.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();
        
        InstanceCreator creator;
        
        try {
            creator = new ByteBuddy()
                    .subclass(InstanceCreator.class)
                    .method(not(isDeclaredBy(Object.class)))
                    .intercept(MethodDelegation.toConstructor(wrapperClass))
                    .make()
                    .load(wrapperClass.getClassLoader())
                    .getLoaded().getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException 
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw UncheckedThrow.throwUnchecked(e);
        }
        
        creatorMap.put(driverName, creator);
        return creator;
    }
    
    /**
     * This interface defines accessor and mutator methods for element method interceptor.
     */
    public interface InterceptionAccessor {
        /**
         * Get the {@link RobustElementWrapper} interceptor.
         * 
         * @return RobustElementWrapper object
         */
        RobustElementWrapper getInterceptor();
        
        /**
         * Set the {@link RobustElementWrapper} interceptor.
         * 
         * @param interceptor RobustElementWrapper object
         */
        void setInterceptor(RobustElementWrapper interceptor);
    }
    
    /**
     * This interface defines the robust web element factory builder method.
     */
    public interface InstanceCreator {
        
        /**
         * Make a new robust web element instance.
         *  
         * @return robust web element
         */
        Object makeInstance();
    }

    /**
     * Get the list of elements that match the specified locator in the indicated context.
     * 
     * @param context element search context
     * @param locator element locator
     * @return list of robust elements in context that match the locator
     */
    public static List<WebElement> getElements(final WrapsContext context, final By locator) {
        List<WebElement> elements;
        try {
            elements = context.getWrappedContext().findElements(locator);
            for (int index = 0; index < elements.size(); index++) {
                elements.set(index, makeRobustElement(elements.get(index), context, locator, index));
            }
        } catch (StaleElementReferenceException e) { //NOSONAR
            elements = context.refreshContext(context.acquiredAt()).findElements(locator);
        }
        return elements;
    }

    /**
     * Get the first element that matches the specified locator in the indicated context.
     * 
     * @param context element search context
     * @param locator element locator
     * @return robust element in context that matches the locator
     */
    public static WebElement getElement(final WrapsContext context, final By locator) {
        return getElement(context, locator, RobustElementWrapper.CARDINAL);
    }

    /**
     * Get the item at the specified index in the list of elements matching the specified 
     * locator in the indicated context.
     * 
     * @param context element search context
     * @param locator element locator
     * @param index element index
     * @return indexed robust element in context that matches the locator
     */
    public static WebElement getElement(final WrapsContext context, final By locator, final int index) {
        return makeRobustElement(null, context, locator, index);
    }
}
