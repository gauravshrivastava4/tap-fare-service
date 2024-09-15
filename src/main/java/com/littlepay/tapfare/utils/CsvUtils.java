package com.littlepay.tapfare.utils;

import com.littlepay.tapfare.constant.TapType;
import com.littlepay.tapfare.exceptions.CsvProcessingException;
import com.littlepay.tapfare.model.Tap;
import com.littlepay.tapfare.model.Trip;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class CsvUtils {

    private static final String DATE_PATTERN = "dd-MM-yyyy HH:mm:ss";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private static final String[] TRIP_CSV_HEADER = {"Started", "Finished", "DurationSecs", "FromStopId", "ToStopId",
            "ChargeAmount", "CompanyId", "BusID", "PAN", "Status"};

    public List<Tap> readTapsFromCsv(final String inputFilePath) {
        final List<Tap> taps = new ArrayList<>();

        try (final CSVReader reader = new CSVReader(new FileReader(inputFilePath))) {
            log.info("Reading taps from CSV file: {}", inputFilePath);
            String[] line;
            reader.readNext(); // Skip header

            while ((line = reader.readNext()) != null) {
                try {
                    final Tap tap = parseCsvLineToTap(line);
                    taps.add(tap);
                } catch (final DateTimeParseException e) {
                    log.error("Error parsing date for tap ID: {}", line[0], e);
                    throw new CsvProcessingException("Error parsing date for tap ID: %s".formatted(line[0]), e);
                } catch (final IllegalArgumentException e) {
                    log.error("Invalid data found in line for tap ID: {}", line[0], e);
                    throw new CsvProcessingException("Invalid data found in line for tap ID: %s".formatted(line[0]), e);
                }
            }
        } catch (final Exception e) {
            log.error("Error reading from CSV file: {}", inputFilePath, e);
            throw new CsvProcessingException("Error reading from CSV file: %s".formatted(inputFilePath), e);
        }
        sortByDatetime(taps);
        return taps;
    }

    private static void sortByDatetime(final List<Tap> taps) {
        taps.sort(Comparator.comparing(Tap::getLocalDateTime));
    }

    public void writeTripsToCsv(final List<Trip> trips, final String outputFilePath) {
        try (final CSVWriter writer = new CSVWriter(new FileWriter(outputFilePath))) {
            log.info("Writing trips to CSV file: {}", outputFilePath);
            writer.writeNext(TRIP_CSV_HEADER, false);

            for (final Trip trip : trips) {
                writer.writeNext(convertTripToCsvLine(trip), false);
            }

        } catch (final IOException e) {
            log.error("Error writing to CSV file: {}", outputFilePath, e);
            throw new CsvProcessingException("Error writing to CSV file: %s".formatted(outputFilePath), e);
        }
    }

    private Tap parseCsvLineToTap(final String[] line) throws DateTimeParseException, IllegalArgumentException {
        final long id = Long.parseLong(line[0]);
        final LocalDateTime dateTime = LocalDateTime.parse(line[1], formatter);
        final TapType tapType = TapType.valueOf(line[2]);
        final String stopId = line[3];
        final String companyId = line[4];
        final String busId = line[5];
        final String pan = line[6];

        return new Tap(id, dateTime, tapType, stopId, companyId, busId, pan);
    }

    private String[] convertTripToCsvLine(final Trip trip) {
        return new String[]{
                trip.getStarted() != null ? trip.getStarted().format(formatter) : "",
                trip.getFinished() != null ? trip.getFinished().format(formatter) : "",
                String.valueOf(trip.getDurationSecs()),
                trip.getFromStopId() != null ? trip.getFromStopId() : "",
                trip.getToStopId() != null ? trip.getToStopId() : "",
                String.valueOf(trip.getChargeAmount()),
                trip.getCompanyId(),
                trip.getBusId(),
                trip.getPan(),
                trip.getStatus().toString()
        };
    }
}
