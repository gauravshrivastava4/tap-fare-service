package com.littlepay.tapfare.service;

import com.littlepay.tapfare.constant.TapType;
import com.littlepay.tapfare.constant.TripStatus;
import com.littlepay.tapfare.model.Tap;
import com.littlepay.tapfare.model.Trip;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TripsCreationService {

    private final FareCalculator fareCalculator;

    public TripsCreationService(final FareCalculator fareCalculator) {
        this.fareCalculator = fareCalculator;
    }

    public List<Trip> createTrips(final List<Tap> taps) {
        final List<Trip> trips = new ArrayList<>();

        Tap previousTap = null;
        TripStatus previousTripStatus = null;
        for (int i = 0; i < taps.size(); i++) {
            final Tap currentTap = taps.get(i);

            // Handle current ON, look for matching OFF
            final Tap tapOff = findTapOffForPan(taps, currentTap.getPan(), currentTap.getLocalDateTime(), i);
            if (currentTap.getTapType() == TapType.ON) {
                final Trip trip = createTrip(currentTap, tapOff);
                trips.add(trip);
                previousTap = currentTap;
                previousTripStatus = trip.getStatus();
            } else if (currentTap.getTapType() == TapType.OFF) {
                // Handle orphaned OFF (no matching ON)
                if (previousTap == null || (!previousTripStatus.equals(TripStatus.COMPLETED) && (!previousTap.getPan().equals(currentTap.getPan())) && i == taps.size() - 1)) {
                    trips.add(handleOrphanTapOff(currentTap));
                }
            }
        }
        return trips;
    }

    private Tap findTapOffForPan(final List<Tap> taps, final String pan, final LocalDateTime localDateTime, final int index) {
        return taps.stream()
                .skip(index + 1)
                .filter(tap -> tap.getPan().equals(pan) && tap.getTapType() == TapType.OFF && tap.getLocalDateTime().toLocalDate().isEqual(localDateTime.toLocalDate()))
                .findFirst()
                .orElse(null);
    }

    private Trip createTrip(final Tap tapOn, final Tap tapOff) {
        if (tapOff == null) {
            return createIncompleteTrip(tapOn);
        }
        if (tapOn.getStopId().equals(tapOff.getStopId())) {
            return createCancelledTrip(tapOn, tapOff);
        }
        return createCompletedTrip(tapOn, tapOff);
    }

    private Trip handleOrphanTapOff(final Tap tapOff) {
        // Create an incomplete trip assuming the passenger didn't tap on
        final double maxFare = fareCalculator.calculateMaxFare(tapOff.getStopId());
        return new Trip(
                null, // No start time (since TAPON is missing)
                tapOff.getLocalDateTime(),
                0, // No duration since the trip is incomplete
                null, // No starting stop
                tapOff.getStopId(),
                maxFare,
                tapOff.getCompanyId(),
                tapOff.getBusId(),
                tapOff.getPan(),
                TripStatus.INCOMPLETE
        );
    }

    private Trip createIncompleteTrip(final Tap tapOn) {
        final double fare = fareCalculator.calculateMaxFare(tapOn.getStopId());
        return new Trip(
                tapOn.getLocalDateTime(),
                null,
                0,
                tapOn.getStopId(),
                null,
                fare,
                tapOn.getCompanyId(),
                tapOn.getBusId(),
                tapOn.getPan(),
                TripStatus.INCOMPLETE
        );
    }

    private Trip createCancelledTrip(final Tap tapOn, final Tap tapOff) {
        return new Trip(
                tapOn.getLocalDateTime(),
                tapOff.getLocalDateTime(),
                0,
                tapOn.getStopId(),
                tapOff.getStopId(),
                0.0,
                tapOn.getCompanyId(),
                tapOn.getBusId(),
                tapOn.getPan(),
                TripStatus.CANCELLED
        );
    }

    private Trip createCompletedTrip(final Tap tapOn, final Tap tapOff) {
        final long durationSecs = Duration.between(tapOn.getLocalDateTime(), tapOff.getLocalDateTime()).getSeconds();
        final double fare = fareCalculator.calculateFare(tapOn.getStopId(), tapOff.getStopId());
        return new Trip(
                tapOn.getLocalDateTime(),
                tapOff.getLocalDateTime(),
                durationSecs,
                tapOn.getStopId(),
                tapOff.getStopId(),
                fare,
                tapOn.getCompanyId(),
                tapOn.getBusId(),
                tapOn.getPan(),
                TripStatus.COMPLETED
        );
    }
}
