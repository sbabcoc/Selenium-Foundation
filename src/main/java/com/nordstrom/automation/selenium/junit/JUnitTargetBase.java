package com.nordstrom.automation.selenium.junit;

import com.nordstrom.automation.selenium.platform.TargetType;

public class JUnitTargetBase extends JUnitPlatformBase<TargetType> {

    public JUnitTargetBase() {
        super(TargetType.class);
    }

    @Override
    public TargetType getDefaultPlatform() {
        return TargetType.SUPPORT;
    }

}
