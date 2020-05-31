package com.nordstrom.automation.selenium.utility;

import static net.bytebuddy.matcher.ElementMatchers.*;
import static  org.openqa.selenium.remote.BrowserType.SAFARI;
import static org.openqa.selenium.remote.CapabilityType.BROWSER_NAME;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.openqa.grid.internal.utils.DefaultCapabilityMatcher;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;

/**
 * This capability matcher is functionally equivalent to {@link DefaultCapabilityMatcher}, implemented to avoid direct
 * references to the {@code SafariOptions} class. This avoids the need to include the path to the <b>safari-driver</b>
 * JAR on the class path provided to the Selenium Grid hub process.
 */
public class RevisedCapabilityMatcher extends DefaultCapabilityMatcher {

    private static final String SAFARI_SPECIFIC_VALIDATOR = "org.openqa.grid.internal.utils.DefaultCapabilityMatcher$SafariSpecificValidator";
    private static final String REVISED_SAFARI_VALIDATOR = "org.openqa.grid.internal.utils.DefaultCapabilityMatcher$RevisedSafariValidator";
    
    private static Class<?> safariValidator;
    
    /**
     * This constructor replaces the {@code SafariSpecificValidator} instance in the <b>validators</b> list of the
     * {@link DefaultCapabilityMatcher} with an instance of a dynamically-generated {@code RevisedSafariValidator}
     * class. This dynamic validator is functionally equivalent, but is implemented without explicit references to
     * the {@code SafariOptions} class.
     */
    @SuppressWarnings("unchecked")
    public RevisedCapabilityMatcher() {
        super();
        Field field = FieldUtils.getField(DefaultCapabilityMatcher.class, "validators", true);
        if (field != null) {
            field.setAccessible(true);
            try {
                List<Object> list = (List<Object>) field.get(this);
                Iterator<Object> iter = list.iterator();
                while (iter.hasNext()) {
                    Object item = iter.next();
                    Class<?> clazz = item.getClass();
                    if (SAFARI_SPECIFIC_VALIDATOR.equals(clazz.getName())) {
                        Object validator = newSafariValidator(clazz);
                        iter.remove();
                        list.add(validator);
                        break;
                    }
                }
            } catch (IllegalArgumentException | IllegalAccessException | ClassCastException | InstantiationException e) {
                // just eat the exception
            }
        }
    }
    
    /**
     * Create a new instance of the dynamically-generated {@code RevisedSafariValidator} class.
     * 
     * @param validatorClass {@code SafariSpecificValidator} class
     * @return instance of dynamically-generated replacement class
     * @throws InstantiationException if this class represents an abstract class, an interface, an array class, a
     *         primitive type, or {@code void}; if the class lacks a no-argument constructor; or if instantiation
     *         fails for some other reason.
     * @throws IllegalAccessException if the class or its no-argument constructor are inaccessible.
     */
    private static Object newSafariValidator(Class<?> validatorClass) throws InstantiationException, IllegalAccessException {
        if (safariValidator == null) {
            safariValidator = subclassSafariValidator(validatorClass);
        }
        return safariValidator.newInstance();
    }
    
    /**
     * Dynamically generate a replacement for the {@code SafariSpecificValidator} class.
     * 
     * @param validatorClass {@code SafariSpecificValidator} class
     * @return dynamically-generated {@code RevisedSafariValidator} class
     */
    private static Class<?> subclassSafariValidator(Class<?> validatorClass) {
        Class<?> validatorIntfc = validatorClass.getInterfaces()[0];
        return new ByteBuddy()
            .subclass(validatorIntfc)
            .name(REVISED_SAFARI_VALIDATOR)
            .method(named("apply"))
            .intercept(MethodDelegation.to(SafariValidator.class))
            .make()
            .load(validatorClass.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
            .getLoaded();
    }
    
    /**
     * This class implements to {@code apply()} method declared by the {@code Validator} interface. It also implements
     * methods to extract Safari-specific settings from specified capabilities maps.
     */
    public static class SafariValidator {
        static final String SAFARI_TECH_PREVIEW = "Safari Technology Preview";
        static final String AUTOMATIC_INSPECTION  = "safari:automaticInspection";
        static final String AUTOMATIC_PROFILING = "safari:automaticProfiling";
        static final String TECHNOLOGY_PREVIEW = "technologyPreview";

        public static Boolean apply(Map<String, Object> providedCapabilities, Map<String, Object> requestedCapabilities) {
            if (!SAFARI.equals(getBrowserName(requestedCapabilities)) && 
                !SAFARI_TECH_PREVIEW.equals(getBrowserName(requestedCapabilities))) {
                return true;
            }
            
            return getAutomaticInspection(requestedCapabilities) == getAutomaticInspection(providedCapabilities) &&
                   getAutomaticProfiling(requestedCapabilities) == getAutomaticProfiling(providedCapabilities) &&
                   getUseTechnologyPreview(requestedCapabilities) == getUseTechnologyPreview(providedCapabilities);
        }
        
        public static String getBrowserName(Map<String, Object> capabilities) {
            return (String) capabilities.get(BROWSER_NAME);
        }

        public static boolean getAutomaticInspection(Map<String, Object> capabilities) {
            return Boolean.TRUE.equals(capabilities.get(AUTOMATIC_INSPECTION));
        }

        public static boolean getAutomaticProfiling(Map<String, Object> capabilities) {
            return Boolean.TRUE.equals(capabilities.get(AUTOMATIC_PROFILING));
        }

        public static boolean getUseTechnologyPreview(Map<String, Object> capabilities) {
            return SAFARI_TECH_PREVIEW.equals(getBrowserName(capabilities)) ||
                   Boolean.TRUE.equals(capabilities.get(TECHNOLOGY_PREVIEW));
        }
    }
}
