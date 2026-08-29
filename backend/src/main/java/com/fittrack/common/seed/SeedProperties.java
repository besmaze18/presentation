package com.fittrack.common.seed;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fittrack.seed")
public class SeedProperties {

    /** Off by default. Turning it on creates a demo account with sample history. */
    private boolean enabled = false;

    private String email = "demo@fittrack.local";

    /** Required when seeding is enabled; no default, so no known password ever ships. */
    private String password;

    private int days = 30;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }
}
