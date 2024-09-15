package com.littlepay.tapfare.service;

import com.littlepay.tapfare.config.TripsCsvConfig;
import com.littlepay.tapfare.constant.ProcessState;
import com.littlepay.tapfare.exceptions.ProcessFailedException;
import com.littlepay.tapfare.exceptions.ProcessStartedException;
import com.littlepay.tapfare.model.Tap;
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
    private final TripsCreationService tripsCreationService;

    private static ProcessState processState = ProcessState.NOT_STARTED;

    public String processTaps() {
        if (isProcessingStarted()) {
            throw new ProcessStartedException("Taps processing is already running.");
        }

        try {
            log.info("Starting taps processing.");
            updateProcessState(ProcessState.STARTED);

            final List<Tap> taps = readTapsFromCsv();
            final List<Trip> trips = createTrips(taps);
            generateOutputTripsCsv(trips);

            updateProcessState(ProcessState.COMPLETED);
            final String message = "Processing completed, output saved to %s".formatted(tripsCsvConfig.getOutputFilePath());
            log.info(message);
            return message;
        } catch (final Exception e) {
            log.error("Taps processing failed due to unexpected error.", e);
            updateProcessState(ProcessState.FAILED);
            throw new ProcessFailedException("Taps processing failed due to unexpected error: %s".formatted(e.getMessage()), e);
        }
    }

    private boolean isProcessingStarted() {
        return processState.equals(ProcessState.STARTED);
    }

    private void updateProcessState(final ProcessState newState) {
        processState = newState;
    }

    private void generateOutputTripsCsv(final List<Trip> trips) {
        try {
            csvUtils.writeTripsToCsv(trips, tripsCsvConfig.getOutputFilePath());
        } catch (final Exception e) {
            log.error("Error writing trips to CSV.", e);
            throw new ProcessFailedException("Error writing write trips to CSV: %s".formatted(e.getCause()), e);
        }
    }

    private List<Trip> createTrips(final List<Tap> taps) {
        try {
            return tripsCreationService.createTrips(taps);
        } catch (final Exception e) {
            log.error("Error creating trips from taps.", e);
            throw new ProcessFailedException("Error creating create trips from taps: %s".formatted(e.getCause()), e);
        }
    }

    private List<Tap> readTapsFromCsv() {
        try {
            return csvUtils.readTapsFromCsv(tripsCsvConfig.getInputFilePath());
        } catch (final Exception e) {
            log.error("Error reading taps from CSV.", e);
            throw new ProcessFailedException("Error reading read taps from CSV: %s".formatted(e.getCause()), e);
        }
    }
}
