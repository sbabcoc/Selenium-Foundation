package com.nordstrom.automation.selenium.junit;

import com.nordstrom.automation.selenium.platform.TargetType;

/**
 * This class is a concrete subclass of {@link JUnitPlatformBase} specifying {@link TargetType} as the platform.
 */
public class JUnitTargetBase extends JUnitPlatformBase<TargetType> {

    /**
     * Constructor for <b>JUnit</b> tests classes that support the {@link TargetType} platform.
     */
    public JUnitTargetBase() {
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
