package com.littlepay.tapfare;

import com.littlepay.tapfare.config.TripsCsvConfig;
import com.littlepay.tapfare.config.TripsFareConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({TripsCsvConfig.class, TripsFareConfig.class})
public class TapFareApplication {

    public static void main(final String[] args) {
        SpringApplication.run(TapFareApplication.class, args);
    }

}
