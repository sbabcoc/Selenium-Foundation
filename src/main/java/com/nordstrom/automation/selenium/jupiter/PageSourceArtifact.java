package com.nordstrom.automation.selenium.jupiter;

import java.nio.file.Path;
import java.util.Optional;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nordstrom.automation.jupiter.ArtifactType;
import com.nordstrom.automation.selenium.core.DriverManager;
import com.nordstrom.automation.selenium.utility.PageSourceUtils;

/**
 * Jupiter-native counterpart to the JUnit 4 {@code PageSourceArtifact}. Only the extended
 * {@code ArtifactType} changes (JUnit-Foundation's &rarr; Jupiter Foundation's); the capture logic itself —
 * {@code DriverManager.nabDriver(instance)} + {@code PageSourceUtils} — is copied unchanged, since none
 * of it ever depended on JUnit 4 in the first place.
 */
public class PageSourceArtifact extends ArtifactType {

    private static final String ARTIFACT_PATH = "page-source";
    private static final Logger LOGGER = LoggerFactory.getLogger(PageSourceArtifact.class);

    private String memoizedExtension;

    @Override
    public boolean canGetArtifact(final Object instance) {
        Optional<WebDriver> optDriver = DriverManager.nabDriver(instance);
        return PageSourceUtils.canGetArtifact(optDriver, LOGGER);
    }

    @Override
    public byte[] getArtifact(final Object instance, final Throwable reason) {
        memoizedExtension = null;

        Optional<byte[]> optArtifact = DriverManager.nabDriver(instance)
                .map(driver -> PageSourceUtils.getArtifact(Optional.of(driver), reason, LOGGER));

        return optArtifact
                .filter(bytes -> bytes.length > 0)
                .map(bytes -> {
                    memoizedExtension = PageSourceUtils.isXml(bytes) ? "xml" : "html";
                    return bytes;
                })
                .orElse(new byte[0]);
    }

    @Override
    public Path getArtifactPath(final Object instance) {
        return super.getArtifactPath(instance).resolve(ARTIFACT_PATH);
    }

    @Override
    public String getArtifactExtension() {
        return Optional.ofNullable(memoizedExtension).orElse("html");
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }
}
