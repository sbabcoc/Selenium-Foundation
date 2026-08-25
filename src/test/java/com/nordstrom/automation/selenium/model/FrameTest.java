package com.nordstrom.automation.selenium.model;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import com.nordstrom.automation.selenium.exceptions.OptionalElementNotAcquiredException;

/**
 * Unit test for {@link Frame#switchToContext()}: any operation that requires the frame context must fail
 * cleanly when the frame's own context element is an absent optional element, rather than handing an
 * unresolved element straight to the driver's frame-switch command.
 * <p>
 * The complementary "present element" scenario is already covered - more meaningfully than a mock could -
 * by {@code ModelTestCore.testFrameByElement()}, which switches into a real iframe against the example site
 * and asserts on the frame's actual heading text ({@code FRAME_B}), proving the driver switched into the
 * correct frame rather than merely that some {@code frame(WebElement)} call was made. That test exercises
 * only the unmodified, working half of {@code switchToContext()}, so it needs no changes here. There's no
 * real-content analog for the absent case (there's nothing to read), so the meaningful assertions here are
 * the failure's exact cause and the absence of any side effect on the driver.
 */
public class FrameTest {

    @Test
    public void switchToContext_withAbsentElement_throwsOptionalElementNotAcquired() {
        WebDriver mockDriver = mock(WebDriver.class);
        Page parentPage = new Page(mockDriver);

        RobustWebElement mockElement = mock(RobustWebElement.class);
        when(mockElement.hasReference()).thenReturn(false);
        when(mockElement.getIndex()).thenReturn(RobustElementWrapper.CARDINAL);

        Frame frame = new Frame(mockElement, parentPage);

        try {
            frame.switchToContext();
            fail("Expected OptionalElementNotAcquiredException for absent context element");
        } catch (OptionalElementNotAcquiredException e) {
            // OptionalElementNotAcquiredException's own message is a fixed, generic string - the specific
            // cause lives in getCause(), which is what Frame.switchToContext() actually sets
            Throwable cause = e.getCause();
            assertTrue(cause != null && cause.getMessage() != null
                            && cause.getMessage().contains("context element is absent"),
                    "Exception cause should identify the absent context element; got: "
                            + (cause == null ? "null" : cause.getMessage()));
        }

        // the guard must short-circuit before the driver is touched at all - not merely throw the expected
        // exception type for some other, coincidental reason after already having (mis)used the driver
        verify(mockDriver, never()).switchTo();
    }
}
