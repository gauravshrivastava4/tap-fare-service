package com.littlepay.tapfare.service;

import com.littlepay.tapfare.config.TripsCsvConfig;
import com.littlepay.tapfare.constant.ProcessState;
import com.littlepay.tapfare.exceptions.ProcessFailedException;
import com.littlepay.tapfare.exceptions.ProcessStartedException;
import com.littlepay.tapfare.model.Trip;
import com.littlepay.tapfare.utils.CsvUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class TapsProcessor {

    private final CsvUtils csvUtils;
    private final TripsCsvConfig tripsCsvConfig;
    private final ProcessStateHandler processStateHandler;

    public String processTaps() {
        validateProcessState();
        try {
            final List<Trip> trips = readTapsFromCsvAndCreateTrips();
            return writeTripsToCsv(trips);
        } catch (final Exception e) {
            log.error("Taps processing failed due to unexpected error.", e);
            updateProcessState(ProcessState.FAILED);
            throw new ProcessFailedException("Taps processing failed due to unexpected error: %s".formatted(e.getMessage()), e);
        }
    }

    private void validateProcessState() {
        if (processStateHandler.isProcessingAlreadyRunning()) {
            throw new ProcessStartedException("Taps processing is already running.");
        }
    }

    private void updateProcessState(final ProcessState newState) {
        processStateHandler.setProcessState(newState);
    }

    private String writeTripsToCsv(final List<Trip> trips) {
        try {
            csvUtils.writeTripsToCsv(trips, tripsCsvConfig.getOutputFilePath());
            updateProcessState(ProcessState.COMPLETED);
            final String message = "Processing completed, output saved to %s".formatted(tripsCsvConfig.getOutputFilePath());
            log.info(message);
            return message;
        } catch (final Exception e) {
            log.error("Error writing trips to CSV.", e);
            throw new ProcessFailedException("Error writing write trips to CSV: %s".formatted(e.getCause()), e);
        }
    }

    private List<Trip> readTapsFromCsvAndCreateTrips() {
        try {
            log.info("Starting taps processing.");
            updateProcessState(ProcessState.STARTED);
            return csvUtils.readTapsFromCsvAndCreateTrips(tripsCsvConfig.getInputFilePath());
        } catch (final Exception e) {
            log.error("Error reading taps from CSV.", e);
            throw new ProcessFailedException("Error reading read taps from CSV: %s".formatted(e.getCause()), e);
        }
    }
}
