package com.example.flightinformationtracker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * OpenSky 相关配置。
 */
@Configuration
@ConfigurationProperties(prefix = "opensky")
public class OpenSkyConfig {
    private String clientId;
    private String clientSecret;
    private String authUrl;
    private String apiUrl;
    private boolean filterOnGround = true;
    private int retryMaxAttempts = 3;
    private int retryIntervalSeconds = 3;
    private int tokenRefreshAdvanceSeconds = 300;
    private int rateLimitPauseSeconds = 300;
    private Long pollingIntervalSeconds;
    private final Bbox bbox = new Bbox();

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getAuthUrl() {
        return authUrl;
    }

    public void setAuthUrl(String authUrl) {
        this.authUrl = authUrl;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public boolean isFilterOnGround() {
        return filterOnGround;
    }

    public void setFilterOnGround(boolean filterOnGround) {
        this.filterOnGround = filterOnGround;
    }

    public int getRetryMaxAttempts() {
        return retryMaxAttempts;
    }

    public void setRetryMaxAttempts(int retryMaxAttempts) {
        this.retryMaxAttempts = retryMaxAttempts;
    }

    public int getRetryIntervalSeconds() {
        return retryIntervalSeconds;
    }

    public void setRetryIntervalSeconds(int retryIntervalSeconds) {
        this.retryIntervalSeconds = retryIntervalSeconds;
    }

    public int getTokenRefreshAdvanceSeconds() {
        return tokenRefreshAdvanceSeconds;
    }

    public void setTokenRefreshAdvanceSeconds(int tokenRefreshAdvanceSeconds) {
        this.tokenRefreshAdvanceSeconds = tokenRefreshAdvanceSeconds;
    }

    public int getRateLimitPauseSeconds() {
        return rateLimitPauseSeconds;
    }

    public void setRateLimitPauseSeconds(int rateLimitPauseSeconds) {
        this.rateLimitPauseSeconds = rateLimitPauseSeconds;
    }

    public Long getPollingIntervalSeconds() {
        return pollingIntervalSeconds;
    }

    public void setPollingIntervalSeconds(Long pollingIntervalSeconds) {
        this.pollingIntervalSeconds = pollingIntervalSeconds;
    }

    public Bbox getBbox() {
        return bbox;
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder().baseUrl(openSkyConfigBaseUrl());
    }

    private String openSkyConfigBaseUrl() {
        String base = apiUrl == null ? "https://opensky-network.org/api" : apiUrl.trim();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base;
    }

    public static class Bbox {
        private double lamin = 18.0;
        private double lamax = 55.0;
        private double lomin = 70.0;
        private double lomax = 140.0;

        public double getLamin() {
            return lamin;
        }

        public void setLamin(double lamin) {
            this.lamin = lamin;
        }

        public double getLamax() {
            return lamax;
        }

        public void setLamax(double lamax) {
            this.lamax = lamax;
        }

        public double getLomin() {
            return lomin;
        }

        public void setLomin(double lomin) {
            this.lomin = lomin;
        }

        public double getLomax() {
            return lomax;
        }

        public void setLomax(double lomax) {
            this.lomax = lomax;
        }
    }
}
