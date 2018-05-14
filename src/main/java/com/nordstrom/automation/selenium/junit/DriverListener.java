package com.nordstrom.automation.selenium.junit;

import com.nordstrom.automation.junit.ShutdownListener;
import com.nordstrom.automation.selenium.core.DriverManager;

public class DriverListener implements ShutdownListener {

    @Override
    public void onShutdown() {
        DriverManager.onFinish();
    }

}
