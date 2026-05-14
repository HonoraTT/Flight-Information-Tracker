package com.example.flightinformationtracker.controller;

import com.example.flightinformationtracker.dto.FlightState;
import com.example.flightinformationtracker.service.FlightMergeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightController {
    @Autowired
    private FlightMergeService flightMergeService;
    /**
     * 查询实时航班列表，支持多条件筛选
     * @param callsign  航班号（可选）
     * @param icao24    飞机唯一编码（可选）
     * @param country   国籍/地区（可选）
     * @return 筛选后的实时航班列表
     */
    @GetMapping
    public ResponseEntity<List<FlightState>> getFlights(
            @RequestParam(value = "callsign", required = false) String callsign,
            @RequestParam(value = "icao24", required = false) String icao24,
            @RequestParam(value = "country", required = false) String country) {

        List<FlightState> flights = flightMergeService.getCachedMergedFlights();

        if (StringUtils.hasText(callsign)) {
            String c = callsign.trim().toLowerCase();
            flights = flights.stream()
                    .filter(f -> f.getCallsign() != null && f.getCallsign().trim().toLowerCase().equals(c))
                    .collect(Collectors.toList());
        }

        if (StringUtils.hasText(icao24)) {
            String i = icao24.trim().toLowerCase();
            flights = flights.stream()
                    .filter(f -> f.getIcao24() != null && f.getIcao24().toLowerCase().equals(i))
                    .collect(Collectors.toList());
        }

        if (StringUtils.hasText(country)) {
            String ct = country.trim().toLowerCase();
            flights = flights.stream()
                    .filter(f -> f.getOriginCountry() != null && f.getOriginCountry().toLowerCase().contains(ct))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(flights);
    }
}