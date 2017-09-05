package com.nordstrom.automation.selenium.model;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.WrapsDriver;
import org.testng.annotations.Test;

public class ComponentContainerTest {

    @Test
    public void updateTextInputSameValue() {
        WebElement elementWithValue = mockElement("input", "Nordstrom", false);
        assertFalse(ComponentContainer.updateValue(elementWithValue, "Nordstrom"));
    }

    @Test
    public void updateTextInputNewValue() {
        WebElement elementWithValue = mockElement("input", "Nordstrom", false);
        assertTrue(ComponentContainer.updateValue(elementWithValue, "HauteLook"));
    }

    @Test
    public void updateTextInputBoolValue() {
        WebElement elementWithValue = mockElement("input", "Nordstrom", false);
        assertTrue(ComponentContainer.updateValue(elementWithValue, true));
    }

    @Test
    public void updateTextInputNullValue() {
        WebElement elementWithValue = mockElement("input", "Nordstrom", false);
        assertTrue(ComponentContainer.updateValue(elementWithValue, null));
    }

    @Test
    public void updateCheckboxSameValue() {
        WebElement elementWithValue = mockElement("input", "false", true);
        assertFalse(ComponentContainer.updateValue(elementWithValue, false));
    }

    @Test
    public void updateCheckboxNewValue() {
        WebElement elementWithValue = mockElement("input", "false", true);
        assertTrue(ComponentContainer.updateValue(elementWithValue, true));
    }

    @Test
    public void updateCheckboxStringValue() {
        WebElement elementWithValue = mockElement("input", "false", true);
        assertTrue(ComponentContainer.updateValue(elementWithValue, "true"));
    }

    @Test
    public void updateCheckboxNullValue() {
        WebElement elementWithValue = mockElement("input", "false", true);
        assertFalse(ComponentContainer.updateValue(elementWithValue, null));
    }

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
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Unable to acquire reference to: valueEquals(WebElement, String)", e);
        } catch (SecurityException e) {
            throw new AssertionError("Security violation acquiring reference to: valueEquals(WebElement, String)", e);
        } catch (IllegalAccessException e) {
            throw new AssertionError("Illegal access invoking: valueEquals(WebElement, String)", e);
        } catch (IllegalArgumentException e) {
            throw new AssertionError("Illegal argument invoking: valueEquals(WebElement, String)", e);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    /**
     * Create mocked {@link WebElement} object.
     * 
     * @param type element type
     * @param value element value
     * @param isCheckbox 'true' is checkbox is desired; otherwise 'false'
     * @return mocked WebElement object
     */
    private static WebElement mockElement(String type, String value, boolean isCheckbox) {
        WebElement element = mock(WebElement.class, withSettings().extraInterfaces(WrapsDriver.class));
        when(element.getTagName()).thenReturn(type);
        if (isCheckbox) {
            when(element.getAttribute("type")).thenReturn("checkbox");
            when(element.getAttribute("value")).thenReturn("isSelected: " + value);
            when(element.isSelected()).thenReturn(Boolean.parseBoolean(value));
        } else {
            when(element.getAttribute("type")).thenReturn("text");
            when(element.getAttribute("value")).thenReturn(value);
            when(element.isSelected()).thenReturn(false);
        }

        WebDriver driver = mockDriver();
        when(((WrapsDriver) element).getWrappedDriver()).thenReturn(driver);
        return element;
    }

    /**
     * Create mocked {@link WebDriver} object.
     * 
     * @return mocked WebDriver object
     */
    private static WebDriver mockDriver() {
        WebDriver driver = mock(WebDriver.class, withSettings().extraInterfaces(JavascriptExecutor.class));
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        when(executor.executeScript(any(String.class), anyVararg())).thenReturn(null);
        return driver;
    }
}
