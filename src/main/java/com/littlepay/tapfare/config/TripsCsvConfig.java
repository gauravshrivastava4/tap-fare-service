package com.littlepay.tapfare.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("trips.csv")
public class TripsCsvConfig {
    private final String inputFilePath;
    private final String outputFilePath;
}
