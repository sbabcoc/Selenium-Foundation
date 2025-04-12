package com.nordstrom.automation.selenium.junit;

import com.nordstrom.automation.junit.ShutdownListener;
import com.nordstrom.automation.selenium.core.DriverManager;

/**
 * This class implements a driver shutdown listener.
 */
public class DriverListener implements ShutdownListener {

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShutdown() {
        DriverManager.onFinish();
    }

}
