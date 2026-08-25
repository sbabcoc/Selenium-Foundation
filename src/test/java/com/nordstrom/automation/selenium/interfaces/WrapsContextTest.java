package com.nordstrom.automation.selenium.interfaces;

import static org.testng.Assert.assertTrue;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

/**
 * Unit test for {@link WrapsContext#hasReference()}'s default implementation (selenium-foundation issue #192).
 * Every implementation that doesn't wrap an optional element/component - i.e. every implementation that
 * doesn't override this method - must report itself as always present.
 */
public class WrapsContextTest {

    @Test
    public void hasReference_defaultImplementation_returnsTrue() {
        WrapsContext context = new WrapsContext() {
            @Override
            public SearchContext switchTo() {
                return null;
            }

            @Override
            public SearchContext switchToParentFrame() {
                return null;
            }

            @Override
            public SearchContext getWrappedContext() {
                return null;
            }

            @Override
            public SearchContext refreshContext(final long expiration) {
                return null;
            }

            @Override
            public long acquiredAt() {
                return 0;
            }

            @Override
            public WebDriver getWrappedDriver() {
                return null;
            }
        };

        assertTrue(context.hasReference(), "Default hasReference() implementation must return true");
    }
}
