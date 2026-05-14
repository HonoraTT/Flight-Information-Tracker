package com.example.flightinformationtracker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 国内模拟航班配置。
 */
@Component
@ConfigurationProperties(prefix = "mock.china-flights")
public class MockChinaFlightConfig {
    private boolean enabled = true;
    private int count = 500;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
