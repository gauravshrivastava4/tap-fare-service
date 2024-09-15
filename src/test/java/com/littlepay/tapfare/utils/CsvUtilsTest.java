package com.littlepay.tapfare.utils;

import com.littlepay.tapfare.constant.TripStatus;
import com.littlepay.tapfare.exceptions.CsvProcessingException;
import com.littlepay.tapfare.model.Tap;
import com.littlepay.tapfare.model.Trip;
import com.littlepay.tapfare.service.TripsCreationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CsvUtilsTest {

    @Mock
    private TripsCreationService tripsCreationService;

    @InjectMocks
    private CsvUtils csvUtils;

    @Test
    public void testReadTapsFromCsvAndCreateTrips_Success() throws IOException {
        // Mock input CSV data
        final String inputCsv = """
                ID,DateTime,TapType,StopId,CompanyId,BusId,PAN
                1,01-01-2023 08:00:00,ON,Stop1,Company1,Bus1,PAN1
                2,01-01-2023 08:10:00,OFF,Stop2,Company1,Bus1,PAN1
                """;

        // Set up a temporary CSV file for the test
        final File tempFile = File.createTempFile("testInput", ".csv");
        try (final FileWriter fileWriter = new FileWriter(tempFile)) {
            fileWriter.write(inputCsv);
        }

        doNothing().when(tripsCreationService).createCompletedAndCancelledTrips(any(Tap.class));
        when(tripsCreationService.getTrips()).thenReturn(new ArrayList<>());

        final List<Trip> trips = csvUtils.readTapsFromCsvAndCreateTrips(tempFile.getAbsolutePath());

        verify(tripsCreationService, times(2)).createCompletedAndCancelledTrips(any(Tap.class));
        assertNotNull(trips);
        assertEquals(0, trips.size()); // Since we mocked an empty return list
    }

    @Test
    public void testReadTapsFromCsvAndCreateTrips_Exception() throws IOException {
        // Mock input CSV data with an invalid date format
        final String inputCsv = """
                ID,DateTime,TapType,StopId,CompanyId,BusId,PAN
                1,invalid-date,ON,Stop1,Company1,Bus1,PAN1
                """;

        // Set up a temporary CSV file for the test
        final File tempFile = File.createTempFile("testInput", ".csv");
        try (final FileWriter fileWriter = new FileWriter(tempFile)) {
            fileWriter.write(inputCsv);
        }

        assertThrows(CsvProcessingException.class, () -> {
            csvUtils.readTapsFromCsvAndCreateTrips(tempFile.getAbsolutePath());
        });
    }

    @Test
    public void testWriteTripsToCsv_Success() throws IOException {
        final List<Trip> trips = new ArrayList<>();
        trips.add(new Trip(LocalDateTime.of(2023, 1, 1, 8, 0),
                LocalDateTime.of(2023, 1, 1, 8, 10),
                600, "Stop1", "Stop2", 3.25, "Company1", "Bus1", "PAN1", TripStatus.COMPLETED));

        // Set up a temporary CSV file for the test
        final File tempFile = File.createTempFile("testOutput", ".csv");

        csvUtils.writeTripsToCsv(trips, tempFile.getAbsolutePath());

        try (final BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {
            final String header = reader.readLine();
            final String firstLine = reader.readLine();

            assertNotNull(header);
            assertEquals("Started,Finished,DurationSecs,FromStopId,ToStopId,ChargeAmount,CompanyId,BusID,PAN,Status", header);

            assertNotNull(firstLine);
            assertTrue(firstLine.contains("01-01-2023 08:00:00"));
            assertTrue(firstLine.contains("600")); // DurationSecs
            assertTrue(firstLine.contains("COMPLETED")); // Status
        }
    }

    @Test
    public void testWriteTripsToCsv_Exception() {
        final List<Trip> trips = new ArrayList<>();
        trips.add(new Trip(LocalDateTime.of(2023, 1, 1, 8, 0),
                LocalDateTime.of(2023, 1, 1, 8, 10),
                600, "Stop1", "Stop2", 3.25, "Company1", "Bus1", "PAN1", TripStatus.COMPLETED));

        assertThrows(CsvProcessingException.class, () -> {
            csvUtils.writeTripsToCsv(trips, "/invalid/path/output.csv");
        });
    }
}
