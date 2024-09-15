package com.littlepay.tapfare.utils;

import com.littlepay.tapfare.config.TripsFareConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FareMatrixUtilsTest {

    @Mock
    private TripsFareConfig tripsFareConfig;
    @InjectMocks
    private FareMatrixUtils fareMatrixUtils;

    @BeforeEach
    void setUp() {

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
        assertThat(FareMatrixUtils.getFareMatrix()).containsKey("Stop1");
        assertThat(FareMatrixUtils.getFareMatrix().get("Stop1")).containsEntry("Stop2", 3.25);
        assertThat(FareMatrixUtils.getFareMatrix().get("Stop1")).containsEntry("Stop3", 7.00);

        assertThat(FareMatrixUtils.getFareMatrix()).containsKey("Stop2");
        assertThat(FareMatrixUtils.getFareMatrix().get("Stop2")).containsEntry("Stop1", 3.25);
        assertThat(FareMatrixUtils.getFareMatrix().get("Stop2")).containsEntry("Stop3", 5.50);

        assertThat(FareMatrixUtils.getFareMatrix().get("Stop3").get("Stop1")).isEqualTo(7.00);
        assertThat(FareMatrixUtils.getFareMatrix().get("Stop3").get("Stop2")).isEqualTo(5.50);
    }

    @Test
    void testFareMatrixSymmetry() {
        assertThat(FareMatrixUtils.getFareMatrix().get("Stop1").get("Stop2"))
                .isEqualTo(FareMatrixUtils.getFareMatrix().get("Stop2").get("Stop1"));

        assertThat(FareMatrixUtils.getFareMatrix().get("Stop1").get("Stop3"))
                .isEqualTo(FareMatrixUtils.getFareMatrix().get("Stop3").get("Stop1"));

        assertThat(FareMatrixUtils.getFareMatrix().get("Stop2").get("Stop3"))
                .isEqualTo(FareMatrixUtils.getFareMatrix().get("Stop3").get("Stop2"));
    }
}

