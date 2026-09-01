package com.nordstrom.automation.selenium.jupiter;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

/**
 * Direct unit tests for {@link ScreenshotArtifact} - same style as {@code PageSourceArtifactTest}.
 * <p>
 * <b>NOTE</b>: {@code ScreenshotUtils.canGetArtifact(...)} defaults to {@code false} for an absent
 * driver, whereas {@code PageSourceUtils.canGetArtifact(...)} defaults to {@code true} for the same
 * case - a real asymmetry between the two utilities, confirmed by tracing both implementations, not
 * something either {@code ArtifactType} overrides. Both still resolve {@code getArtifact(...)} to an
 * empty byte array either way, just via different internal paths.
 */
class ScreenshotArtifactTest {

    private interface MockableDriver extends WebDriver, TakesScreenshot, HasCapabilities { }

    private final ScreenshotArtifact artifact = new ScreenshotArtifact();

    @Test
    void capableSessionProducesScreenshot() {
        MockableDriver driver = mock(MockableDriver.class);
        Capabilities caps = mock(Capabilities.class);
        when(driver.getCapabilities()).thenReturn(caps);
        when(caps.getCapability("takesScreenshot")).thenReturn(Boolean.TRUE);
        byte[] fakePng = {(byte) 0x89, 'P', 'N', 'G'};
        when(driver.getScreenshotAs(OutputType.BYTES)).thenReturn(fakePng);

        ArtifactTestFixture fixture = new ArtifactTestFixture();
        fixture.setDriver(driver);

        assertTrue(artifact.canGetArtifact(fixture));
        assertArrayEquals(fakePng, artifact.getArtifact(fixture, null));
    }

    @Test
    void deadSessionReturnsEmptyArtifact() {
        MockableDriver driver = mock(MockableDriver.class);
        when(driver.getCapabilities()).thenThrow(new WebDriverException("session terminated"));

        ArtifactTestFixture fixture = new ArtifactTestFixture();
        fixture.setDriver(driver);

        assertFalse(artifact.canGetArtifact(fixture));
        assertArrayEquals(new byte[0], artifact.getArtifact(fixture, null));
    }

    @Test
    void noDriverPresentReportsUnableToCapture() {
        // no setDriver(...) call - nabDriver() returns Optional.empty()
        ArtifactTestFixture fixture = new ArtifactTestFixture();

        // unlike PageSourceArtifact, ScreenshotArtifact's canGetArtifact is FALSE here - the more
        // conservative of the two utilities' defaults for an absent driver
        assertFalse(artifact.canGetArtifact(fixture));
        assertArrayEquals(new byte[0], artifact.getArtifact(fixture, null));
    }

    @Test
    void artifactExtensionIsPng() {
        assertEquals("png", artifact.getArtifactExtension());
    }
}
