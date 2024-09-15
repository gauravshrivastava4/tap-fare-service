package com.littlepay.tapfare.service;

import com.littlepay.tapfare.constant.TapType;
import com.littlepay.tapfare.constant.TripStatus;
import com.littlepay.tapfare.model.Tap;
import com.littlepay.tapfare.model.Trip;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TripsCreationServiceTest {

    @Mock
    private FareCalculator fareCalculator;

    @InjectMocks
    private TripsCreationService tripsCreationService;

    private static final String DATE_PATTERN = "dd-MM-yyyy HH:mm:ss";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private Tap tapOn;
    private Tap tapOff;

    @BeforeEach
    void setUp() {
        tapOn = new Tap(1L, LocalDateTime.parse("22-01-2023 13:00:00", formatter), TapType.ON, "Stop1", "Company1", "Bus37", "4111111111111111");
        tapOff = new Tap(2L, LocalDateTime.parse("22-01-2023 13:05:00", formatter), TapType.OFF, "Stop2", "Company1", "Bus37", "4111111111111111");
    }

    @Test
    void testCreateCompletedAndCancelledTripsOnWithMatchingOffTap() {

        when(fareCalculator.calculateFare("Stop1", "Stop2")).thenReturn(3.25);

        final List<Trip> trips = tripsCreationService.createTrips(List.of(tapOff, tapOn));

        assertEquals(1, trips.size());
        final Trip trip = trips.get(0);
        assertEquals(TripStatus.COMPLETED, trip.getStatus());
        assertEquals(3.25, trip.getChargeAmount());
        assertEquals("Stop1", trip.getFromStopId());
        assertEquals("Stop2", trip.getToStopId());
    }

    @Test
    void testCreateCompletedAndCancelledTripsOffWithMatchingOnTap() {

        when(fareCalculator.calculateFare("Stop1", "Stop2")).thenReturn(2.50);

        final List<Trip> trips = tripsCreationService.createTrips(List.of(tapOn, tapOff));

        assertEquals(1, trips.size());
        final Trip trip = trips.get(0);
        assertEquals(TripStatus.COMPLETED, trip.getStatus());
        assertEquals(2.50, trip.getChargeAmount());
        assertEquals("Stop1", trip.getFromStopId());
        assertEquals("Stop2", trip.getToStopId());
    }

    @Test
    void testCreateCompletedAndCancelledTripsOnWithoutOffTap() {

        final List<Trip> trips = tripsCreationService.createTrips(List.of(tapOn));

        assertEquals(1, trips.size());
        final Trip trip = trips.get(0);
        assertEquals(TripStatus.INCOMPLETE, trip.getStatus());
    }

    @Test
    void testCreateCompletedAndCancelledTripsForOrphanOnTap() {

        when(fareCalculator.calculateMaxFare("Stop1")).thenReturn(5.50);

        final List<Trip> trips = tripsCreationService.createTrips(List.of(tapOn));

        assertEquals(1, trips.size());
        final Trip trip = trips.get(0);
        assertEquals(TripStatus.INCOMPLETE, trip.getStatus());
        assertEquals(5.50, trip.getChargeAmount());
    }

    @Test
    void testCreateCompletedAndCancelledTripsForOrphanOffTap() {
        when(fareCalculator.calculateMaxFare("Stop2")).thenReturn(7.00);

        final List<Trip> trips = tripsCreationService.createTrips(List.of(tapOff));

        assertEquals(1, trips.size());
        final Trip trip = trips.get(0);
        assertEquals(TripStatus.INCOMPLETE, trip.getStatus());
        assertEquals(7.00, trip.getChargeAmount());
    }

    @Test
    void testCancelledTripWhenTapOnAndOffAtSameStop() {
        final Tap tapOn = new Tap(1L, LocalDateTime.of(2023, 1, 22, 13, 0), TapType.ON, "Stop1", "Company1", "Bus37", "4111111111111111");
        final Tap tapOff = new Tap(2L, LocalDateTime.of(2023, 1, 22, 13, 5), TapType.OFF, "Stop1", "Company1", "Bus37", "4111111111111111");

        final List<Trip> trips = tripsCreationService.createTrips(List.of(tapOn, tapOff));

        assertEquals(1, trips.size());
        final Trip trip = trips.get(0);
        assertEquals(TripStatus.CANCELLED, trip.getStatus());
        assertEquals(0.0, trip.getChargeAmount());
    }

    @Test
    void testCreateMultipleIncompleteTrips() {
        final Tap tapOn1 = new Tap(1L, LocalDateTime.parse("22-01-2023 13:00:00", formatter), TapType.ON, "Stop1", "Company1", "Bus37", "4111111111111111");
        final Tap tapOn2 = new Tap(2L, LocalDateTime.parse("22-01-2023 13:05:00", formatter), TapType.ON, "Stop2", "Company1", "Bus37", "4111111111111112");

        when(fareCalculator.calculateMaxFare("Stop1")).thenReturn(5.50);
        when(fareCalculator.calculateMaxFare("Stop2")).thenReturn(6.00);

        final List<Trip> trips = tripsCreationService.createTrips(List.of(tapOn1, tapOn2));

        assertEquals(2, trips.size());
        assertEquals(TripStatus.INCOMPLETE, trips.get(0).getStatus());
        assertEquals(TripStatus.INCOMPLETE, trips.get(1).getStatus());
        assertEquals(5.50, trips.get(0).getChargeAmount());
        assertEquals(6.00, trips.get(1).getChargeAmount());
    }

    @Test
    void testCreateMultipleCompletedTrips() {
        final Tap tapOn1 = new Tap(1L, LocalDateTime.parse("22-01-2023 13:00:00", formatter), TapType.ON, "Stop1", "Company1", "Bus37", "4111111111111111");
        final Tap tapOff1 = new Tap(2L, LocalDateTime.parse("22-01-2023 13:05:00", formatter), TapType.OFF, "Stop2", "Company1", "Bus37", "4111111111111111");

        final Tap tapOn2 = new Tap(3L, LocalDateTime.parse("22-01-2023 14:00:00", formatter), TapType.ON, "Stop3", "Company1", "Bus37", "4111111111111112");
        final Tap tapOff2 = new Tap(4L, LocalDateTime.parse("22-01-2023 14:10:00", formatter), TapType.OFF, "Stop4", "Company1", "Bus37", "4111111111111112");

        when(fareCalculator.calculateFare("Stop1", "Stop2")).thenReturn(3.25);
        when(fareCalculator.calculateFare("Stop3", "Stop4")).thenReturn(4.50);

        final List<Trip> trips = tripsCreationService.createTrips(List.of(tapOn1, tapOff1, tapOn2, tapOff2));

        assertEquals(2, trips.size());

        // First trip assertions
        final Trip trip1 = trips.get(0);
        assertEquals(TripStatus.COMPLETED, trip1.getStatus());
        assertEquals(3.25, trip1.getChargeAmount());
        assertEquals("Stop1", trip1.getFromStopId());
        assertEquals("Stop2", trip1.getToStopId());

        // Second trip assertions
        final Trip trip2 = trips.get(1);
        assertEquals(TripStatus.COMPLETED, trip2.getStatus());
        assertEquals(4.50, trip2.getChargeAmount());
        assertEquals("Stop3", trip2.getFromStopId());
        assertEquals("Stop4", trip2.getToStopId());
    }

    @Test
    void testCreateTripsWithEmptyList() {
        final List<Trip> trips = tripsCreationService.createTrips(List.of());

        assertEquals(0, trips.size());
    }

    @Test
    void testCreateTripsWithNoMatchingOffTapForMultipleOn() {
        final Tap tapOn1 = new Tap(1L, LocalDateTime.parse("22-01-2023 13:00:00", formatter), TapType.ON, "Stop1", "Company1", "Bus37", "4111111111111111");
        final Tap tapOn2 = new Tap(2L, LocalDateTime.parse("22-01-2023 14:00:00", formatter), TapType.ON, "Stop2", "Company1", "Bus37", "4111111111111112");

        when(fareCalculator.calculateMaxFare("Stop1")).thenReturn(5.50);
        when(fareCalculator.calculateMaxFare("Stop2")).thenReturn(6.50);

        final List<Trip> trips = tripsCreationService.createTrips(List.of(tapOn1, tapOn2));

        assertEquals(2, trips.size());
        assertEquals(TripStatus.INCOMPLETE, trips.get(0).getStatus());
        assertEquals(TripStatus.INCOMPLETE, trips.get(1).getStatus());
        assertEquals(5.50, trips.get(0).getChargeAmount());
        assertEquals(6.50, trips.get(1).getChargeAmount());
    }

    @Test
    void testInvalidTapType() {
        final Tap invalidTap = new Tap(1L, LocalDateTime.parse("22-01-2023 13:00:00", formatter), null, "Stop1", "Company1", "Bus37", "4111111111111111");

        final List<Trip> trips = tripsCreationService.createTrips(List.of(invalidTap));

        assertEquals(0, trips.size());  // Invalid tap should not result in a trip
    }

    @Test
    void testOutOfOrderTaps() {
        when(fareCalculator.calculateFare("Stop1", "Stop2")).thenReturn(3.25);

        final List<Trip> trips = tripsCreationService.createTrips(List.of(tapOff, tapOn));

        assertEquals(1, trips.size());
        final Trip trip = trips.get(0);
        assertEquals(TripStatus.COMPLETED, trip.getStatus());
        assertEquals(3.25, trip.getChargeAmount());
        assertEquals("Stop1", trip.getFromStopId());
        assertEquals("Stop2", trip.getToStopId());
    }
}


