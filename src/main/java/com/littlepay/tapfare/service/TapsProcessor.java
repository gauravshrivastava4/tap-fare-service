package com.littlepay.tapfare.service;

import com.littlepay.tapfare.config.TripsCsvConfig;
import com.littlepay.tapfare.model.Tap;
import com.littlepay.tapfare.model.Trip;
import com.littlepay.tapfare.utils.CsvUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class TapsProcessor {

    private final CsvUtils csvUtils;
    private final TripsCsvConfig tripsCsvConfig;
    private final TripsCreationService tripsCreationService;

    public String processTaps() {
        final List<Tap> taps = readTapsFromCsv();
        final List<Trip> trips = createTrips(taps);
        generateOutputTripsCsv(trips);
        return "Processing completed, output saved to " + tripsCsvConfig.getOutputFilePath();
    }

    private void generateOutputTripsCsv(final List<Trip> trips) {
        csvUtils.writeTripsToCsv(trips, tripsCsvConfig.getOutputFilePath());
    }

    private List<Trip> createTrips(final List<Tap> taps) {
        return tripsCreationService.createTrips(taps);
    }

    private List<Tap> readTapsFromCsv() {
        return csvUtils.readTapsFromCsv(tripsCsvConfig.getInputFilePath());
    }

}
