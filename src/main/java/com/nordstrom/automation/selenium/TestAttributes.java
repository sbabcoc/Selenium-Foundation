package com.nordstrom.automation.selenium;

import java.util.Objects;
import java.util.Optional;

import org.openqa.grid.common.GridRole;
import org.openqa.selenium.WebDriver;

import com.nordstrom.automation.selenium.exceptions.DriverNotAvailableException;
import com.nordstrom.automation.selenium.exceptions.InitialPageNotSpecifiedException;
import com.nordstrom.automation.selenium.model.Page;

public class TestAttributes {
    
    private Process hubProcess;
    private Process nodeProcess;
    private Optional<SeleniumConfig> config = Optional.empty();
    private Optional<WebDriver> driver = Optional.empty();
    private Optional<Page> initialPage = Optional.empty();
    private boolean closeDriver;
    
    private static final ThreadLocal<TestAttributes> attributes = new InheritableThreadLocal<TestAttributes>() {
        @Override
        protected TestAttributes initialValue() {
            return new TestAttributes();
        }
    };
    
    public static TestAttributes getAttributes() {
        return attributes.get();
    }
    
    public void setProcess(GridRole processRole, Process process) {
        switch (processRole) {
            case HUB:
                setHubProcess(process);
                break;
                
            case NODE:
                setNodeProcess(process);
                break;
                
            default:
                throw new UnsupportedOperationException();
        }
    }

    public Process getHubProcess() {
        return hubProcess;
    }

    void setHubProcess(Process hubProcess) {
        this.hubProcess = hubProcess;
    }

    public Process getNodeProcess() {
        return nodeProcess;
    }

    void setNodeProcess(Process nodeProcess) {
        this.nodeProcess = nodeProcess;
    }

    public Optional<SeleniumConfig> getConfig() {
        return config;
    }

    Optional<SeleniumConfig> setConfig(SeleniumConfig seleniumConfig) {
        Objects.requireNonNull(seleniumConfig);
        config = Optional.of(seleniumConfig);
        return config;
    }

    public WebDriver getDriver() {
        if (driver.isPresent()) {
            return driver.get();
        }
        throw new DriverNotAvailableException();
    }
    
    public Optional<WebDriver> findDriver() {
        return driver;
    }

    public Optional<WebDriver> setDriver(WebDriver driver) {
        if (driver != null) {
            this.driver = Optional.of(driver);
        } else {
            this.driver = Optional.empty();
        }
        return this.driver;
    }

    public Page getInitialPage() {
        if (initialPage.isPresent()) {
            return initialPage.get();
        }
        throw new InitialPageNotSpecifiedException();
    }

    public boolean hasInitialPage() {
        return initialPage.isPresent();
    }

    public void setInitialPage(Page initialPage) {
        this.initialPage = Optional.of(initialPage);
    }
    
    public boolean doCloseDriver() {
        boolean result = closeDriver;
        closeDriver = false;
        return result;
    }

    public void setCloseDriver() {
        closeDriver = true;
    }

}
