package com.littlepay.tapfare.service;

public interface FareCalculator {
    double calculateFare(String fromStop, String toStop);

    double calculateMaxFare(String stopId);
}
