package com.nordstrom.automation.selenium.core;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.annotations.NoDriver;
import com.nordstrom.automation.selenium.model.ExamplePage;
import com.nordstrom.automation.selenium.support.TestNgBase;

@InitialPage(ExamplePage.class)
public class JsUtilityTest extends TestNgBase {

    @NoDriver
    @Test(expectedExceptions = {AssertionError.class},
            expectedExceptionsMessageRegExp = "JsUtility is a static utility class that cannot be instantiated")
    public void testPrivateConstructor() throws Throwable {
        
        Constructor<?>[] ctors;
        ctors = JsUtility.class.getDeclaredConstructors();
        assertEquals(ctors.length, 1, "JsUtility must have exactly one constructor");
        assertEquals(ctors[0].getModifiers() & Modifier.PRIVATE, Modifier.PRIVATE,
                        "JsUtility constructor must be private");
        assertEquals(ctors[0].getParameterTypes().length, 0, "JsUtility constructor must have no arguments");
        
        try {
            ctors[0].setAccessible(true);
            ctors[0].newInstance();
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
    
    @Test
    public void testRun() {
        ExamplePage page = getPage();
        String script = "document.querySelector(arguments[0]).value = arguments[1];";
        JsUtility.run(page.getDriver(), script, page.getInputLocator(), "test");
        assertEquals(page.getInputValue(), "test");
    }
    
    @Test
    public void testRunAndReturn() {
        ExamplePage page = getPage();
        page.setInputValue("test");
        String script = "return document.querySelector(arguments[0]).value;";
        String value = JsUtility.runAndReturn(page.getDriver(), script, page.getInputLocator());
        assertEquals(value, "test");
    }
    
    @Test
    public void testInjectGlueLib() {
        ExamplePage page = getPage();
        WebDriver driver = page.getDriver();
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        JsUtility.injectGlueLib(page.getDriver());
        Boolean hasFunction = (Boolean) executor.executeScript("return (typeof isObject == 'function');");
        assertTrue(hasFunction);
    }
    
    @Test
    public void testPropagate() {
        ExamplePage page = getPage();
        try {
            getMetaTagNamed(page.getDriver(), "test");
            fail("No exception was thrown");
        } catch (NoSuchElementException e) {
            assertTrue(e.getMessage().startsWith("No meta element found with name: "));
        }
    }
    
    private String getMetaTagNamed(WebDriver driver, String name) {
        JsUtility.injectGlueLib(driver);
        String script = JsUtility.getScriptResource("requireMetaTagByName.js");
         
        try {
            WebElement response = JsUtility.runAndReturn(driver, script, name);
            return response.getAttribute("content");
        } catch (WebDriverException e) {
            throw JsUtility.propagate(driver, e);
        }
    }
    
    private ExamplePage getPage() {
        return (ExamplePage) getInitialPage();
    }
}
