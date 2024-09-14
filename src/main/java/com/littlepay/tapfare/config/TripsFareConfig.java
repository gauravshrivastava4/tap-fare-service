package com.littlepay.tapfare.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties("trips")
public class TripsFareConfig {

    private Map<String, Map<String, Double>> fare;

    public Map<String, Map<String, Double>> getFare() {
        return fare;
    }

    public void setFare(final Map<String, Map<String, Double>> fare) {
        this.fare = fare;
    }
}
