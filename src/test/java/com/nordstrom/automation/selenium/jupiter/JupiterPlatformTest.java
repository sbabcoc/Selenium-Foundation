package com.nordstrom.automation.selenium.jupiter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

import com.nordstrom.automation.selenium.annotations.NoDriver;
import com.nordstrom.automation.selenium.exceptions.PlatformActivationFailedException;
import com.nordstrom.automation.selenium.platform.TargetPlatform;
import com.nordstrom.automation.selenium.platform.Transition;

/**
 * Jupiter-native equivalent of {@code JUnitPlatformTest}.
 * <p>
 * <b>NOTE</b>: {@code JUnitPlatformTest}'s {@code activatePlatform} override determines the currently
 * running test method via {@code LifecycleHooks.getAtomicTestOf(this)} - a JUnit-Foundation/Byte Buddy
 * mechanism with no Jupiter equivalent. {@code getExtensionContext().getRequiredTestMethod()} (already
 * built into {@code JupiterTestBase} for exactly this class of need) replaces it directly, natively.
 */
public class JupiterPlatformTest extends JupiterPlatformBase<Transition> {

    public JupiterPlatformTest() {
        super(Transition.class);
    }

    @Test
    @NoDriver
    public void testDefaultPlatform() {
        assertTrue(getTargetPlatform().matches(Transition.PHASE1_NAME));
        assertEquals("green", getTargetPlatform().getColor());
    }

    @Test
    @NoDriver
    @TargetPlatform(Transition.PHASE2_NAME)
    public void testPlatformTwo() {
        assertTrue(getTargetPlatform().matches(Transition.PHASE2_NAME));
        assertEquals("amber", getTargetPlatform().getColor());
    }

    @Override
    public void activatePlatform(final WebDriver driver, final Transition platform)
            throws PlatformActivationFailedException {

        String methodName = getExtensionContext().getRequiredTestMethod().getName();

        Transition expected;
        switch (methodName) {
        case "testDefaultPlatform":
            expected = Transition.PHASE1;
            break;
        case "testPlatformTwo":
            expected = Transition.PHASE2;
            break;
        default:
            throw new RuntimeException("Unexpected method: " + methodName);
        }

        if (platform != expected) {
            throw new PlatformActivationFailedException(platform, "expected: " + expected.getName());
        }

        // perform some platform-related activation
        System.setProperty("platform.phase.color", platform.getColor());
    }

    @Override
    public Transition getDefaultPlatform() {
        return Transition.PHASE1;
    }
}
