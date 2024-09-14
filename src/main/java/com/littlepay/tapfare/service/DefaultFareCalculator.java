package com.littlepay.tapfare.service;

import com.littlepay.tapfare.utils.FareMatrixUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@RequiredArgsConstructor
public class DefaultFareCalculator implements FareCalculator {


    @Override
    public double calculateFare(final String fromStop, final String toStop) {
        return FareMatrixUtils.fareMatrix.getOrDefault(fromStop, new HashMap<>()).getOrDefault(toStop, 0.0);
    }

    @Override
    public double calculateMaxFare(final String stopId) {
        return FareMatrixUtils.fareMatrix.getOrDefault(stopId, new HashMap<>())
                .values()
                .stream()
                .max(Double::compare)
                .orElse(0.0);
    }
}

