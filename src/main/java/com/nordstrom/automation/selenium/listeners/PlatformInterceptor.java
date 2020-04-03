package com.nordstrom.automation.selenium.listeners;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;

import com.nordstrom.automation.selenium.AbstractSeleniumConfig.SeleniumSettings;
import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.platform.PlatformEnum;
import com.nordstrom.automation.selenium.platform.PlatformTargetable;
import com.nordstrom.automation.selenium.platform.TargetPlatform;
import com.nordstrom.automation.selenium.platform.TargetPlatformHandler;
import com.nordstrom.automation.selenium.utility.DataUtils;

public class PlatformInterceptor implements IMethodInterceptor {

    private static final String INTERCEPT = "Intercept";
    private static Logger logger = LoggerFactory.getLogger(PlatformInterceptor.class);

   /**
     * Assemble a list of methods that support the current target platform
     *
     * @param methods list of methods that are about the be run
     * @param context test context
     */
    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
        List<IMethodInstance> result = new ArrayList<>();
        String contextPlatform = getContextPlatform(context);

        // iterate over method list
        for (IMethodInstance thisMethod : methods) {
            PlatformEnum platformConstant = resolveTargetPlatform(thisMethod);
            
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
     * @param platformConstant target platform on which to run this method
     */
    private static <P extends Enum<?> & PlatformEnum> void addMethodForPlatform(List<IMethodInstance> methodList, IMethodInstance testMethod, PlatformEnum platformConstant) {
        if (platformConstant != null) {
            PlatformIdentity identity = new PlatformIdentity(platformConstant);
            testMethod.getMethod().setDescription(DataUtils.toString(identity));
        }
        methodList.add(testMethod);
    }
    
    /**
     * Resolve the target platform for the associated method.
     * 
     * @param testMethod test method object
     * @return target platform constant; 'null' if test class object is not {@link PlatformTargetable}
     */
    private static PlatformEnum resolveTargetPlatform(IMethodInstance testMethod) {
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

    public static class PlatformIdentity {
        private PlatformEnum platform;
        
        private PlatformIdentity(PlatformEnum platformConstant) {
            this.platform = platformConstant;
        }
        
        public PlatformEnum getPlatform() {
            return platform;
        }
        
    }

}
