package com.nordstrom.automation.selenium.model;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.interfaces.WrapsContext;

/**
 * Unit tests covering acquisition behavior when a {@link RobustElementWrapper}'s parent context is an
 * unresolved optional element or component (selenium-foundation issue #192).
 * <p>
 * Uses {@link RobustElementWrapper}'s package-private {@code getDeferredException()}, {@code getWrapped()},
 * and {@code acquireReference()} to assert on and exercise internal state directly, rather than reflecting
 * into private members - a compile-time-checked seam that exists specifically for this kind of test support.
 */
public class RobustElementWrapperTest {

    /**
     * A {@link RobustElementWrapper} whose parent context reports {@code hasReference() == false} must never
     * attempt to derive a driver or search context from that parent - it must fail immediately and cleanly.
     * <p>
     * This exercises the constructor's driver-acquisition fix directly: prior to the fix, an absent parent
     * caused {@code WebDriverUtils.getDriver(context.getWrappedContext())} to be invoked against a {@code null}
     * search context, throwing an opaque {@link UnsupportedOperationException} instead of resolving gracefully.
     * <p>
     * {@code hasReference() == false} alone would also be true if the wrapper failed for some unrelated reason
     * (e.g. a swallowed exception elsewhere), so this also inspects the stored deferred exception directly to
     * confirm the absence was actually attributed to the parent context, and confirms the parent's
     * {@code hasReference()} was the thing consulted to reach that conclusion.
     */
    @Test
    public void findOptional_onAbsentOptionalParent_reportsAbsentWithoutThrowing() {
        WrapsContext mockContext = mock(WrapsContext.class);
        when(mockContext.hasReference()).thenReturn(false);
        when(mockContext.getWrappedContext()).thenReturn(null);
        when(mockContext.getWrappedDriver()).thenReturn(mock(WebDriver.class));

        // constructing an OPTIONAL child of an absent parent must not throw
        RobustElementWrapper wrapper =
                new RobustElementWrapper(null, mockContext, By.id("child"), RobustElementWrapper.OPTIONAL);

        assertFalse(wrapper.hasReference(),
                "Child of an absent optional parent should itself report no reference");
        verify(mockContext, atLeastOnce()).hasReference();

        NoSuchElementException deferred = wrapper.getDeferredException();
        assertTrue(deferred.getMessage().contains("absent optional element"),
                "Deferred exception should attribute the absence to the parent context, not some other cause");
    }

    /**
     * The same absent-parent condition, but for a non-optional (CARDINAL) child, must surface as a clean,
     * retryable {@link NoSuchElementException} - not the {@link UnsupportedOperationException} that comes from
     * an unguarded {@code null} search context, and not a raw {@link NullPointerException}.
     * <p>
     * {@code acquireReference} is invoked directly (bypassing the constructor's normal
     * {@code refreshReference}/wait-and-retry path) so this test runs immediately rather than waiting out the
     * full implied-wait timeout for a condition that can never succeed.
     */
    @Test
    public void acquireReference_onAbsentOptionalParent_throwsNoSuchElementException() {
        WrapsContext mockContext = mock(WrapsContext.class);
        when(mockContext.hasReference()).thenReturn(false);
        when(mockContext.getWrappedContext()).thenReturn(null);
        when(mockContext.getWrappedDriver()).thenReturn(mock(WebDriver.class));

        // supply a non-null placeholder element so the constructor doesn't itself attempt resolution
        WebElement placeholder = mock(WebElement.class);
        RobustElementWrapper wrapper =
                new RobustElementWrapper(placeholder, mockContext, By.id("child"), RobustElementWrapper.CARDINAL);

        try {
            RobustElementWrapper.acquireReference(wrapper);
            fail("Expected NoSuchElementException for absent parent context");
        } catch (NoSuchElementException e) {
            assertTrue(e.getMessage().contains("absent optional element"),
                    "Exception message should identify the absent parent context");
        }

        // the placeholder supplied at construction must be discarded, not left dangling as a stale reference
        assertNull(wrapper.getWrapped(), "Wrapped reference should be cleared after a failed acquisition");
    }

    /**
     * Sanity/regression check: when the parent context is present, ordinary acquisition still succeeds, and -
     * critically - resolves to the exact element the native search returned. Verifying only that
     * {@code findElement} was called (without checking what became of its result) would still pass a
     * regression where the wrapper ends up holding the wrong element, a stale one, or none at all. This test
     * distinguishes the correct child from a decoy the driver also happens to expose, so a mismatch or an
     * index/assignment bug would be caught, not just an outright crash.
     */
    @Test
    public void findChild_onPresentParent_resolvesToTheMatchedElement() {
        By locator = By.id("child");
        WebElement decoyElement = mock(WebElement.class);
        WebElement nativeChild = mock(WebElement.class);
        SearchContext nativeContext = mock(SearchContext.class);
        // a decoy is registered for a different locator so a wrapper that searched (or matched) incorrectly
        // would be caught by identity, not just by "something non-null came back"
        when(nativeContext.findElement(By.id("decoy"))).thenReturn(decoyElement);
        when(nativeContext.findElement(locator)).thenReturn(nativeChild);

        WebDriver mockDriver = mock(WebDriver.class);
        WebDriver.Options mockOptions = mock(WebDriver.Options.class);
        WebDriver.Timeouts mockTimeouts = mock(WebDriver.Timeouts.class);
        when(mockDriver.manage()).thenReturn(mockOptions);
        when(mockOptions.timeouts()).thenReturn(mockTimeouts);

        WrapsContext mockContext = mock(WrapsContext.class,
                withSettings().extraInterfaces(SearchContext.class));
        when(mockContext.hasReference()).thenReturn(true);
        when(mockContext.getWrappedContext()).thenReturn(nativeContext);
        when(mockContext.getWrappedDriver()).thenReturn(mockDriver);

        RobustElementWrapper wrapper =
                new RobustElementWrapper(null, mockContext, locator, RobustElementWrapper.CARDINAL);

        verify(nativeContext).findElement(locator);
        assertSame(nativeChild, wrapper.getWrappedElement(),
                "Wrapper should hold exactly the element the native search matched, not the decoy or none");
    }
}
