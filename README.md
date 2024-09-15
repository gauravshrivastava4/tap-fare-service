# LittlePay Tap Fare Processing Service

## Overview

The **LittlePay Tap Fare Processing Service** is a Java-based system designed to process transit tap-on and tap-off
data, calculate trip fares, and generate a CSV file containing details of the processed trips. This system reads raw tap
data (tap-on and tap-off events), determines the status of trips (completed, incomplete, or canceled), and calculates
the fare for each trip based on a predefined fare matrix.

## Assumptions

- Processing concurrent taps are out of scope.
- In order to consider a trip COMPLETE, tap-on and tap-off should happen at the same day
- One csv file will contain data only for one Company
- CSV may have orphan ONs and OFFs
- CSV will not have data sorted by time
- CSV can have multiple COMPLETED trips for a single PAN, hence sorting the CSV by localdatetime before processing
- There is no need to consider different timezones
- Input and Output files can be stored within the project

## Improvement Opportunities

- We can use kafka for parallel streaming of taps, we can publish the taps on to the kafka topic while reading the taps
  from csv.
- Kafka consumer can be created for consuming the taps and persist it
- A separate process can be created to create the trips and write it to CSV
- Fare details can also be stored in db

## Features

- **Tap Processing**: Processes tap-on and tap-off events to create trips.
- **Fare Calculation**: Calculates fares based on the distance between stops and pre-defined fare rules.
- **CSV Import/Export**: Reads raw tap data from a CSV file, sorts them by time and writes the processed
  trip data to a CSV file.
- **Error Handling**: Gracefully handles cases like orphan tap-on or tap-off events.
- **Symmetric Fare Matrix**: Ensures that fares from Stop A to Stop B are the same as from Stop B to Stop A.

## Project Structure

### 1. **Services**

- **TapsProcessor**: This service orchestrates the reading of taps from a CSV file, creating trips based on tap data,
  and exporting the trip results to another CSV.
- **TripsCreationService**: This service handles the core logic of creating trips from tap events. It detects and
  processes different types of trips:
    - **Completed Trip**: Both tap-on and tap-off are present, with different stops.
    - **Canceled Trip**: Tap-on and tap-off occurred at the same stop.
    - **Incomplete Trip**: Only a tap-on or tap-off exists without its counterpart.
- **Fare Calculation**: The `DefaultFareCalculator` calculates the fare for trips using the predefined fare matrix from
  `FareMatrixUtils`.

### 2. **Utilities**

- **CsvUtils**: Reads tap data from a CSV file and writes processed trip data to another CSV file.
- **FareMatrixUtils**: Loads a fare matrix from configuration and ensures symmetric fare calculation between stops.

### 3. **Models**

- **Tap**: Represents a tap-on or tap-off event.
- **Trip**: Represents a trip created from one or more taps, including details like duration, stops, fare, and trip
  status (completed, canceled, or incomplete).

### 4. **Configuration**

- **TripsFareConfig**: Configures fare rules between different stops.
- **TripsCsvConfig**: Configure csv input and output file path

## How It Works

1. **Reading Tap Data**:
    - Tap data is imported from a CSV file. Each tap record includes details like tap time, type (on or off), stop ID,
      company ID, bus ID, and PAN (Payment Account Number).
    - Data is then sorted by time

2. **Trip Processing**:
    - The system processes each tap event, looks for a corresponding tap-off or tap-onn event happened at the same day,
      and generates a trip.
    - If no corresponding tap-off or tap-on is found, the trip is marked as **incomplete**.
    - If tap-on and tap-off happen at the same stop, the trip is marked as **canceled**.

3. **Fare Calculation**:
    - For completed trips, the fare is calculated based on the distance between the start and end stops using a
      predefined fare matrix.
    - For incomplete trips, the maximum possible fare from the starting stop is charged.

4. **Output Generation**:
    - After processing, the system writes the resulting trip data to an output CSV file, containing details of each trip
      such as duration, stops, fare, and trip status.

## Project Setup

### Prerequisites

- Java 17
- Maven 3.6+
- Spring Boot 3.3.3

### Running the Project

1. **Clone the Repository**:

   ```bash
   git clone https://github.com/gauravshrivastava4/tap-fare-service.git
   cd tap-fare-service
   ```

2. **Configure the Fare Matrix**:

   Update the `application.yml` configuration file with the fare matrix between different stops. Example:

   ```yaml
   tripsFare:
     fare:
       Stop1:
         Stop2: 3.25
         Stop3: 7.00
       Stop2:
         Stop1: 3.25
         Stop3: 5.50
   ```

3. **Input and Output Files**:
    - Place your input CSV file with tap data in the directory specified by `inputFilePath` in the `application.yml`.
    - The processed trip data will be generated and saved to the file specified by `outputFilePath` in the
      configuration.


4. **Build the Project**:

    ```bash
    mvn clean install
    ```

5. **Start the application**:

    ```bash
    mvn spring-boot:run
    ```

6. **Execute Curl**:

    ```bash
    Invoke-WebRequest -Uri http://localhost:8080/taps/process -Method POST
    ```

    ```bash
    curl -X POST http://localhost:8080/taps/process
    ```   
   *[processTaps.http](httpRequests/processTaps.http) Can also be used to start the process*

## Example CSV Format

### [input-file](src/main/resources/taps.csv) Input CSV (Tap Data)

```
ID,DateTimeUTC,TapType,StopId,CompanyId,BusID,PAN
1,22-01-2023 13:00:00,ON,Stop1,Company1,Bus37,5500005555555559
2,22-01-2023 13:05:00,OFF,Stop2,Company1,Bus37,5500005555555559
3,22-01-2023 09:20:00,ON,Stop3,Company1,Bus36,4111111111111111
4,23-01-2023 08:00:00,ON,Stop1,Company1,Bus37,4111111111111111
5,23-01-2023 08:02:00,OFF,Stop1,Company1,Bus37,4111111111111111
6,24-01-2023 16:30:00,OFF,Stop2,Company1,Bus37,5500005555555559
...
```

### [output-file](src/main/resources/trips.csv) Output CSV (Processed Trip Data)

```
Started,Finished,DurationSecs,FromStopId,ToStopId,ChargeAmount,CompanyId,BusID,PAN,Status
22-01-2023 13:00:00,22-01-2023 13:05:00,300,Stop1,Stop2,3.25,Company1,Bus37,5500005555555559,COMPLETED
23-01-2023 08:00:00,23-01-2023 08:02:00,0,Stop1,Stop1,0.0,Company1,Bus37,4111111111111111,CANCELLED
22-01-2023 09:20:00,,0,Stop3,,7.3,Company1,Bus36,4111111111111111,INCOMPLETE
,24-01-2023 16:30:00,0,,Stop2,5.5,Company1,Bus37,5500005555555559,INCOMPLETE
...
```

## Testing

This project includes unit tests to validate the main logic and ensure the accuracy of the fare calculation and trip
processing.

To run the tests:

```bash
mvn test
```

### Key Tests

- **TripsCreationServiceTest**: Tests for different trip scenarios (completed, incomplete, canceled).
- **FareMatrixUtilsTest**: Ensures that the fare matrix is correctly populated and symmetric.
- **CsvUtilsTest**: Validates reading and writing CSV files.
- **DefaultFareCalculatorTest**: Tests fare calculation based on different stops.