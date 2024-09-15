package com.littlepay.tapfare.utils;

import com.littlepay.tapfare.config.TripsFareConfig;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for managing and accessing fare information between multiple stops.
 * This class provides a fare matrix which is initialized from a configuration and
 * allows efficient retrieval of fare amounts for trips between any two stops.
 */
@Component
public class FareMatrixUtils {

    /**
     * A static map representing the fare matrix for trips between various stops.
     * The outer map's keys are the starting stop names, and the values are nested maps.
     * The nested maps have destination stop names as keys and fare amounts as values.
     * This matrix provides quick access to the fare for any trip from one stop to another.
     */
    @Getter
    private static final Map<String, Map<String, Double>> fareMatrix = new HashMap<>();

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
