package com.nordstrom.automation.selenium.support;

import com.nordstrom.automation.selenium.platform.TargetType;

public class TestNgTargetBase extends TestNgPlatformBase<TargetType> {

    public TestNgTargetBase() {
        super(TargetType.class);
    }

    @Override
    public TargetType getDefaultPlatform() {
        return TargetType.SUPPORT;
    }

}
