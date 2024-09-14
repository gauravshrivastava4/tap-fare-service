package com.littlepay.tapfare.utils;

import com.littlepay.tapfare.config.TripsFareConfig;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Getter
public class FareMatrixUtils {

    public static final Map<String, Map<String, Double>> fareMatrix = new HashMap<>();

    public FareMatrixUtils(final TripsFareConfig tripsFareConfig) {
        populateFareMatrix(tripsFareConfig);
    }

    private void populateFareMatrix(final TripsFareConfig tripsFareConfig) {
        tripsFareConfig.getFare().forEach((fromStop, toMap) -> {
            toMap.forEach((toStop, fare) -> addFare(fromStop, toStop, fare));
        });
    }

    private void addFare(final String stopA, final String stopB, final double fare) {
        fareMatrix.computeIfAbsent(stopA, k -> new HashMap<>()).put(stopB, fare);
        fareMatrix.computeIfAbsent(stopB, k -> new HashMap<>()).put(stopA, fare);
    }
}
