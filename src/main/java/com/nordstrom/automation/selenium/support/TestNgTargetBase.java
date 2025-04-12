package com.nordstrom.automation.selenium.support;

import com.nordstrom.automation.selenium.platform.TargetType;

/**
 * This class is a concrete subclass of {@link TestNgPlatformBase} specifying {@link TargetType} as the platform.
 */
public class TestNgTargetBase extends TestNgPlatformBase<TargetType> {

    /**
     * Constructor for <b>TestNG</b> tests classes that support the {@link TargetType} platform.
     */
    public TestNgTargetBase() {
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
