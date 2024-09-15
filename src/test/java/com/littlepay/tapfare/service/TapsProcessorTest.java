package com.littlepay.tapfare.service;

import com.littlepay.tapfare.config.TripsCsvConfig;
import com.littlepay.tapfare.constant.ProcessState;
import com.littlepay.tapfare.constant.TapType;
import com.littlepay.tapfare.constant.TripStatus;
import com.littlepay.tapfare.exceptions.ProcessFailedException;
import com.littlepay.tapfare.exceptions.ProcessStartedException;
import com.littlepay.tapfare.model.Tap;
import com.littlepay.tapfare.model.Trip;
import com.littlepay.tapfare.utils.CsvUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TapsProcessorTest {

    @InjectMocks
    private TapsProcessor tapsProcessor;

    @Mock
    private CsvUtils csvUtils;

    @Mock
    private TripsCsvConfig tripsCsvConfig;

    @Mock
    ProcessStateHandler processStateHandler;

    @Mock
    TripsCreationService tripsCreationService;

    private static final String DATE_PATTERN = "dd-MM-yyyy HH:mm:ss";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private List<Tap> mockTaps;
    private List<Trip> mockTrips;

    @BeforeEach
    void setUp() {
        mockTaps = List.of(
                new Tap(1, LocalDateTime.parse("22-01-2023 13:00:00", formatter), TapType.ON, "Stop1", "Company1", "Bus1", "4111111111111111"),
                new Tap(2, LocalDateTime.parse("22-01-2023 13:05:00", formatter), TapType.OFF, "Stop2", "Company1", "Bus1", "4111111111111111")
        );

        mockTrips = List.of(
                new Trip(LocalDateTime.parse("22-01-2023 13:00:00", formatter), LocalDateTime.parse("22-01-2023 13:05:00", formatter), 300, "Stop1", "Stop2", 3.25, "Company1", "Bus1", "4111111111111111", TripStatus.COMPLETED)
        );
    }

    @Test
    void testProcessTaps_success() {
        final String inputFilePath = "input.csv";
        final String outputFilePath = "output.csv";
        when(tripsCsvConfig.getInputFilePath()).thenReturn(inputFilePath);
        when(tripsCsvConfig.getOutputFilePath()).thenReturn(outputFilePath);
        when(csvUtils.readTapsFromCsv(inputFilePath)).thenReturn(mockTaps);
        when(tripsCreationService.createTrips(mockTaps)).thenReturn(mockTrips);

        final String result = tapsProcessor.processTaps();

        verify(csvUtils).readTapsFromCsv(inputFilePath);
        verify(csvUtils).writeTripsToCsv(mockTrips, outputFilePath);

        assertThat(result).isEqualTo("Processing completed, output saved to output.csv");
    }

    @Test
    void testProcessTaps_emptyInput() {
        final String inputFilePath = "input.csv";
        final String outputFilePath = "output.csv";
        when(tripsCsvConfig.getInputFilePath()).thenReturn(inputFilePath);
        when(tripsCsvConfig.getOutputFilePath()).thenReturn(outputFilePath);

        final List<Tap> emptyTaps = List.of();
        when(csvUtils.readTapsFromCsv(inputFilePath)).thenReturn(emptyTaps);
        when(tripsCreationService.createTrips(emptyTaps)).thenReturn(List.of());

        final String result = tapsProcessor.processTaps();

        verify(csvUtils).readTapsFromCsv(inputFilePath);
        verify(tripsCreationService).createTrips(emptyTaps);
        verify(csvUtils).writeTripsToCsv(Collections.emptyList(), outputFilePath);

        assertThat(result).isEqualTo("Processing completed, output saved to output.csv");
    }

    @Test
    void testProcessTaps_errorInReading() {
        final String inputFilePath = "input.csv";
        when(tripsCsvConfig.getInputFilePath()).thenReturn(inputFilePath);

        when(csvUtils.readTapsFromCsv(inputFilePath)).thenThrow(new RuntimeException("Error reading CSV"));

        final ProcessFailedException exception = Assertions.assertThrows(ProcessFailedException.class, () -> tapsProcessor.processTaps());
        assertThat(exception.getMessage()).contains("Taps processing failed due to unexpected error");

        verify(csvUtils).readTapsFromCsv(inputFilePath);
        verify(processStateHandler).setProcessState(ProcessState.FAILED);
    }

    @Test
    void testProcessTaps_errorInTripCreation() {
        final String inputFilePath = "input.csv";
        when(tripsCsvConfig.getInputFilePath()).thenReturn(inputFilePath);

        when(csvUtils.readTapsFromCsv(inputFilePath)).thenReturn(mockTaps);
        when(tripsCreationService.createTrips(mockTaps)).thenThrow(new RuntimeException("Error creating trips"));

        final ProcessFailedException exception = Assertions.assertThrows(ProcessFailedException.class, () -> tapsProcessor.processTaps());
        assertThat(exception.getMessage()).contains("Taps processing failed due to unexpected error");

        verify(csvUtils).readTapsFromCsv(inputFilePath);
        verify(tripsCreationService).createTrips(mockTaps);
        verify(processStateHandler).setProcessState(ProcessState.FAILED);
    }

    @Test
    void testProcessTaps_errorInCsvWriting() {
        final String inputFilePath = "input.csv";
        final String outputFilePath = "output.csv";
        when(tripsCsvConfig.getInputFilePath()).thenReturn(inputFilePath);
        when(tripsCsvConfig.getOutputFilePath()).thenReturn(outputFilePath);
        when(csvUtils.readTapsFromCsv(inputFilePath)).thenReturn(mockTaps);
        when(tripsCreationService.createTrips(mockTaps)).thenReturn(mockTrips);
        doThrow(new RuntimeException("Error writing to CSV")).when(csvUtils).writeTripsToCsv(mockTrips, outputFilePath);

        final ProcessFailedException exception = Assertions.assertThrows(ProcessFailedException.class, () -> tapsProcessor.processTaps());
        assertThat(exception.getMessage()).contains("Error writing write trips to CSV");

        verify(csvUtils).readTapsFromCsv(inputFilePath);
        verify(tripsCreationService).createTrips(mockTaps);
        verify(csvUtils).writeTripsToCsv(mockTrips, outputFilePath);
        verify(processStateHandler).setProcessState(ProcessState.FAILED);
    }

    @Test
    void testProcessTaps_errorAlreadyStarted() {
        when(processStateHandler.isProcessingAlreadyRunning()).thenReturn(true);

        final ProcessStartedException exception = Assertions.assertThrows(ProcessStartedException.class, () -> tapsProcessor.processTaps());
        assertThat(exception.getMessage()).contains("Taps processing is already running.");
    }

    @Test
    void testProcessTaps_updatesStateCorrectly() {
        final String inputFilePath = "input.csv";
        final String outputFilePath = "output.csv";
        when(tripsCsvConfig.getInputFilePath()).thenReturn(inputFilePath);
        when(tripsCsvConfig.getOutputFilePath()).thenReturn(outputFilePath);
        when(csvUtils.readTapsFromCsv(inputFilePath)).thenReturn(mockTaps);
        when(tripsCreationService.createTrips(mockTaps)).thenReturn(mockTrips);

        tapsProcessor.processTaps();

        verify(processStateHandler).setProcessState(ProcessState.STARTED);
        verify(processStateHandler).setProcessState(ProcessState.COMPLETED);
    }
}

