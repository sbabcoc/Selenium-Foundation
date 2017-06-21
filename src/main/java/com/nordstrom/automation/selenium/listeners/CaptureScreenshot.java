package com.nordstrom.automation.selenium.listeners;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.Reporter;

/**
 * Take the screenshot as quickly as possible to minimize the time delay between the the event and
 * what the user sees in the report. Enough time can pass and the UI state may change between the
 * event and when the screenshot is taken.
 */
public class CaptureScreenshot implements ITestListener {

    private final static String SCREENSHOT_FILE_EXTENSION = "png";
    private final static String SCREENSHOT_STORAGE_NAME = "screenshots";
    private final static String HTML_LINK_TEMPLATE = "<br /> <img src=\"%s\" /> <br />";

    private static final Logger LOGGER = LoggerFactory.getLogger(CaptureScreenshot.class);

    @Override
    public void onFinish(ITestContext testContext) {
    }

    @Override
    public void onStart(ITestContext testContext) {
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult testResult) {
    }

    @Override
    public void onTestFailure(ITestResult testResult) {
        WebDriver driver = DriverManager.getDriver(testResult);
        ITestContext context = testResult.getTestContext();
        
        if (!isScreenshotCapable(driver)) {
            return;
        }

        byte[] screenshot;
        try {
            screenshot = getScreenshot(driver);
        } catch (WebDriverException e) {
            String messageTemplate =
                    "The driver is capable of taking a screenshot, but it failed because (%s).";
            LOGGER.info(String.format(messageTemplate, e.toString()));
            return;
        }

        Path storageLocation = getStorageLocation(context);
        if (!(Files.exists(storageLocation))) {
            try {
                Files.createDirectory(storageLocation);
            } catch (IOException e) {
                String messageTemplate = "The screenshot storage location (%s) failed to be created, so a screenshot will be not available.";
                LOGGER.info(String.format(messageTemplate, storageLocation));
                return;
            }
        }

        Path targetScreenshotFile = getPathToTargetScreenshotFile(testResult) ;
        try {
            putScreenshotInStorage(screenshot, targetScreenshotFile);
        } catch (IOException e) {
            String messageTemplate = "The screenshot was successfully taken, but unable to be written to (%s).";
            LOGGER.info(String.format(messageTemplate, targetScreenshotFile));
            return;
        }

        createReportLinkToScreenshot(targetScreenshotFile);
    }

    @Override
    public void onTestSkipped(ITestResult testResult) {
    }

    @Override
    public void onTestStart(ITestResult testResult) {
    }

    @Override
    public void onTestSuccess(ITestResult testResult) {
    }

    /**
     * Not every driver supports screenshot-taking.
     * 
     * @param driver
     * @return true if it supports screenshots
     */
    private boolean isScreenshotCapable(WebDriver driver) {
        Boolean isScreenshotCapable = driver instanceof TakesScreenshot;
        if (!(isScreenshotCapable)) {
            String messageTemplate =
                    "This driver is not capable of taking a screenshot.  If a screenshot is desired, use a WebDriver implementation that supports screenshots.  https://seleniumhq.github.io/selenium/docs/api/java/org/openqa/selenium/TakesScreenshot.html";
            LOGGER.info(messageTemplate);
        }

        return isScreenshotCapable;
    }

    /**
     * Driver must be isScreenshotCapable before using this.
     * 
     * @param driver
     * @return
     */
    private byte[] getScreenshot(WebDriver driver) {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    }

    /**
     * Screenshots will exist within a subdirectory of a TestNG test-output location, that is a
     * publicly-accessible.
     * 
     * @param testContext
     * @return the location where screenshots should be stored.
     */
    private Path getStorageLocation(ITestContext testContext) {
        String outputDirectoryLocation = testContext.getOutputDirectory();
        Path outputDirectory = Paths.get(outputDirectoryLocation);
        Path screenshotStorage = outputDirectory.resolve(SCREENSHOT_STORAGE_NAME);

        return screenshotStorage;
    }

    /**
     * Copy the screenshot from memory to the filesystem.
     * 
     * @param memoryScreenshot
     * @param fsTarget
     * @throws IOException if an error occurs while creating or writing the screenshot to a file.
     */
    private void putScreenshotInStorage(byte[] memoryScreenshot, Path fsTarget) throws IOException {
        String messageTemplate = "Placing a screenshot of the event at (%s).";
        LOGGER.info(String.format(messageTemplate, fsTarget.toString()));
        
        // Files.write should be good enough for a <2-5Mb file, typical of a screenshot.
        Files.write(fsTarget, memoryScreenshot);
    }

    /**
     * Create a note in the testcase results that includes a clickable reference to the screenshot.
     * 
     * @param target
     */
    private void createReportLinkToScreenshot(Path target) {
        Reporter.log(String.format(HTML_LINK_TEMPLATE, target.toString()));
    }

    /**
     * Create the the target path to where the screenshot will be placed.
     * 
     * @param testResult
     * @return the ideal location where the screenshot should be stored.
     */
    private Path getPathToTargetScreenshotFile(ITestResult testResult) {
        Path screenshotStorage = getStorageLocation(testResult.getTestContext());
        Path fsScreenshot = screenshotStorage.resolve(getTargetFilename(testResult));

        return fsScreenshot;
    }

    /**
     * For parameterized tests, unique identifiers are included in the filename so if it fails 2+, the
     * the screenshots will not be overwritten.
     * 
     * The returned image format is assumed to be PNG, but this is not documented within the
     * Selenium project -- just appened ".png" to the filename.
     * 
     * @param testResult
     * @return an identifying name for the screenshot file
     */
    private String getTargetFilename(ITestResult testResult) {
        int hashcode = Arrays.deepHashCode(testResult.getParameters());
        
        StringBuilder builder = new StringBuilder();
        builder.append(getTestName(testResult));
        builder.append("-");
        builder.append(hashcode);
        builder.append(".");
        builder.append(SCREENSHOT_FILE_EXTENSION);

        return builder.toString();
    }

    /**
     * If a name for the test has been specified, use that.  If not, then return the name of the method.
     * @param testResult
     * @return the name of the testcase
     */
    private String getTestName(ITestResult testResult) {
        // TODO What does `getTestName' return if @Test(testName=...) is not used, but the testcase
        //      implements ITest?  Documentation is not clear.  The worst case scenario is that we have a
        //      redundant method call (because it might just call getName).  If it doesn't call getName,
        //      then we are providing a better default.
        LOGGER.debug("getTestname output:  {}", testResult.getTestName());
        LOGGER.debug("getName output:  {}", testResult.getName());
        LOGGER.debug("getMethodName output:  {}", testResult.getMethod().getMethodName());
        LOGGER.debug("getConstructorOrMethod.getName output:  {}", testResult.getMethod().getConstructorOrMethod().getName());
        LOGGER.debug("getConstructorOrMethod.getMethod.getName output:  {}", testResult.getMethod().getConstructorOrMethod().getMethod().getName());

        String testName = testResult.getTestName();
        
        if (testName == null) {
            testName = testResult.getName();
        }

        return testName;
    }
}