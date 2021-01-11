package com.nordstrom.automation.selenium.junit;

import java.lang.reflect.Method;
import java.util.Map;

import com.nordstrom.automation.junit.AtomIdentity;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.openqa.selenium.WebDriver;
import com.google.common.base.Optional;
import com.nordstrom.automation.junit.ArtifactParams;
import com.nordstrom.automation.junit.RuleChainWalker;
import com.nordstrom.automation.selenium.core.TestBase;
import com.nordstrom.automation.selenium.model.Page;
import com.nordstrom.common.file.PathUtils;

/**
 * This abstract class implements the contract for Selenium Foundation test classes for JUnit.
 */
public abstract class JUnitBase extends TestBase implements ArtifactParams {
    
    /** This method rule manages driver lifetimes and opens initial pages. */
    @Rule
    public final RuleChain ruleChain = RuleChain
            .outerRule(new ScreenshotCapture(this))
            .around(new PageSourceCapture(this))
            .around(DriverWatcher.getTestWatcher(this));
    
    private WebDriver driver = null;
    private Page initialPage = null;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<WebDriver> nabDriver() {
        return TestBase.optionalOf(driver);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setDriver(final WebDriver driver) {
        this.driver = driver;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Page> Optional<T> nabInitialPage() {
        return (Optional<T>) TestBase.optionalOf(initialPage);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Page> void setInitialPage(final T pageObj) {
        initialPage = pageObj;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getOutputDirectory() {
        return PathUtils.ReportsDirectory.getPathForObject(this).toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTest(final Method method) {
        return null != method.getAnnotation(Test.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBeforeMethod(final Method method) {
        return null != method.getAnnotation(Before.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAfterMethod(final Method method) {
        return null != method.getAnnotation(After.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBeforeClass(final Method method) {
        return null != method.getAnnotation(BeforeClass.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAfterClass(final Method method) {
        return null != method.getAnnotation(AfterClass.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AtomIdentity getAtomIdentity() {
        return getLinkedRule(ScreenshotCapture.class).getAtomIdentity();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Description getDescription() {
        return getLinkedRule(ScreenshotCapture.class).getDescription();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Map<String, Object>> getParameters() {
        return Optional.absent();
    }

    /**
     * Get the test rule of the specified type that's attached to the rule chain.
     * 
     * @param <T> test rule type
     * @param testRuleType test rule type
     * @return {@link ScreenshotCapture} test rule
     */
    public <T extends TestRule> T getLinkedRule(final Class<T> testRuleType) {
        Optional<T> optional = RuleChainWalker.getAttachedRule(ruleChain, testRuleType);
        if (optional.isPresent()) {
            return optional.get();
        }
        throw new IllegalStateException(testRuleType.getSimpleName() + " test rule wasn't found on the rule chain");
    }
}
