package com.nordstrom.automation.selenium.jupiter;

import java.nio.file.Path;
import java.util.Optional;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nordstrom.automation.jupiter.ArtifactType;
import com.nordstrom.automation.selenium.core.DriverManager;
import com.nordstrom.automation.selenium.utility.ScreenshotUtils;

/**
 * Jupiter-native counterpart to the JUnit 4 {@code ScreenshotArtifact} — same thin-wrapper swap as
 * {@link PageSourceArtifact}.
 */
public class ScreenshotArtifact extends ArtifactType {

    private static final String ARTIFACT_PATH = "screenshots";
    private static final String EXTENSION = "png";
    private static final Logger LOGGER = LoggerFactory.getLogger(ScreenshotArtifact.class);

    @Override
    public boolean canGetArtifact(final Object instance) {
        Optional<WebDriver> optDriver = DriverManager.nabDriver(instance);
        return ScreenshotUtils.canGetArtifact(optDriver, LOGGER);
    }

    @Override
    public byte[] getArtifact(final Object instance, final Throwable reason) {
        Optional<WebDriver> optDriver = DriverManager.nabDriver(instance);
        return ScreenshotUtils.getArtifact(optDriver, reason, LOGGER);
    }

    @Override
    public Path getArtifactPath(final Object instance) {
        return super.getArtifactPath(instance).resolve(ARTIFACT_PATH);
    }

    @Override
    public String getArtifactExtension() {
        return EXTENSION;
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }
}
