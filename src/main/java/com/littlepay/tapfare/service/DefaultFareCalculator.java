package com.littlepay.tapfare.service;

import com.littlepay.tapfare.utils.FareMatrixUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@RequiredArgsConstructor
public class DefaultFareCalculator implements FareCalculator {


    /**
     * Calculates the fare between two specified stops using the fare matrix.
     *
     * @param fromStop the name of the starting stop
     * @param toStop   the name of the destination stop
     * @return the fare amount between the two stops; returns 0.0 if no fare is found between the specified stops
     */
    @Override
    public double calculateFare(final String fromStop, final String toStop) {
        return FareMatrixUtils.getFareMatrix().getOrDefault(fromStop, new HashMap<>()).getOrDefault(toStop, 0.0);
    }

    /**
     * Calculates the maximum fare from a given stop to any other stop within the fare matrix.
     *
     * @param stopId the ID of the stop from which to calculate the maximum fare
     * @return the maximum fare from the specified stop; returns 0.0 if the stop is not found or has no outgoing fares
     */
    @Override
    public double calculateMaxFare(final String stopId) {
        return FareMatrixUtils.getFareMatrix().getOrDefault(stopId, new HashMap<>())
                .values()
                .stream()
                .max(Double::compare)
                .orElse(0.0);
    }
}

