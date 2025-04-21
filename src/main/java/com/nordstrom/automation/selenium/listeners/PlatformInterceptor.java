package com.nordstrom.automation.selenium.listeners;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

import com.nordstrom.automation.selenium.AbstractSeleniumConfig.SeleniumSettings;
import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.platform.PlatformEnum;
import com.nordstrom.automation.selenium.platform.PlatformTargetable;
import com.nordstrom.automation.selenium.platform.TargetPlatform;
import com.nordstrom.automation.selenium.platform.TargetPlatformHandler;
import com.nordstrom.automation.selenium.utility.DataUtils;
import com.nordstrom.common.base.UncheckedThrow;

/**
 * This class implements the <b>TestNG</b> {@link IMethodInterceptor} interface to assemble a list of methods
 * that support the current target platform.
 */
public class PlatformInterceptor implements IMethodInterceptor {

    private static final String INTERCEPT = "Intercept";
    private static Logger logger = LoggerFactory.getLogger(PlatformInterceptor.class);

   /**
     * Assemble a list of methods that support the current target platform
     *
     * @param methods list of methods that are about to be run
     * @param context test context
     */
    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
        List<IMethodInstance> result = new ArrayList<>();
        String contextPlatform = getContextPlatform(context);

        // iterate over method list
        for (IMethodInstance thisMethod : methods) {
            PlatformEnum platformConstant = (PlatformEnum) resolveTargetPlatform(thisMethod);
            
            // if this method supports the current target platform
            if (TargetPlatformHandler.shouldRun(contextPlatform, platformConstant)) {
                addMethodForPlatform(result, thisMethod, platformConstant);
            }
        }

        if (result.isEmpty()) {
            logger.warn("No tests were found for context platform '{}'", contextPlatform);
        }

        // indicate intercept has been invoked
        context.setAttribute(INTERCEPT, Boolean.TRUE);
        return result;
    }

    /**
     * Get the target platform for the specified test context
     * <p>
     * <b>NOTE</b>: If the context name doesn't match a standard target platform name, the CONTEXT_PLATFORM setting is
     * used. If the CONTEXT_PLATFORM setting doesn't match a standard target platform name, this method returns 'null'
     * as the platform.
     *
     * @param context test context
     * @return target platform for the specified context
     */
    private static String getContextPlatform(ITestContext context) {
        String platform = context.getCurrentXmlTest().getParameter(SeleniumSettings.CONTEXT_PLATFORM.key());
        if (platform == null) {
            platform = SeleniumConfig.getConfig().getContextPlatform();
        }
        return platform;
    }
    
    /**
     * Add the specified method to the method list
     *
     * @param methodList list of methods that are about to be run
     * @param testMethod method to be added to the list
     * @param  platformConstant target platform on which to run this method
     */
    @SuppressWarnings("unchecked")
    private static <P extends Enum<?> & PlatformEnum> void addMethodForPlatform(List<IMethodInstance> methodList, IMethodInstance testMethod, PlatformEnum platformConstant) {
        if (platformConstant != null) {
            ITestNGMethod method = testMethod.getMethod();
            PlatformIdentity<P> identity = new PlatformIdentity<>((P) platformConstant, method.getDescription());
            method.setDescription(DataUtils.toString(identity));
        }
        methodList.add(testMethod);
    }
    
    /**
     * Resolve the target platform for the associated method.
     * 
     * @param <P> target platform class (enumeration)
     * @param testMethod test method object
     * @return target platform constant; 'null' if test class object is not {@link PlatformTargetable}
     */
    private static <P extends Enum<?> & PlatformEnum> P resolveTargetPlatform(IMethodInstance testMethod) {
        Object testObject = testMethod.getInstance();
        TargetPlatform targetPlatform = getTargetPlatform(testMethod);
        return TargetPlatformHandler.resolveTargetPlatform(testObject, targetPlatform);
    }

    /**
     * Get the target platform annotation for the specified method
     *
     * @param testMethod method for which annotation is to be retrieved
     * @return {@link TargetPlatform} annotation; 'null' if absent
     */
    private static TargetPlatform getTargetPlatform(IMethodInstance testMethod) {
        Method realMethod = testMethod.getMethod().getConstructorOrMethod().getMethod();
        return realMethod.getAnnotation(TargetPlatform.class);
    }

    /**
     * This class implements a "platform identity" object, which is used to attach a specified platform
     * value to a test method.
     * 
     * @param <P> target platform class (enumeration)
     */
    public static class PlatformIdentity<P extends Enum<?> & PlatformEnum> implements Serializable {
        
        private static final long serialVersionUID = 3048495330930703188L;
        
        /** platform constant name */
        private String constName;
        /** platform class name */
        private String className;
        /** test method description */
        private String description;
        
        /**
         * No-argument constructor for platform identity objects.
         * <p>
         * <b>NOTE</b>: This constructor is required by the <b>JavaBeans</b> de-serialization functionality
         * of the {@link org.openqa.selenium.json.Json Json} API.
         */
        public PlatformIdentity() { }
        
        /**
         * Constructor for platform identity object with the specified constant and description.
         * 
         * @param platformConst platform constant
         * @param description test method description
         */
        private PlatformIdentity(P platformConst, String description) {
            setConstName(platformConst.name());
            setClassName(platformConst.getClass().getName());
            setDescription(description);
        }
        
        /**
         * Set the constant name of this platform identity.
         * 
         * @param constName platform identity constant name
         */
        public void setConstName(String constName) {
            this.constName = constName;
        }
        
        /**
         * Get the constant name of this platform identity.
         * 
         * @return platform identity constant name
         */
        public String getConstName() {
            return constName;
        }
        
        /**
         * Set the name of the class (enumeration) that defines the constant assigned to this platform identity.
         *  
         * @param className platform constant class name
         */
        public void setClassName(String className) {
            this.className = className;
        }
        
        /**
         * Get the name of the class (enumeration) that defines the constant assigned to this platform identity.
         *  
         * @return platform constant class name
         */
        public String getClassName() {
            return className;
        }
        
        /**
         * Set the test method description for this platform identity.
         * 
         * @param description platform identity test method description
         */
        public void setDescription(String description) {
            this.description = description;
        }
        
        /**
         * Get the test method description for this platform identity.
         * 
         * @return platform identity test method description
         */
        public String getDescription() {
            return description;
        }
        
        /**
         * De-serialize the platform constant of this platform identity.
         * 
         * @return platform constant
         */
        @SuppressWarnings("unchecked")
        public P deserialize() {
            return (P) valueOf(classForName(className), constName);
        }
        
        /**
         * Get the target platform class (enumeration) for the specified name.
         * 
         * @param <P> target platform class (enumeration)
         * @param className name of target platform class (enumeration)
         * @return target platform class (enumeration)
         */
        @SuppressWarnings("unchecked")
        public static <P extends Enum<?> & PlatformEnum> Class<P> classForName(String className) {
            try {
                return (Class<P>) Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw UncheckedThrow.throwUnchecked(e);
            }
        }
        
        /**
         * Get the value of the specified platform constant name from the indicated class (enumeration).
         * 
         * @param <P> target platform class (enumeration)
         * @param platformClass target platform class (enumeration)
         * @param constName name of platform constant
         * @return target platform constant
         */
        @SuppressWarnings("unchecked")
        public static <P extends Enum<?> & PlatformEnum> P valueOf(Class<P> platformClass, String constName) {
            try {
                Method valueOf = platformClass.getMethod("valueOf", String.class);
                return (P) valueOf.invoke(null, constName);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException
                            | IllegalArgumentException | InvocationTargetException e) {
                throw UncheckedThrow.throwUnchecked(e);
            }
        }
    }

}
