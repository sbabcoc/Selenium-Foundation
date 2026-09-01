package com.nordstrom.automation.selenium.jupiter;

import com.nordstrom.automation.selenium.platform.TargetType;

/**
 * This class is a concrete subclass of {@link JupiterPlatformBase} specifying {@link TargetType} as the platform.
 */
public class JupiterTargetBase extends JupiterPlatformBase<TargetType> {

    /**
     * Constructor for <b>JUnit</b> tests classes that support the {@link TargetType} platform.
     */
    public JupiterTargetBase() {
        super(TargetType.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TargetType getDefaultPlatform() {
        return TargetType.SUPPORT;
    }

}
