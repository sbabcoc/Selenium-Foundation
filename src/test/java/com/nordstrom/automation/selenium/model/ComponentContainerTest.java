package com.nordstrom.automation.selenium.model;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.annotations.PageUrl;

public class ComponentContainerTest {

    private static final URI targetUri = URI.create("http://target.com/basepath/");
    
    @Test(expectedExceptions = {NullPointerException.class},
                    expectedExceptionsMessageRegExp = "\\[element\\] must be non-null")
    public void updateElementNullWithString() {
        ComponentContainer.updateValue(null, "");
    }

    @Test(expectedExceptions = {NullPointerException.class},
                    expectedExceptionsMessageRegExp = "\\[element\\] must be non-null")
    public void updateElementNullWithBoolean() {
        ComponentContainer.updateValue(null, false);
    }

    @Test(expectedExceptions = {NullPointerException.class},
                    expectedExceptionsMessageRegExp = "\\[element\\] must be non-null")
    public void verifyValueEqualsNullCheck() throws Throwable {
        try {
            Method method = ComponentContainer.class.getDeclaredMethod("valueEquals", WebElement.class, String.class);
            method.setAccessible(true);
            method.invoke(null, null, "");
        } catch (NoSuchMethodException | SecurityException e) {
            throw new AssertionError("Unable to acquire reference to: valueEquals(WebElement, String)", e);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new AssertionError("Failure to invoke reference to: valueEquals(WebElement, String)", e);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }
    
    @Test
    public void getRelativePageUrl() {
        String url = ComponentContainer.getPageUrl(pageUrl(PageOne.class), targetUri);
        assertEquals(url, "http://target.com/basepath/page-one");
    }
    
    @Test
    public void getAbsolutePageUrl() {
        String url = ComponentContainer.getPageUrl(pageUrl(PageTwo.class), targetUri);
        assertEquals(url, "http://example.com/page-two");
    }
    
    @Test
    public void getPageUrlWithScheme() {
        String url = ComponentContainer.getPageUrl(pageUrl(PageThree.class), targetUri);
        assertEquals(url, "https://target.com/basepath/page-three");
    }
    
    @Test
    public void getPageUrlWithHost() {
        String url = ComponentContainer.getPageUrl(pageUrl(PageFour.class), targetUri);
        assertEquals(url, "http://new-host.org/basepath/page-four");
    }
    
    @Test
    public void getPageUrlWithPort() {
        String url = ComponentContainer.getPageUrl(pageUrl(PageFive.class), targetUri);
        assertEquals(url, "http://target.com:2020/basepath/page-five");
    }
    
    @Test
    public void getPageUrlWithUserInfo() {
        String url = ComponentContainer.getPageUrl(pageUrl(PageSix.class), targetUri);
        assertEquals(url, "http://user:pass@target.com/basepath/page-six");
    }
    
    @Test
    public void getResourceFilePageUrl() {
        String url = ComponentContainer.getPageUrl(pageUrl(FilePage.class), targetUri);
        
        // create URI from URL
        URI uri = URI.create(url);
        // verify scheme
        assertEquals(uri.getScheme(), "file");
        // verify resource file path
        assertTrue(uri.getPath().endsWith("/ExamplePage.html"));
        // verify file exists
        assertTrue(new File(uri).exists());
    }
    
    @Test
    public void verifyLandingPage() {
        PageUrlExample page = new PageUrlExample(mockDriver());
        PageUrl pageUrl = PageUrlExample.class.getAnnotation(PageUrl.class);
        ComponentContainer.verifyLandingPage(page, PageUrlExample.class, pageUrl, targetUri);
    }

    /**
     * Create mocked {@link WebDriver} object.
     * 
     * @return mocked WebDriver object
     */
    private static WebDriver mockDriver() {
        WebDriver driver = mock(WebDriver.class, withSettings().extraInterfaces(JavascriptExecutor.class));
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        when(executor.executeScript(any(String.class), any())).thenReturn(null);
        return driver;
    }
    
    /**
     * Get {@link PageUrl} annotation for the specified page class
     * 
     * @param pageClass page class
     * @return {@link PageUrl} annotation object
     */
    private PageUrl pageUrl(Class<?> pageClass) {
        return pageClass.getAnnotation(PageUrl.class);
    }
    
    @PageUrl("/page-one")
    static class PageOne { }
    
    @PageUrl("http://example.com/page-two")
    static class PageTwo { }
    
    @PageUrl(scheme="https", value="/page-three")
    static class PageThree { }
    
    @PageUrl(host="new-host.org", value="/page-four")
    static class PageFour { }
    
    @PageUrl(port="2020", value="/page-five")
    static class PageFive { }
    
    @PageUrl(userInfo="user:pass", value="/page-six")
    static class PageSix { }
    
    @PageUrl(scheme="file", value="ExamplePage.html")
    static class FilePage{ }
    
    @PageUrl(pattern="sub-path\\.do|main\\.do", value="main.do", params={"one=foo|oof", "two=bare?", "deadbeef"})
    static class PageUrlExample extends Page
    {
        public PageUrlExample(WebDriver driver) {
            super(driver);
        }
        
        @Override
        public String getCurrentUrl() {
            return targetUri.toString() + "sub-path.do?deadbeef&two=bar&one=foo";
        }
    }
}
