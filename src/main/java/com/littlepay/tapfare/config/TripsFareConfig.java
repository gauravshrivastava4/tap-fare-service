package com.littlepay.tapfare.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Data
@ConfigurationProperties("trips")
public class TripsFareConfig {

    private Map<String, Map<String, Double>> fare;

}
