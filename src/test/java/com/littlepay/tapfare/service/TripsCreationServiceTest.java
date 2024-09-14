package com.littlepay.tapfare.service;

import com.littlepay.tapfare.constant.TapType;
import com.littlepay.tapfare.constant.TripStatus;
import com.littlepay.tapfare.model.Tap;
import com.littlepay.tapfare.model.Trip;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TripsCreationServiceTest {

    @InjectMocks
    private TripsCreationService tripsCreationService;

    @Mock
    private FareCalculator fareCalculator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testCreateCompletedTrip() {
        // Arrange
        final Tap tapOn = new Tap(1L, LocalDateTime.of(2023, 1, 22, 13, 0), TapType.ON, "Stop1", "Company1", "Bus37", "4111111111111111");
        final Tap tapOff = new Tap(2L, LocalDateTime.of(2023, 1, 22, 13, 5), TapType.OFF, "Stop2", "Company1", "Bus37", "4111111111111111");
        final List<Tap> taps = Arrays.asList(tapOn, tapOff);

        when(fareCalculator.calculateFare("Stop1", "Stop2")).thenReturn(3.25);

        // Act
        final List<Trip> trips = tripsCreationService.createTrips(taps);

        // Assert
        assertThat(trips).hasSize(1);
        final Trip trip = trips.get(0);
        assertThat(trip.getFromStopId()).isEqualTo("Stop1");
        assertThat(trip.getToStopId()).isEqualTo("Stop2");
        assertThat(trip.getDurationSecs()).isEqualTo(300); // 5 minutes in seconds
        assertThat(trip.getChargeAmount()).isEqualTo(3.25);
        assertThat(trip.getStatus()).isEqualTo(TripStatus.COMPLETED);

        verify(fareCalculator).calculateFare("Stop1", "Stop2");
    }

    @Test
    void testCreateIncompleteTripWithOnlyTapOn() {
        // Arrange
        final Tap tapOn = new Tap(1L, LocalDateTime.of(2023, 1, 22, 13, 0), TapType.ON, "Stop1", "Company1", "Bus37", "4111111111111111");
        final List<Tap> taps = Arrays.asList(tapOn);

        when(fareCalculator.calculateMaxFare("Stop1")).thenReturn(5.00);

        // Act
        final List<Trip> trips = tripsCreationService.createTrips(taps);

        // Assert
        assertThat(trips).hasSize(1);
        final Trip trip = trips.get(0);
        assertThat(trip.getFromStopId()).isEqualTo("Stop1");
        assertThat(trip.getToStopId()).isNull();
        assertThat(trip.getDurationSecs()).isEqualTo(0);
        assertThat(trip.getChargeAmount()).isEqualTo(5.00);
        assertThat(trip.getStatus()).isEqualTo(TripStatus.INCOMPLETE);

        verify(fareCalculator).calculateMaxFare("Stop1");
    }

    @Test
    void testCreateCancelledTrip() {
        // Arrange
        final Tap tapOn = new Tap(1L, LocalDateTime.of(2023, 1, 22, 13, 0), TapType.ON, "Stop1", "Company1", "Bus37", "4111111111111111");
        final Tap tapOff = new Tap(2L, LocalDateTime.of(2023, 1, 22, 13, 5), TapType.OFF, "Stop1", "Company1", "Bus37", "4111111111111111");
        final List<Tap> taps = Arrays.asList(tapOn, tapOff);

        // Act
        final List<Trip> trips = tripsCreationService.createTrips(taps);

        // Assert
        assertThat(trips).hasSize(1);
        final Trip trip = trips.get(0);
        assertThat(trip.getFromStopId()).isEqualTo("Stop1");
        assertThat(trip.getToStopId()).isEqualTo("Stop1");
        assertThat(trip.getDurationSecs()).isEqualTo(0);
        assertThat(trip.getChargeAmount()).isEqualTo(0.0);
        assertThat(trip.getStatus()).isEqualTo(TripStatus.CANCELLED);
    }

    @Test
    void testCreateIncompleteTripWithOrphanTapOff() {
        // Arrange
        final Tap tapOff = new Tap(1L, LocalDateTime.of(2023, 1, 22, 13, 0), TapType.OFF, "Stop2", "Company1", "Bus37", "4111111111111111");
        final List<Tap> taps = Arrays.asList(tapOff);

        when(fareCalculator.calculateMaxFare("Stop2")).thenReturn(7.00);

        // Act
        final List<Trip> trips = tripsCreationService.createTrips(taps);

        // Assert
        assertThat(trips).hasSize(1);
        final Trip trip = trips.get(0);
        assertThat(trip.getFromStopId()).isNull();
        assertThat(trip.getToStopId()).isEqualTo("Stop2");
        assertThat(trip.getDurationSecs()).isEqualTo(0);
        assertThat(trip.getChargeAmount()).isEqualTo(7.00);
        assertThat(trip.getStatus()).isEqualTo(TripStatus.INCOMPLETE);

        verify(fareCalculator).calculateMaxFare("Stop2");
    }

    @Test
    void testCreateIncompleteTripForConsecutiveOns() {
        // Arrange
        final Tap tapOn1 = new Tap(1L, LocalDateTime.of(2023, 1, 22, 13, 0), TapType.ON, "Stop1", "Company1", "Bus37", "4111111111111111");
        final Tap tapOn2 = new Tap(2L, LocalDateTime.of(2023, 1, 22, 13, 5), TapType.ON, "Stop2", "Company1", "Bus37", "4111111111111111");
        final List<Tap> taps = Arrays.asList(tapOn1, tapOn2);

        when(fareCalculator.calculateMaxFare("Stop1")).thenReturn(5.00);

        // Act
        final List<Trip> trips = tripsCreationService.createTrips(taps);

        // Assert
        assertThat(trips).hasSize(2);
        final Trip incompleteTrip = trips.get(0);
        assertThat(incompleteTrip.getFromStopId()).isEqualTo("Stop1");
        assertThat(incompleteTrip.getToStopId()).isNull();
        assertThat(incompleteTrip.getDurationSecs()).isEqualTo(0);
        assertThat(incompleteTrip.getChargeAmount()).isEqualTo(5.00);
        assertThat(incompleteTrip.getStatus()).isEqualTo(TripStatus.INCOMPLETE);

        verify(fareCalculator).calculateMaxFare("Stop1");
    }
}

