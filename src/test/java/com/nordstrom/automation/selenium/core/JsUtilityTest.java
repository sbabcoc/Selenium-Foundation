package com.nordstrom.automation.selenium.core;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import static com.nordstrom.automation.selenium.platform.TargetType.SUPPORT_NAME;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.annotations.InitialPage;
import com.nordstrom.automation.selenium.annotations.NoDriver;
import com.nordstrom.automation.selenium.examples.ExamplePage;
import com.nordstrom.automation.selenium.examples.ShadowRootComponent;
import com.nordstrom.automation.selenium.examples.TestNgTargetRoot;
import com.nordstrom.automation.selenium.platform.TargetPlatform;

@InitialPage(ExamplePage.class)
public class JsUtilityTest extends TestNgTargetRoot {

    @NoDriver
    @TargetPlatform(SUPPORT_NAME)
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
        JsUtility.run(page.getWrappedDriver(), script, page.getInputLocator(), "test");
        assertEquals(page.getInputValue(), "test");
    }
    
    @Test
    public void testRunAndReturn() {
        ExamplePage page = getPage();
        page.setInputValue("test");
        String script = "return document.querySelector(arguments[0]).value;";
        String value = JsUtility.runAndReturn(page.getWrappedDriver(), script, page.getInputLocator());
        assertEquals(value, "test");
    }
    
    @Test
    public void testRuntimeLib() {
        ExamplePage page = getPage();
        WebDriver driver = page.getWrappedDriver();
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        JsUtility.injectRuntime(driver);
        Boolean hasFunction = (Boolean) executor.executeScript("return (typeof __wdRuntime.runSync === 'function');");
        assertTrue(hasFunction);
    }
    
    @Test
    public void testPropagate() {
        skipIfSafariOnIOS();
        ExamplePage page = getPage();
        try {
            getMetaTagNamed(page.getWrappedDriver(), "test");
            fail("No exception was thrown");
        } catch (NoSuchElementException e) {
            assertTrue(e.getMessage().startsWith("No meta element found with name: "));
        }
    }
    
    @Test
    public void testShadowRun() {
        skipIfNoShadowDom();
        ExamplePage page = getPage();
        ShadowRootComponent shadowRoot = page.getShadowRootByLocator();
        String script = "arguments[0].querySelector(arguments[1]).value = arguments[2];";
        JsUtility.run(
                page.getWrappedDriver(), script, shadowRoot.getWrappedContext(), shadowRoot.getInputLocator(), "test");
        assertEquals(shadowRoot.getInputValue(), "test");
    }
    
    @Test
    public void testShadowRunAndReturn() {
        skipIfNoShadowDom();
        ExamplePage page = getPage();
        ShadowRootComponent shadowRoot = page.getShadowRootByElement();
        shadowRoot.setInputValue("test");
        String script = "return arguments[0].querySelector(arguments[1]).value;";
        String value = JsUtility.runAndReturn(
                page.getWrappedDriver(), script, shadowRoot.getWrappedContext(), shadowRoot.getInputLocator());
        assertEquals(value, "test");
    }
    
    @Test
    public void testDriverAutoWrapSurvivesStaleness() {
        // This is the mechanism with the least coverage of all: it's never invoked explicitly by test
        // code, only through RobustDriverFactory's proxy installed by DriverManager.injectDriver, so this
        // calls executeScript() directly, the same way any ordinary, wrapping-unaware caller would, to
        // confirm the automatic path actually wraps results and survives staleness with no opt-in at all.
        ExamplePage page = getPage();
        String script = "return document.querySelector(\"p[id^='para-']\");";
        Object result = ((JavascriptExecutor) page.getWrappedDriver()).executeScript(script);
        assertTrue(result instanceof WebElement, "Driver's own executeScript should return a wrapped element");
        WebElement paragraph = (WebElement) result;
        assertEquals(paragraph.getText(), page.getParagraphs().get(0));
        
        page.getWrappedDriver().navigate().refresh();
        
        assertEquals(paragraph.getText(), page.getParagraphs().get(0));
    }
    
    private String getMetaTagNamed(WebDriver driver, String name) {
        String script = JsUtility.getScriptResource("requireMetaTagByName.js");
        WebElement response = JsUtility.runAndReturn(driver, script, name);
        return WebDriverUtils.getDomPropertyOf(response, "content");
    }
    
    private ExamplePage getPage() {
        return (ExamplePage) getInitialPage();
    }
}
