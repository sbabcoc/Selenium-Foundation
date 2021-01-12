package com.nordstrom.automation.selenium.platform;

public enum ExamplePlatform implements PlatformEnum {
    PHASE1("green"),
    PHASE2("amber"),
    PHASE3("coral");
    
    private String color;
    private String name;
    
    ExamplePlatform(String color) {
        this.color = color;
    }

    public static final String PHASE1_NAME = "phase-1";
    public static final String PHASE2_NAME = "phase-2";
    public static final String PHASE3_NAME = "phase-3";
    
    static {
        PHASE1.name = PHASE1_NAME;
        PHASE2.name = PHASE2_NAME;
        PHASE3.name = PHASE3_NAME;
    }
    
    public String getColor() {
        return color;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public boolean matches(String contextPlatform) {
        return name.equals(contextPlatform);
    }
}
