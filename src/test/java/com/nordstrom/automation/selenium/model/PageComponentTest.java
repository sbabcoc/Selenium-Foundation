package com.nordstrom.automation.selenium.model;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link PageComponent#hasReference()} (selenium-foundation issue #192) - a component must
 * accurately report whether its own root/context element is currently resolvable, delegating directly to that
 * element rather than to {@link PageComponent#getViewport()}, which may point to a different, nested element.
 * <p>
 * Each test also verifies the root element's {@code hasReference()} was actually consulted, so a stub that
 * happened to return the expected boolean for an unrelated reason (or a hard-coded return in the production
 * code) would be caught rather than passing by coincidence.
 */
public class PageComponentTest {

    @Test
    public void hasReference_delegatesToRootElement_whenAbsent() {
        RobustWebElement mockRoot = mock(RobustWebElement.class);
        when(mockRoot.hasReference()).thenReturn(false);
        when(mockRoot.getWrappedDriver()).thenReturn(mock(WebDriver.class));

        ComponentContainer mockParent = mock(ComponentContainer.class);

        PageComponent component = new PageComponent(mockRoot, mockParent);

        assertFalse(component.hasReference(), "Component should report absence when its root element is absent");
        verify(mockRoot).hasReference();
    }

    @Test
    public void hasReference_delegatesToRootElement_whenPresent() {
        RobustWebElement mockRoot = mock(RobustWebElement.class);
        when(mockRoot.hasReference()).thenReturn(true);
        when(mockRoot.getWrappedDriver()).thenReturn(mock(WebDriver.class));

        ComponentContainer mockParent = mock(ComponentContainer.class);

        PageComponent component = new PageComponent(mockRoot, mockParent);

        assertTrue(component.hasReference(), "Component should report presence when its root element is present");
        verify(mockRoot).hasReference();
    }
}
