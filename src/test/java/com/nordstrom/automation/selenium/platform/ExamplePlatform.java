package com.nordstrom.automation.selenium.platform;

public enum ExamplePlatform implements PlatformEnum {
    PLATFORM_ONE,
    PLATFORM_TWO;
    
    private String name;

    public static final String PLATFORM_ONE_ = "platform_one";
    public static final String PLATFORM_TWO_ = "platform_two";
    
    static {
        PLATFORM_ONE.name = PLATFORM_ONE_;
        PLATFORM_TWO.name = PLATFORM_TWO_;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getName() {
        return name;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matches(String contextPlatform) {
        return name.equals(contextPlatform);
    }
    
    public static ExamplePlatform fromString(String name) {
        for (ExamplePlatform platform : values()) {
            if (platform.name.equals(name)) {
                return platform;
            }
        }
        return null;
    }

}
