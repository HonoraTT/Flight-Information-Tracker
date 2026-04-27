package com.example.flightinformationtracker;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.example.flightinformationtracker.mapper")
public class FlightInformationTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlightInformationTrackerApplication.class, args);
    }

}
