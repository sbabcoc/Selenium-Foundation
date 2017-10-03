package com.nordstrom.automation.selenium.listeners;

import java.lang.reflect.Method;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.Reporter;

import com.nordstrom.automation.selenium.annotations.PageUrl;
import com.nordstrom.automation.selenium.core.DriverManager;
import com.nordstrom.automation.selenium.core.GridUtility;

/**
 * This TestNG listener performs several basic functions related to driver session management:
 * <ul>
 *     <li>Manage Selenium driver lifetime.</li>
 *     <li>For local execution, manage a local instance of Selenium Grid.</li>
 *     <li>Store and dispense the driver instance created for the test.</li>
 *     <li>Manage configured driver timeout intervals.</li>
 *     <li>If an initial page class is specified:
 *         <ul>
 *             <li>Open the initial page based on its {@link PageUrl} annotation.</li>
 *             <li>Store the page object for subsequent dispensing to the test.</li>
 *         </ul>
 *     </li>
 * </ul>
 * 
 * @see GridUtility
 */
public class DriverListener implements IInvokedMethodListener, ITestListener {

    /**
     * Perform pre-invocation processing:
     * <ul>
     *     <li>Ensure that a driver instance has been created for the test.</li>
     *     <li>Store the driver instance for subsequent dispensing.</li>
     *     <li>Manage configured driver timeout intervals.</li>
     *     <li>If specified, open the initial page, storing the page object for subsequent dispensing.</li>
     * </ul>
     * 
     * @param invokedMethod an object representing the method that's about to be invoked
     * @param testResult test result object for the method that's about to be invoked
     */
    @Override
    public void beforeInvocation(IInvokedMethod invokedMethod, ITestResult testResult) {
        // ensure current test result is set
        Reporter.setCurrentTestResult(testResult);
        
        Object obj = testResult.getInstance();
        Method method = invokedMethod.getTestMethod().getConstructorOrMethod().getMethod();
        
        DriverManager.beforeInvocation(obj, method);
    }
    
    /**
     * Perform post-invocation processing:
     * <ul>
     *     <li>If indicated, close the driver that was acquired for this method.</li>
     * </ul>
     * 
     * @param invokedMethod an object representing the method that's just been invoked
     * @param testResult test result object for the method that's just been invoked
     */
    @Override
    public void afterInvocation(IInvokedMethod invokedMethod, ITestResult testResult) {
        // ensure current test result is set
        Reporter.setCurrentTestResult(testResult);
        
        Object obj = testResult.getInstance();
        Method method = invokedMethod.getTestMethod().getConstructorOrMethod().getMethod();
        
        DriverManager.afterInvocation(obj, method);
    }
    
    /**
     * Perform post-suite processing:
     * <ul>
     *     <li>If a Selenium Grid node process was spawned, shut it down.</li>
     *     <li>If a Selenium Grid hub process was spawned, shut it down.</li>
     * </ul>
     * 
     * @param testContext execution context for the test suite that just finished
     */
    @Override
    public void onFinish(ITestContext testContext) {
        DriverManager.onFinish();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onStart(ITestContext testContext) {
        // no pre-run processing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult testResult) {
        DriverManager.closeDriver(testResult.getInstance());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTestFailure(ITestResult testResult) {
        DriverManager.closeDriver(testResult.getInstance());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTestSkipped(ITestResult testResult) {
        DriverManager.closeDriver(testResult.getInstance());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTestStart(ITestResult testResult) {
        // no pre-test processing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTestSuccess(ITestResult testResult) {
        DriverManager.closeDriver(testResult.getInstance());
    }
    
}
