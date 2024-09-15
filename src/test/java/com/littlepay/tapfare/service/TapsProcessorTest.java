package com.littlepay.tapfare.service;

import com.littlepay.tapfare.config.TripsCsvConfig;
import com.littlepay.tapfare.constant.TapType;
import com.littlepay.tapfare.constant.TripStatus;
import com.littlepay.tapfare.model.Tap;
import com.littlepay.tapfare.model.Trip;
import com.littlepay.tapfare.utils.CsvUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TapsProcessorTest {

    @InjectMocks
    private TapsProcessor tapsProcessor;

    @Mock
    private CsvUtils csvUtils;

    @Mock
    private TripsCsvConfig tripsCsvConfig;

    @Mock
    private TripsCreationService tripsCreationService;

    private static final String DATE_PATTERN = "dd-MM-yyyy HH:mm:ss";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testProcessTaps_success() {
        // Arrange
        final String inputFilePath = "input.csv";
        final String outputFilePath = "output.csv";
        when(tripsCsvConfig.getInputFilePath()).thenReturn(inputFilePath);
        when(tripsCsvConfig.getOutputFilePath()).thenReturn(outputFilePath);

        final List<Tap> mockTaps = Arrays.asList(
                new Tap(1, LocalDateTime.parse("22-01-2023 13:00:00", formatter), TapType.ON, "Stop1", "Company1", "Bus1", "4111111111111111"),
                new Tap(2, LocalDateTime.parse("22-01-2023 13:05:00", formatter), TapType.OFF, "Stop2", "Company1", "Bus1", "4111111111111111")
        );
        final List<Trip> mockTrips = Arrays.asList(
                new Trip(LocalDateTime.parse("22-01-2023 13:00:00", formatter), LocalDateTime.parse("22-01-2023 13:05:00", formatter), 300, "Stop1", "Stop2", 3.25, "Company1", "Bus1", "4111111111111111", TripStatus.COMPLETED)
        );

        when(csvUtils.readTapsFromCsv(inputFilePath)).thenReturn(mockTaps);
        when(tripsCreationService.createTrips(mockTaps)).thenReturn(mockTrips);

        // Act
        final String result = tapsProcessor.processTaps();

        // Assert
        verify(csvUtils).readTapsFromCsv(inputFilePath);
        verify(tripsCreationService).createTrips(mockTaps);
        verify(csvUtils).writeTripsToCsv(mockTrips, outputFilePath);

        assertThat(result).isEqualTo("Processing completed, output saved to output.csv");
    }

    @Test
    void testProcessTaps_emptyInput() {
        // Arrange
        final String inputFilePath = "input.csv";
        final String outputFilePath = "output.csv";
        when(tripsCsvConfig.getInputFilePath()).thenReturn(inputFilePath);
        when(tripsCsvConfig.getOutputFilePath()).thenReturn(outputFilePath);

        final List<Tap> emptyTaps = Arrays.asList();
        when(csvUtils.readTapsFromCsv(inputFilePath)).thenReturn(emptyTaps);
        when(tripsCreationService.createTrips(emptyTaps)).thenReturn(Arrays.asList());

        // Act
        final String result = tapsProcessor.processTaps();

        // Assert
        verify(csvUtils).readTapsFromCsv(inputFilePath);
        verify(tripsCreationService).createTrips(emptyTaps);
        verify(csvUtils).writeTripsToCsv(Arrays.asList(), outputFilePath);

        assertThat(result).isEqualTo("Processing completed, output saved to output.csv");
    }

    @Test
    void testProcessTaps_errorInReading() {
        // Arrange
        final String inputFilePath = "input.csv";
        final String outputFilePath = "output.csv";
        when(tripsCsvConfig.getInputFilePath()).thenReturn(inputFilePath);
        when(tripsCsvConfig.getOutputFilePath()).thenReturn(outputFilePath);

        when(csvUtils.readTapsFromCsv(inputFilePath)).thenThrow(new RuntimeException("Error reading CSV"));

        // Act & Assert
        final RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> tapsProcessor.processTaps());
        assertThat(exception.getMessage()).contains("Taps processing failed due to unexpected error: Error reading read taps from CSV:");

        verify(csvUtils).readTapsFromCsv(inputFilePath);
        verifyNoMoreInteractions(tripsCreationService, csvUtils);
    }
}
