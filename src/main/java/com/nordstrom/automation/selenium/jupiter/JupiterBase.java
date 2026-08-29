package com.nordstrom.automation.selenium.jupiter;

import java.util.Optional;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.WebDriver;

import com.nordstrom.automation.selenium.model.Page;

/**
 * Base class for Selenium Foundation Jupiter tests using the default ({@code PER_METHOD}) test-instance
 * lifecycle. Storage is a plain field: a fresh instance is created for every test method, so no
 * concurrent access against a single instance's fields is possible, regardless of parallel execution
 * configuration.
 */
@TestInstance(Lifecycle.PER_METHOD)
public abstract class JupiterBase extends JupiterTestBase {

    private WebDriver driver;
    private Page initialPage;
    private ExtensionContext currentContext;

    @Override
    public Optional<WebDriver> nabDriver() {
        return Optional.ofNullable(driver);
    }

    @Override
    public void setDriver(final WebDriver driver) {
        this.driver = driver;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Page> Optional<T> nabInitialPage() {
        return Optional.ofNullable((T) initialPage);
    }

    @Override
    public <T extends Page> void setInitialPage(final T initialPage) {
        this.initialPage = initialPage;
    }

    @Override
    protected ExtensionContext getExtensionContext() {
        return currentContext;
    }

    @Override
    void setExtensionContext(final ExtensionContext context) {
        this.currentContext = context;
    }

    @Override
    void clearExtensionContext() {
        this.currentContext = null;
    }
}
