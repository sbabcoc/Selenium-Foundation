package com.nordstrom.automation.selenium.jupiter;

/**
 * Minimal concrete {@link JupiterBase} subclass used only to obtain a real {@code TestBase} instance
 * for driver-storage - {@code DriverManager.nabDriver(Object)} requires {@code instanceof TestBase},
 * and {@code JupiterBase} already implements the storage side correctly (plain field, safe for
 * PER_METHOD). No test methods of its own - these tests call PageSourceArtifact/ScreenshotArtifact
 * directly against an instance of this class, never through the Jupiter engine.
 */
class ArtifactTestFixture extends JupiterBase {
    // intentionally empty
}
