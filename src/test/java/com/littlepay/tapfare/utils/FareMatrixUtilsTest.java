package com.littlepay.tapfare.utils;

import com.littlepay.tapfare.config.TripsFareConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class FareMatrixUtilsTest {

    private TripsFareConfig tripsFareConfig;
    private FareMatrixUtils fareMatrixUtils;

    @BeforeEach
    void setUp() {
        tripsFareConfig = Mockito.mock(TripsFareConfig.class);

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

        fareMatrixUtils = new FareMatrixUtils(tripsFareConfig);
    }

    @Test
    void testPopulateFareMatrix() {
        // Check fares for Stop1
        assertThat(FareMatrixUtils.fareMatrix).containsKey("Stop1");
        assertThat(FareMatrixUtils.fareMatrix.get("Stop1")).containsEntry("Stop2", 3.25);
        assertThat(FareMatrixUtils.fareMatrix.get("Stop1")).containsEntry("Stop3", 7.00);

        // Check fares for Stop2
        assertThat(FareMatrixUtils.fareMatrix).containsKey("Stop2");
        assertThat(FareMatrixUtils.fareMatrix.get("Stop2")).containsEntry("Stop1", 3.25);
        assertThat(FareMatrixUtils.fareMatrix.get("Stop2")).containsEntry("Stop3", 5.50);

        // Check reciprocal fares (ensures symmetry)
        assertThat(FareMatrixUtils.fareMatrix.get("Stop3").get("Stop1")).isEqualTo(7.00);
        assertThat(FareMatrixUtils.fareMatrix.get("Stop3").get("Stop2")).isEqualTo(5.50);
    }

    @Test
    void testFareMatrixSymmetry() {
        // Ensure that fares are symmetric (StopA -> StopB is the same as StopB -> StopA)

        assertThat(FareMatrixUtils.fareMatrix.get("Stop1").get("Stop2"))
                .isEqualTo(FareMatrixUtils.fareMatrix.get("Stop2").get("Stop1"));

        assertThat(FareMatrixUtils.fareMatrix.get("Stop1").get("Stop3"))
                .isEqualTo(FareMatrixUtils.fareMatrix.get("Stop3").get("Stop1"));

        assertThat(FareMatrixUtils.fareMatrix.get("Stop2").get("Stop3"))
                .isEqualTo(FareMatrixUtils.fareMatrix.get("Stop3").get("Stop2"));
    }
}

