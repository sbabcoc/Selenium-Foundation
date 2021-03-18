package com.nordstrom.automation.selenium.support;

import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.WrapsElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.google.common.base.Function;
import com.nordstrom.automation.selenium.core.WebDriverUtils;
import com.nordstrom.automation.selenium.exceptions.ConditionStillInvalidTimeoutException;
import com.nordstrom.automation.selenium.exceptions.ConditionStillValidTimeoutException;
import com.nordstrom.automation.selenium.exceptions.ElementAbsentOrHiddenTimeoutException;
import com.nordstrom.automation.selenium.exceptions.ElementAttributeTimeoutException;
import com.nordstrom.automation.selenium.exceptions.ElementNotClickableTimeoutException;
import com.nordstrom.automation.selenium.exceptions.ElementNotPresentTimeoutException;
import com.nordstrom.automation.selenium.exceptions.ElementSelectionStateTimeoutException;
import com.nordstrom.automation.selenium.exceptions.ElementStillFreshTimeoutException;
import com.nordstrom.automation.selenium.exceptions.ElementStillVisibleTimeoutException;
import com.nordstrom.automation.selenium.exceptions.ElementTextContentTimeoutException;
import com.nordstrom.automation.selenium.exceptions.NoWindowAppearedTimeoutException;
import com.nordstrom.automation.selenium.exceptions.WindowStillExistsTimeoutException;

/**
 * This utility class defines a collection of coordinator objects that enable you to synchronize your automation with
 * the system under test.
 */
public final class Coordinators {
    
    /**
     * Private constructor to prevent instantiation.
     */
    private Coordinators() {
        throw new AssertionError("Coordinators is a static utility class that cannot be instantiated");
    }
    
    /**
     * Returns a 'wait' proxy that determines if a new window has opened
     * 
     * @param initialHandles initial set of window handles
     * @return new window handle; 'null' if no new window found
     */
    public static Coordinator<String> newWindowIsOpened(final Set<String> initialHandles) {
        return new Coordinator<String>() {
            
            /**
             * {@inheritDoc}
             */
            @Override
            public String apply(final SearchContext context) {
                Set<String> currentHandles = WebDriverUtils.getDriver(context).getWindowHandles();
                currentHandles.removeAll(initialHandles);
                if (currentHandles.isEmpty()) {
                    return null;
                } else {
                    return currentHandles.iterator().next();
                }
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString() {
                return "new window to be opened";
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public TimeoutException differentiateTimeout(TimeoutException e) {
                return new NoWindowAppearedTimeoutException(e.getMessage(), e.getCause());
            }
        };
    }
    
    /**
     * Returns a 'wait' proxy that determines if the specified window has closed
     * 
     * @param windowHandle handle of window that's expected to close
     * @return 'true' if the specified window has closed; otherwise 'false'
     */
    public static Coordinator<Boolean> windowIsClosed(final String windowHandle) {
        return new Coordinator<Boolean>() {
            
            /**
             * {@inheritDoc}
             */
            @Override
            public Boolean apply(final SearchContext context) {
                Set<String> currentHandles = WebDriverUtils.getDriver(context).getWindowHandles();
                return ! currentHandles.contains(windowHandle);
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString() {
                return "window with handle '" + windowHandle + "' to be closed";
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public TimeoutException differentiateTimeout(TimeoutException e) {
                return new WindowStillExistsTimeoutException(e.getMessage(), e.getCause());
            }
        };
    }

    /**
     * An expectation for checking that an element is present on the DOM of a page. This does not
     * necessarily mean that the element is visible.
     * 
     * @param locator used to find the element
     * @return the WebElement once it is located
     */
    public static Coordinator<WebElement> presenceOfElementLocated(final By locator) {
        return new Coordinator<WebElement>() {
            
            /**
             * {@inheritDoc}
             */
            @Override
            public WebElement apply(SearchContext context) {
                return context.findElement(locator);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public String toString() {
                return "presence of element located by: " + locator;
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public TimeoutException differentiateTimeout(TimeoutException e) {
                return new ElementNotPresentTimeoutException(e.getMessage(), e.getCause());
            }
        };
    }

    /**
     * Returns a 'wait' proxy that determines if the first element matched by the specified locator is visible
     * 
     * @param locator web element locator
     * @return web element reference; 'null' if the indicated element is absent or hidden
     */
    public static Coordinator<WebElement> visibilityOfElementLocated(final By locator) {
        return new Coordinator<WebElement>() {
            
            /**
             * {@inheritDoc}
             */
            @Override
            public WebElement apply(final SearchContext context) {
                try {
                    return elementIfVisible(context.findElement(locator));
                } catch (StaleElementReferenceException e) { //NOSONAR
                    return null;
                }
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString() {
                return "visibility of element located by " + locator;
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public TimeoutException differentiateTimeout(TimeoutException e) {
                return new ElementAbsentOrHiddenTimeoutException(e.getMessage(), e.getCause());
            }
        };
    }
    
    /**
     * Returns a 'wait' proxy that determines if any element matched by the specified locator is visible
     * 
     * @param locator web element locator
     * @return web element reference; 'null' if no matching elements are visible
     */
    public static Coordinator<WebElement> visibilityOfAnyElementLocated(final By locator) {
        return new Coordinator<WebElement>() {
            
            /**
             * {@inheritDoc}
             */
            @Override
            public WebElement apply(final SearchContext context) {
                try {
                    List<WebElement> visible = context.findElements(locator);
                    if (WebDriverUtils.filterHidden(visible)) {
                        return null;
                    }
                    return visible.get(0);
                } catch (StaleElementReferenceException e) { //NOSONAR
                    return null;
                }
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString() {
                return "visibility of element located by " + locator;
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public TimeoutException differentiateTimeout(TimeoutException e) {
                return new ElementAbsentOrHiddenTimeoutException(e.getMessage(), e.getCause());
            }
        };
    }
    
    /**
     * Returns a 'wait' proxy that determines if an element is either hidden or non-existent.
     *
     * @param locator web element locator
     * @return 'true' if the element is hidden or non-existent; otherwise 'false'
     */
    public static Coordinator<Boolean> invisibilityOfElementLocated(final By locator) {
        return new Coordinator<Boolean>() {
            
            /**
             * {@inheritDoc}
             */
            @Override
            public Boolean apply(final SearchContext context) {
                try {
                    return !(context.findElement(locator).isDisplayed());
                } catch (NoSuchElementException | StaleElementReferenceException e) { //NOSONAR
                    // NoSuchElementException: The element is not present in DOM.
                    // StaleElementReferenceException: Implies that element no longer exists in the DOM.
                    return true;
                }
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString() {
                return "element to no longer be visible: " + locator;
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public TimeoutException differentiateTimeout(TimeoutException e) {
                return new ElementStillVisibleTimeoutException(e.getMessage(), e.getCause());
            }
        };
    }

    /**
     * An expectation for checking that an element, known to be present on the
     * DOM of a page, is visible. Visibility means that the element is not only
     * displayed but also has a height and width that is greater than 0.
     *
     * @param element the WebElement
     * @return the (same) WebElement once it is visible
     */
    public static Coordinator<WebElement> visibilityOf(final WebElement element) {
        return new Coordinator<WebElement>() {
            
            /**
             * {@inheritDoc}
             */
            @Override
            public WebElement apply(SearchContext context) {
                return elementIfVisible(element);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public String toString() {
                return "visibility of " + element;
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public TimeoutException differentiateTimeout(TimeoutException e) {
                return new ElementAbsentOrHiddenTimeoutException(e.getMessage(), e.getCause());
            }
        };
    }

    /**
     * Return a visibility-filtered element reference
     * 
     * @param element element whose visibility is in question
     * @return specified element reference; 'null' if element is hidden
     */
    @SuppressWarnings("squid:S1774")
    private static WebElement elementIfVisible(final WebElement element) {
        return element.isDisplayed() ? element : null;
    }
    
    /**
     *  Returns a 'wait' proxy that determines if an element is either hidden or non-existent.
     * 
     * @param element web element reference
     * @return 'true' if the specified element is hidden
     */
    public static Coordinator<Boolean> invisibilityOf(final WebElement element) {
        return new Coordinator<Boolean>() {
            
            /**
             * {@inheritDoc}
             */
            @Override
            public Boolean apply(SearchContext context) {
                try {
                    return ! element.isDisplayed();
                } catch (StaleElementReferenceException | NoSuchElementException e) {
                    return true;
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public String toString() {
              return String.format("element (%s) to be invisible", element);
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public TimeoutException differentiateTimeout(TimeoutException e) {
                return new ElementStillVisibleTimeoutException(e.getMessage(), e.getCause());
            }
        };
    }

    /**
     * An expectation for checking that an element is visible and enabled such that you can click it.
     * 
     * @param locator used to find the element
     * @return the WebElement once it is located and clickable (visible and enabled)
     */
    public static Coordinator<WebElement> elementToBeClickable(final By locator) {
        return new Coordinator<WebElement>() {

            private final Coordinator<WebElement> visibilityOfElementLocated = visibilityOfElementLocated(locator);

            /**
             * {@inheritDoc}
             */
            @Override
            public WebElement apply(SearchContext context) {
                WebElement element = visibilityOfElementLocated.apply(context);
                try {
                    if (element != null && element.isEnabled()) {
                        return element;
                    } else {
                        return null;
                    }
                } catch (StaleElementReferenceException | NoSuchElementException e) {
                    return null;
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public String toString() {
                return "element to be clickable: " + locator;
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public TimeoutException differentiateTimeout(TimeoutException e) {
                return new ElementNotClickableTimeoutException(e.getMessage(), e.getCause());
            }
        };
    }

    /**
     * An expectation for checking an element is visible and enabled such that
     * you can click it.
     *
     * @param element the WebElement
     * @return the (same) WebElement once it is clickable (visible and enabled)
     */
    public static Coordinator<WebElement> elementToBeClickable(final WebElement element) {
        return new Coordinator<WebElement>() {

            private final Coordinator<WebElement> visibilityOfElement = visibilityOf(element);

            /**
             * {@inheritDoc}
             */
            @Override
            public WebElement apply(SearchContext context) {
                WebElement element = visibilityOfElement.apply(context);
                try {
                    if (element != null && element.isEnabled()) {
                        return element;
                    } else {
                        return null;
                    }
                } catch (StaleElementReferenceException | NoSuchElementException e) {
                    return null;
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public String toString() {
                return "element to be clickable: " + element;
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public TimeoutException differentiateTimeout(TimeoutException e) {
                return new ElementNotClickableTimeoutException(e.getMessage(), e.getCause());
            }
        };
    }

    /**
     * An expectation for checking that an indicated element is selected
     * 
     * @param locator used to find the element
     * @return 'true' once the indicated element is selected
     */
    public static Coordinator<Boolean> elementToBeSelected(final By locator) {
        return elementSelectionStateToBe(locator, true);
    }

    /**
     * An expectation for checking that an indicated element has acquired the desired selection state
     * 
     * @param locator used to find the element
     * @param selected desired selection state
     * @return 'true' once the indicated element acquired the desired selection state
     */
    public static Coordinator<Boolean> elementSelectionStateToBe(final By locator, final boolean selected) {
        return new Coordinator<Boolean>() {
            
            /**
             * {@inheritDoc}
             */
            @Override
            public Boolean apply(SearchContext context) {
                try {
                    WebElement element = context.findElement(locator);
                    return (element.isSelected() == selected);
                } catch (StaleElementReferenceException | NoSuchElementException e) {
                    return null; //NOSONAR
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public String toString() {
                return String.format("element found by %s to %sbe selected", locator, (selected ? "" : "not "));
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public TimeoutException differentiateTimeout(TimeoutException e) {
                return new ElementSelectionStateTimeoutException(e.getMessage(), e.getCause());
            }
        };
    }

    /**
     * An expectation for checking if the given text is present in the element that matches the
     * given locator.
     * 
     * @param locator used to find the element
     * @param text to be present in the element found by the locator
     * @return true once the first element located by locator contains the given text
     */
    public static Coordinator<Boolean> textToBePresentInElementLocated(final By locator, final String text) {
        return new Coordinator<Boolean>() {
            
            /**
             * {@inheritDoc}
             */
            @Override
            public Boolean apply(SearchContext context) {
                try {
                    String elementText = context.findElement(locator).getText();
                    return elementText.contains(text);
                } catch (StaleElementReferenceException | NoSuchElementException e) {
                    return null; //NOSONAR
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public String toString() {
                return String.format("text ('%s') to be present in element found by %s", text, locator);
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public TimeoutException differentiateTimeout(TimeoutException e) {
                return new ElementTextContentTimeoutException(e.getMessage(), e.getCause());
            }
        };
    }

    /**
     * An expectation for checking if text is present in the element that matches the given locator.
     *
     * @param locator used to find the element
     * @return true once the first element located by locator does not have empty text
     */
    public static Coordinator<Boolean> textToNotBeEmptyInElementLocated(final By locator) {
        return new Coordinator<Boolean>() {
            
            /**
             * {@inheritDoc}
             */
            @Override
            public Boolean apply(SearchContext context) {
                try {
                    String elementText = context.findElement(locator).getText();
                    return ! ((elementText == null) || elementText.isEmpty());
                } catch (StaleElementReferenceException | NoSuchElementException e) {
                    return null; //NOSONAR
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public String toString() {
                return String.format("element found by %s to be non-empty", locator);
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public TimeoutException differentiateTimeout(TimeoutException e) {
                return new ElementTextContentTimeoutException(e.getMessage(), e.getCause());
            }
        };
    }

    /**
     * An expectation for checking if the given text is present in the specified elements value
     * attribute.
     * 
     * @param locator used to find the element
     * @param text to be present in the value attribute of the element found by the locator
     * @return true once the value attribute of the first element located by locator contains the
     *         given text
     */
    public static Coordinator<Boolean> textToBePresentInElementValue(final By locator, final String text) {
        return new Coordinator<Boolean>() {
            
            /**
             * {@inheritDoc}
             */
            @Override
            public Boolean apply(SearchContext context) {
                try {
                    String elementText = context.findElement(locator).getAttribute("value");
                    return elementText != null && elementText.contains(text);
                } catch (StaleElementReferenceException | NoSuchElementException e) {
                    return null; //NOSONAR
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public String toString() {
                return String.format("text ('%s') to be the value of element located by %s", text, locator);
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public TimeoutException differentiateTimeout(TimeoutException e) {
                return new ElementAttributeTimeoutException(e.getMessage(), e.getCause());
            }
        };
    }

    /**
     * An expectation for checking if the given value is present in the specified elements value
     * attribute.
     * 
     * @param locator used to find the element
     * @param attribute that will be checked in the element found by the locator
     * @param value the specified attribute will attain in the element found by the locator
     * @return true once the specified attribute of the first element located by locator contains
     *         the given value
     */
    public static Coordinator<Boolean> elementToHaveAttributeValue(final By locator,
            final String attribute, final String value) {
        
        return new Coordinator<Boolean>() {
            
            /**
             * {@inheritDoc}
             */
            @Override
            public Boolean apply(SearchContext context) {
                try {
                    String attrib = context.findElement(locator).getAttribute(attribute);
                    if (attrib != null) {
                        return attrib.equals(value);
                    } else {
                        return (value == null);
                    }
                } catch (StaleElementReferenceException | NoSuchElementException e) {
                    return null; //NOSONAR
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public String toString() {
                return String.format("text ('%s') to be the value of element located by %s", value, locator);
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public TimeoutException differentiateTimeout(TimeoutException e) {
                return new ElementAttributeTimeoutException(e.getMessage(), e.getCause());
            }
        };
    }

    /**
     * Boolean wrapper for a condition, which returns 'true' if the expectation is met.
     * 
     * @param condition expected condition
     * @return 'true' if the specified condition returns a 'positive' result
     */
    public static Coordinator<Boolean> has(final Function<SearchContext, ?> condition) {
        return new Coordinator<Boolean>() {
            
            /**
             * {@inheritDoc}
             */
            @Override
            public Boolean apply(SearchContext context) {
                Object result = condition.apply(context);
                if (result != null) {
                    if (result instanceof Boolean) {
                        return (Boolean) result;
                    } else {
                        return true;
                    }
                }
                return false;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public String toString() {
                return "condition to be valid: " + condition;
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public TimeoutException differentiateTimeout(TimeoutException e) {
                if (condition instanceof Coordinator) {
                    return ((Coordinator<?>) condition).differentiateTimeout(e);
                }
                return new ConditionStillInvalidTimeoutException(e.getMessage(), e.getCause());
            }
        };
    }

    /**
     * Inverse wrapper for a condition, which returns 'false' if the expectation is met.
     * 
     * @param condition expected condition
     * @return 'true' if the specified condition returns a 'negative' result
     */
    public static Coordinator<Boolean> not(final Function<SearchContext, ?> condition) {
        return new Coordinator<Boolean>() {
            
            /**
             * {@inheritDoc}
             */
            @Override
            public Boolean apply(SearchContext context) {
                Object result = condition.apply(context);
                return result == null || result == Boolean.FALSE;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public String toString() {
                return "condition to not be valid: " + condition;
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public TimeoutException differentiateTimeout(TimeoutException e) {
                return new ConditionStillValidTimeoutException(e.getMessage(), e.getCause());
            }
        };
    }

    /**
     * Returns a 'wait' proxy that determines if the specified element reference has gone stale.
     *
     * @param element the element to wait for
     * @return 'false' if the element reference is still valid; otherwise 'true'
     */
    public static Coordinator<Boolean> stalenessOf(final WebElement element) {
        return new Coordinator<Boolean>() {
            private final ExpectedCondition<Boolean> condition = conditionInitializer();

            // initializer for [condition] field
            private final ExpectedCondition<Boolean> conditionInitializer() {
                if (element instanceof WrapsElement) {
                    return ExpectedConditions.stalenessOf(((WrapsElement) element).getWrappedElement());
                } else {
                    return ExpectedConditions.stalenessOf(element);
                }
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public Boolean apply(final SearchContext ignored) {
                return condition.apply(null);
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString() {
                return condition.toString();
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public TimeoutException differentiateTimeout(TimeoutException e) {
                return new ElementStillFreshTimeoutException(e.getMessage(), e.getCause());
            }
        };
    }
}
