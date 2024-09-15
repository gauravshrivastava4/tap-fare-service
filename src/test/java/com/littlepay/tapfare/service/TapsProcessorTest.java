package com.littlepay.tapfare.service;

import com.littlepay.tapfare.config.TripsCsvConfig;
import com.littlepay.tapfare.constant.TripStatus;
import com.littlepay.tapfare.exceptions.ProcessStartedException;
import com.littlepay.tapfare.model.Trip;
import com.littlepay.tapfare.utils.CsvUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    private static final String DATE_PATTERN = "dd-MM-yyyy HH:mm:ss";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);

    @Test
    void testProcessTaps_success() {
        final String inputFilePath = "input.csv";
        final String outputFilePath = "output.csv";
        when(tripsCsvConfig.getInputFilePath()).thenReturn(inputFilePath);
        when(tripsCsvConfig.getOutputFilePath()).thenReturn(outputFilePath);

        final List<Trip> mockTrips = List.of(
                new Trip(LocalDateTime.parse("22-01-2023 13:00:00", formatter), LocalDateTime.parse("22-01-2023 13:05:00", formatter), 300, "Stop1", "Stop2", 3.25, "Company1", "Bus1", "4111111111111111", TripStatus.COMPLETED)
        );

        when(csvUtils.readTapsFromCsvAndCreateTrips(inputFilePath)).thenReturn(mockTrips);

        final String result = tapsProcessor.processTaps();

        verify(csvUtils).readTapsFromCsvAndCreateTrips(inputFilePath);
        verify(csvUtils).writeTripsToCsv(mockTrips, outputFilePath);

        assertThat(result).isEqualTo("Processing completed, output saved to output.csv");
    }

    @Test
    void testProcessTaps_emptyInput() {
        final String inputFilePath = "input.csv";
        final String outputFilePath = "output.csv";
        when(tripsCsvConfig.getInputFilePath()).thenReturn(inputFilePath);
        when(tripsCsvConfig.getOutputFilePath()).thenReturn(outputFilePath);

        final List<Trip> emptyTrip = List.of();
        when(csvUtils.readTapsFromCsvAndCreateTrips(inputFilePath)).thenReturn(emptyTrip);

        final String result = tapsProcessor.processTaps();

        verify(csvUtils).readTapsFromCsvAndCreateTrips(inputFilePath);
        verify(csvUtils).writeTripsToCsv(List.of(), outputFilePath);

        assertThat(result).isEqualTo("Processing completed, output saved to output.csv");
    }

    @Test
    void testProcessTaps_errorInReading() {
        final String inputFilePath = "input.csv";
        when(tripsCsvConfig.getInputFilePath()).thenReturn(inputFilePath);

        when(csvUtils.readTapsFromCsvAndCreateTrips(inputFilePath)).thenThrow(new RuntimeException("Error reading CSV"));

        final RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> tapsProcessor.processTaps());
        assertThat(exception.getMessage()).contains("Taps processing failed due to unexpected error: Error reading read taps from CSV:");

        verify(csvUtils).readTapsFromCsvAndCreateTrips(inputFilePath);
    }


    @Test
    void testProcessTaps_errorAlreadyStarted() {
        when(processStateHandler.isProcessingAlreadyRunning()).thenReturn(true);
        final ProcessStartedException exception = Assertions.assertThrows(ProcessStartedException.class, () -> tapsProcessor.processTaps());
        assertThat(exception.getMessage()).contains("Taps processing is already running.");
    }
}
