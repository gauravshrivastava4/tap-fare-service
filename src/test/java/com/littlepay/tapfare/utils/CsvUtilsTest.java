package com.littlepay.tapfare.utils;

import com.littlepay.tapfare.constant.TapType;
import com.littlepay.tapfare.constant.TripStatus;
import com.littlepay.tapfare.exceptions.CsvProcessingException;
import com.littlepay.tapfare.model.Tap;
import com.littlepay.tapfare.model.Trip;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class CsvUtilsTest {

    @InjectMocks
    private CsvUtils csvUtils;

    @TempDir
    Path tempDir;

    @Test
    void testReadTapsFromCsv_success() throws Exception {

        final Path inputCsv = tempDir.resolve("taps.csv");
        Files.write(inputCsv, List.of(
                "ID,DateTimeUTC,TapType,StopId,CompanyId,BusID,PAN", // Header
                "1,22-01-2023 13:00:00,ON,Stop1,Company1,Bus37,5500005555555559",
                "2,22-01-2023 13:05:00,OFF,Stop2,Company1,Bus37,5500005555555559"
        ));


        final List<Tap> taps = csvUtils.readTapsFromCsv(inputCsv.toString());

        assertThat(taps).hasSize(2);
        final Tap tap1 = taps.get(0);
        final Tap tap2 = taps.get(1);

        assertThat(tap1.getTapType()).isEqualTo(TapType.ON);
        assertThat(tap1.getStopId()).isEqualTo("Stop1");

        assertThat(tap2.getTapType()).isEqualTo(TapType.OFF);
        assertThat(tap2.getStopId()).isEqualTo("Stop2");
    }

    @Test
    void testReadTapsFromCsv_invalidDate() throws Exception {

        final Path inputCsv = tempDir.resolve("invalid_taps.csv");
        Files.write(inputCsv, List.of(
                "ID,DateTimeUTC,TapType,StopId,CompanyId,BusID,PAN", // Header
                "1,INVALID_DATE,ON,Stop1,Company1,Bus37,5500005555555559"
        ));

        final CsvProcessingException exception = Assertions.assertThrows(CsvProcessingException.class, () -> csvUtils.readTapsFromCsv(inputCsv.toString()));
        assertThat(exception.getMessage()).contains("Error reading from CSV file: ");
    }

    @Test
    void testWriteTripsToCsv_success() throws Exception {

        final Path outputCsv = tempDir.resolve("trips.csv");

        final List<Trip> trips = List.of(
                new Trip(LocalDateTime.of(2023, 1, 22, 13, 0), LocalDateTime.of(2023, 1, 22, 13, 5), 300,
                        "Stop1", "Stop2", 3.25, "Company1", "Bus37", "5500005555555559", TripStatus.COMPLETED),
                new Trip(LocalDateTime.of(2023, 1, 23, 9, 0), null, 0, "Stop3", null, 7.00, "Company2",
                        "Bus38", "4111111111111111", TripStatus.INCOMPLETE)
        );

        csvUtils.writeTripsToCsv(trips, outputCsv.toString());

        final List<String> lines = Files.readAllLines(outputCsv);
        assertThat(lines).hasSize(3); // Header + 2 records
        assertThat(lines.get(0)).isEqualTo("Started,Finished,DurationSecs,FromStopId,ToStopId,ChargeAmount,CompanyId,BusID,PAN,Status");
        assertThat(lines.get(1)).isEqualTo("22-01-2023 13:00:00,22-01-2023 13:05:00,300,Stop1,Stop2,3.25,Company1,Bus37,5500005555555559,COMPLETED");
        assertThat(lines.get(2)).isEqualTo("23-01-2023 09:00:00,,0,Stop3,,7.0,Company2,Bus38,4111111111111111,INCOMPLETE");
    }

    @Test
    void testWriteTripsToCsv_error() {

        final String invalidPath = "/invalid_path/trips.csv"; // Invalid path

        final List<Trip> trips = List.of(
                new Trip(LocalDateTime.of(2023, 1, 22, 13, 0), LocalDateTime.of(2023, 1, 22, 13, 5), 300,
                        "Stop1", "Stop2", 3.25, "Company1", "Bus37", "5500005555555559", TripStatus.COMPLETED)
        );

        assertThrows(RuntimeException.class, () -> csvUtils.writeTripsToCsv(trips, invalidPath));
    }
}