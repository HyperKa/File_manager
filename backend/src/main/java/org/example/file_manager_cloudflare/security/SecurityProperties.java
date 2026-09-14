package org.example.file_manager_cloudflare.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.UUID;

@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    private String apiToken;
    private UUID defaultUserId;

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public UUID getDefaultUserId() {
        return defaultUserId;
    }

    public void setDefaultUserId(UUID defaultUserId) {
        this.defaultUserId = defaultUserId;
    }
}
