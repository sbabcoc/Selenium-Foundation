package com.nordstrom.automation.selenium.platform;

import org.junit.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.nordstrom.automation.selenium.SeleniumConfig;

public class TargetPlatformRule<P extends Enum<?> & PlatformEnum> implements TestRule {

    private Object testObject;
    private P platform;
    
    public TargetPlatformRule(Object testObject) {
        this.testObject = testObject;
    }
    
    @Override
    public Statement apply(final Statement base, final Description description) {
        if (!description.isTest()) return base;

        final String contextPlatform = SeleniumConfig.getConfig().getContextPlatform();
        final TargetPlatform targetPlatform = description.getAnnotation(TargetPlatform.class);
        
        platform = TargetPlatformHandler.resolveTargetPlatform(testObject, targetPlatform);
        
        if (TargetPlatformHandler.shouldRun(contextPlatform, platform)) {
            return base;
        } else {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    String message = String.format("%s.%s() doesn't specify platform '%s'",
                            description.getClassName(), description.getMethodName(), contextPlatform);
                    throw new AssumptionViolatedException(message);
                }
            };
        }
    }
    
    public P getPlatform() {
        return platform;
    }
}
