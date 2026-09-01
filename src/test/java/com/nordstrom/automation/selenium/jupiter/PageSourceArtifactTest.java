package com.nordstrom.automation.selenium.jupiter;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

/**
 * Direct unit tests for {@link PageSourceArtifact} - no Jupiter engine involved. A bare
 * {@link ArtifactTestFixture} instance provides the real {@code TestBase} that
 * {@code DriverManager.nabDriver(Object)} requires; the driver itself is a Mockito mock.
 */
class PageSourceArtifactTest {

    /** Combined interface so a single mock satisfies both the driver-presence and capability checks. */
    private interface MockableDriver extends WebDriver, HasCapabilities { }

    private final PageSourceArtifact artifact = new PageSourceArtifact();

    @Test
    void capableSessionProducesPageSource() {
        MockableDriver driver = mock(MockableDriver.class);
        // required: PageSourceUtils.canGetArtifact(...) treats an unstubbed (null) getCapabilities()
        // result identically to a dead session - it can't distinguish "never stubbed" from "genuinely
        // returned nothing", so a capable-session test must stub this explicitly
        when(driver.getCapabilities()).thenReturn(mock(Capabilities.class));
        when(driver.getPageSource()).thenReturn("<html><head></head><body>hi</body></html>");
        when(driver.getCurrentUrl()).thenReturn("about:blank");

        ArtifactTestFixture fixture = new ArtifactTestFixture();
        fixture.setDriver(driver);

        assertTrue(artifact.canGetArtifact(fixture));

        byte[] result = artifact.getArtifact(fixture, null);
        String content = new String(result, StandardCharsets.UTF_8);
        assertTrue(content.contains("hi"), "captured source should contain the original page content");
    }

    @Test
    void deadSessionReturnsEmptyArtifact() {
        MockableDriver driver = mock(MockableDriver.class);
        when(driver.getCapabilities()).thenThrow(new WebDriverException("session terminated"));

        ArtifactTestFixture fixture = new ArtifactTestFixture();
        fixture.setDriver(driver);

        assertFalse(artifact.canGetArtifact(fixture),
                "a driver whose session is dead should report itself as unable to capture");
        assertArrayEquals(new byte[0], artifact.getArtifact(fixture, null));
    }

    @Test
    void noDriverPresentReturnsEmptyArtifactGracefully() {
        // no setDriver(...) call at all - nabDriver() returns Optional.empty()
        ArtifactTestFixture fixture = new ArtifactTestFixture();

        // canGetArtifact's underlying PageSourceUtils check is permissive for an absent driver
        // (returns true rather than false) - this is the utility's own documented fallback, not
        // something PageSourceArtifact overrides
        assertTrue(artifact.canGetArtifact(fixture));

        // getArtifact still resolves this safely: DriverManager.nabDriver(instance).map(...) never
        // invokes PageSourceUtils.getArtifact at all when the Optional is empty, short-circuiting
        // straight to an empty byte array rather than ever calling Optional.get() on nothing
        assertArrayEquals(new byte[0], artifact.getArtifact(fixture, null));
    }

    @Test
    void artifactExtensionDefaultsToHtml() {
        assertEquals("html", artifact.getArtifactExtension());
    }
}
