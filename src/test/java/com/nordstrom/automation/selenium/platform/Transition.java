package com.nordstrom.automation.selenium.platform;

public enum Transition implements PhaseName {
    PHASE1("green", PHASE1_NAME),
    PHASE2("amber", PHASE2_NAME),
    PHASE3("coral", PHASE3_NAME);
    
    private String color;
    private String name;
    
    Transition(String color, String name) {
        this.color = color;
        this.name = name;
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
