package com.example.flightinformationtracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FlightInformationTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlightInformationTrackerApplication.class, args);
    }

}
