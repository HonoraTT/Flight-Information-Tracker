package com.example.flightinformationtracker.service;

import com.example.flightinformationtracker.dto.FlightState;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 合并真实 OpenSky 航班与国内模拟航班。
 */
@Service
public class FlightMergeService {
    private final OpenSkyService openSkyService;
    private final MockChinaFlightService mockChinaFlightService;

    public FlightMergeService(OpenSkyService openSkyService, MockChinaFlightService mockChinaFlightService) {
        this.openSkyService = openSkyService;
        this.mockChinaFlightService = mockChinaFlightService;
    }

    public List<FlightState> fetchMergedFlights() {
        List<FlightState> realFlights = openSkyService.fetchCurrentFlights();
        List<FlightState> mockFlights = mockChinaFlightService.getCurrentMockFlights();
        return merge(realFlights, mockFlights);
    }

    public List<FlightState> getCachedMergedFlights() {
        List<FlightState> realFlights = openSkyService.getCachedFlights();
        List<FlightState> mockFlights = mockChinaFlightService.getCurrentMockFlights();
        return merge(realFlights, mockFlights);
    }

    private List<FlightState> merge(List<FlightState> realFlights, List<FlightState> mockFlights) {
        List<FlightState> merged = new ArrayList<>();
        if (realFlights != null) {
            merged.addAll(realFlights);
        }
        if (mockFlights != null) {
            merged.addAll(mockFlights);
        }
        return merged;
    }
}
