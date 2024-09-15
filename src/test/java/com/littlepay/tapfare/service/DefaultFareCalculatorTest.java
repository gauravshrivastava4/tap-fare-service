package com.littlepay.tapfare.service;

import com.littlepay.tapfare.config.TripsFareConfig;
import com.littlepay.tapfare.utils.FareMatrixUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultFareCalculatorTest {

    private DefaultFareCalculator defaultFareCalculator;

    @Mock
    TripsFareConfig tripsFareConfig;

    @BeforeEach
    void setUp() {

        defaultFareCalculator = new DefaultFareCalculator();

        final Map<String, Map<String, Double>> mockFareConfig = new HashMap<>();

        final Map<String, Double> stop1Fares = new HashMap<>();
        stop1Fares.put("Stop2", 3.25);
        stop1Fares.put("Stop3", 7.00);

        final Map<String, Double> stop2Fares = new HashMap<>();
        stop2Fares.put("Stop1", 3.25);
        stop2Fares.put("Stop3", 5.50);

        mockFareConfig.put("Stop1", stop1Fares);
        mockFareConfig.put("Stop2", stop2Fares);

        when(tripsFareConfig.getFare()).thenReturn(mockFareConfig);

        new FareMatrixUtils(tripsFareConfig);
    }

    @Test
    void testCalculateFare() {
        double fare = defaultFareCalculator.calculateFare("Stop1", "Stop2");
        assertThat(fare).isEqualTo(3.25);

        fare = defaultFareCalculator.calculateFare("Stop2", "Stop3");
        assertThat(fare).isEqualTo(5.50);

        fare = defaultFareCalculator.calculateFare("Stop1", "Stop4");
        assertThat(fare).isEqualTo(0.0);
    }

    @Test
    void testCalculateMaxFare() {
        double maxFare = defaultFareCalculator.calculateMaxFare("Stop1");
        assertThat(maxFare).isEqualTo(7.00);

        maxFare = defaultFareCalculator.calculateMaxFare("Stop2");
        assertThat(maxFare).isEqualTo(5.50);

        maxFare = defaultFareCalculator.calculateMaxFare("Stop4");
        assertThat(maxFare).isEqualTo(0.0);
    }
}

