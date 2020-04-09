package com.nordstrom.automation.selenium.platform;

public enum ExamplePlatform implements PlatformEnum {
    PLATFORM_ONE,
    PLATFORM_TWO;
    
    private String name;

    public static final String PLATFORM_ONE_NAME = "platform_one";
    public static final String PLATFORM_TWO_NAME = "platform_two";
    
    static {
        PLATFORM_ONE.name = PLATFORM_ONE_NAME;
        PLATFORM_TWO.name = PLATFORM_TWO_NAME;
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
}
