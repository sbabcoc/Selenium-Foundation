package com.nordstrom.automation.selenium.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.SeleniumConfig;
import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.annotations.NoDriver;
import com.nordstrom.automation.selenium.model.ExamplePage;
import com.nordstrom.automation.selenium.model.RobustJavascriptExecutor;
import com.nordstrom.automation.selenium.support.TestNgBase;

@InitialPage(ExamplePage.class)
public class WebDriverUtilsTest extends TestNgBase {

    @NoDriver
    @Test(expectedExceptions = {AssertionError.class},
            expectedExceptionsMessageRegExp = "WebDriverUtils is a static utility class that cannot be instantiated")
    public void testPrivateConstructor() throws Throwable {
        
        Constructor<?>[] ctors;
        ctors = WebDriverUtils.class.getDeclaredConstructors();
        assertEquals(ctors.length, 1, "WebDriverUtils must have exactly one constructor");
        assertEquals(ctors[0].getModifiers() & Modifier.PRIVATE, Modifier.PRIVATE,
                        "WebDriverUtils constructor must be private");
        assertEquals(ctors[0].getParameterTypes().length, 0, "WebDriverUtils constructor must have no arguments");
        
        try {
            ctors[0].setAccessible(true);
            ctors[0].newInstance();
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
    
    @Test
    public void testGetDriver() {
        WebDriver driver = getDriver();
        ExamplePage page = getPage();
        WebElement element = page.findElement(By.tagName("html"));
        
        assertTrue(WebDriverUtils.getDriver((SearchContext) driver) == driver);
        assertTrue(WebDriverUtils.getDriver(page) == driver);
        assertTrue(WebDriverUtils.getDriver(element) == driver);
        
        try {
            WebDriverUtils.getDriver(mock(SearchContext.class));
            fail("No exception was thrown");
        } catch (UnsupportedOperationException e) {
            assertEquals(e.getMessage(), "Unable to extract the driver from the specified context");
        }
    }
    
    @Test
    public void testGetExecutor() {
        WebDriver driver = getDriver();
        ExamplePage page = getPage();
        WebElement element = page.findElement(By.tagName("html"));
        
        verifyExecutor(driver, driver);
        verifyExecutor(page, driver);
        verifyExecutor(element, driver);
        
        try {
            WebDriverUtils.getExecutor(mock(WebDriver.class));
            fail("No exception was thrown");
        } catch (UnsupportedOperationException e) {
            assertEquals(e.getMessage(), "The specified context is unable to execute JavaScript");
        }
    }
    
    @Test
    public void testBrowserName() {
        WebDriver driver = getDriver();
        ExamplePage page = getPage();
        WebElement element = page.findElement(By.tagName("html"));
        SeleniumConfig config = SeleniumConfig.getConfig();
        String browserName = config.getCurrentCapabilities().getBrowserName();
        
        assertThat(WebDriverUtils.getBrowserName((SearchContext) driver), equalToIgnoringCase(browserName));
        assertThat(WebDriverUtils.getBrowserName(page), equalToIgnoringCase(browserName));
        assertThat(WebDriverUtils.getBrowserName(element), equalToIgnoringCase(browserName));
        
        try {
            WebDriverUtils.getBrowserName(mock(WebDriver.class));
            fail("No exception was thrown");
        } catch (UnsupportedOperationException e) {
            assertEquals(e.getMessage(), "The specified context is unable to describe its capabilities");
        }
    }
    
    @Test
    public void testFilterHidden() {
        ExamplePage page = getPage();
        
        List<WebElement> elements = page.findElements(By.tagName("p"));
        int total = elements.size();
        assertNotEquals(total, 0);
        assertFalse(WebDriverUtils.filterHidden(elements));
        assertEquals(elements.size(), total - 1);
        
        elements = page.findElements(By.id("hidden-para"));
        total = elements.size();
        assertEquals(total, 1);
        assertTrue(WebDriverUtils.filterHidden(elements));
        assertTrue(elements.isEmpty());
    }
    
    private static void verifyExecutor(SearchContext context, WebDriver driver) {
        JavascriptExecutor executor = WebDriverUtils.getExecutor(context);
        assertTrue(executor instanceof RobustJavascriptExecutor);
        assertTrue(((RobustJavascriptExecutor) executor).getWrappedDriver() == driver);
    }
    
    private ExamplePage getPage() {
        return (ExamplePage) getInitialPage();
    }
}
