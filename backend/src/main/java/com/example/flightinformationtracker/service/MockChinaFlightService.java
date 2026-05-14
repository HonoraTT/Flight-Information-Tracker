package com.example.flightinformationtracker.service;

import com.example.flightinformationtracker.config.MockChinaFlightConfig;
import com.example.flightinformationtracker.dto.FlightState;
import com.example.flightinformationtracker.entity.AirportInfo;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * 基于国内机场坐标和固定航线规则生成模拟航班。
 */
@Service
public class MockChinaFlightService {
    private static final String[] CALLSIGN_PREFIXES = {"CCA", "CSN", "CES", "CHH", "CDG", "DKH", "CXA", "GCR"};
    private static final double EARTH_RADIUS_KM = 6371.0;

    private final MockChinaFlightConfig config;
    private final AirportDataService airportDataService;
    private final List<MockFlightPlan> plans = new ArrayList<>();
    private int lastPlanCount = -1;
    private int lastAirportCount = -1;

    public MockChinaFlightService(MockChinaFlightConfig config, AirportDataService airportDataService) {
        this.config = config;
        this.airportDataService = airportDataService;
    }

    public List<FlightState> getCurrentMockFlights() {
        if (!config.isEnabled()) {
            return List.of();
        }

        List<AirportInfo> airports = airportDataService.getAirports();
        if (airports.size() < 2) {
            return List.of();
        }

        ensurePlans(airports);

        long now = Instant.now().getEpochSecond();
        List<FlightState> flights = new ArrayList<>(plans.size());
        for (MockFlightPlan plan : plans) {
            flights.add(toFlightState(plan, now));
        }
        return flights;
    }

    private synchronized void ensurePlans(List<AirportInfo> airports) {
        int count = Math.max(0, config.getCount());
        if (lastPlanCount == count && lastAirportCount == airports.size() && plans.size() == count) {
            return;
        }

        plans.clear();
        Random random = new Random(20260427L);
        for (int i = 0; i < count; i++) {
            AirportInfo from = airports.get(random.nextInt(airports.size()));
            AirportInfo to = airports.get(random.nextInt(airports.size()));
            int guard = 0;
            while (sameAirport(from, to) && guard++ < 20) {
                to = airports.get(random.nextInt(airports.size()));
            }

            double distanceKm = distanceKm(from.getLat(), from.getLon(), to.getLat(), to.getLon());
            long durationSeconds = Math.max(1800L, Math.round(distanceKm / 780.0 * 3600.0));
            long offsetSeconds = random.nextLong(Math.max(durationSeconds, 1L));
            double cruiseAltitude = 7800 + random.nextInt(4200);
            double velocity = 205 + random.nextInt(65);
            String callsign = CALLSIGN_PREFIXES[i % CALLSIGN_PREFIXES.length] + String.format(Locale.ROOT, "%04d", 1000 + i);
            String icao24 = String.format(Locale.ROOT, "mock-cn-%04d", i + 1);

            plans.add(new MockFlightPlan(icao24, callsign, from, to, durationSeconds, offsetSeconds, cruiseAltitude, velocity));
        }

        lastPlanCount = count;
        lastAirportCount = airports.size();
    }

    private FlightState toFlightState(MockFlightPlan plan, long now) {
        long elapsed = (now + plan.offsetSeconds()) % plan.durationSeconds();
        double progress = elapsed / (double) plan.durationSeconds();

        double lat = interpolate(plan.from().getLat(), plan.to().getLat(), progress);
        double lon = interpolate(plan.from().getLon(), plan.to().getLon(), progress);

        FlightState state = new FlightState();
        state.setIcao24(plan.icao24());
        state.setCallsign(plan.callsign());
        state.setOriginCountry("China");
        state.setLatitude(lat);
        state.setLongitude(lon);
        state.setBaroAltitude(calculateAltitude(plan.cruiseAltitude(), progress));
        state.setVelocity(calculateVelocity(plan.velocity(), progress));
        state.setHeading(bearing(plan.from().getLat(), plan.from().getLon(), plan.to().getLat(), plan.to().getLon()));
        state.setOnGround(false);
        state.setTimePosition(now);
        state.setLastContact(now);
        state.setStale(false);
        return state;
    }

    private double calculateAltitude(double cruiseAltitude, double progress) {
        if (progress < 0.12) {
            return cruiseAltitude * (progress / 0.12);
        }
        if (progress > 0.88) {
            return cruiseAltitude * ((1.0 - progress) / 0.12);
        }
        return cruiseAltitude;
    }

    private double calculateVelocity(double cruiseVelocity, double progress) {
        if (progress < 0.08) {
            return cruiseVelocity * 0.55;
        }
        if (progress > 0.92) {
            return cruiseVelocity * 0.50;
        }
        return cruiseVelocity;
    }

    private boolean sameAirport(AirportInfo a, AirportInfo b) {
        return a.getCode() != null && a.getCode().equals(b.getCode());
    }

    private double interpolate(double start, double end, double progress) {
        return start + (end - start) * progress;
    }

    private double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double rLat1 = Math.toRadians(lat1);
        double rLat2 = Math.toRadians(lat2);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(rLat1) * Math.cos(rLat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    private double bearing(double lat1, double lon1, double lat2, double lon2) {
        double rLat1 = Math.toRadians(lat1);
        double rLat2 = Math.toRadians(lat2);
        double dLon = Math.toRadians(lon2 - lon1);
        double y = Math.sin(dLon) * Math.cos(rLat2);
        double x = Math.cos(rLat1) * Math.sin(rLat2)
                - Math.sin(rLat1) * Math.cos(rLat2) * Math.cos(dLon);
        return (Math.toDegrees(Math.atan2(y, x)) + 360.0) % 360.0;
    }

    private record MockFlightPlan(
            String icao24,
            String callsign,
            AirportInfo from,
            AirportInfo to,
            long durationSeconds,
            long offsetSeconds,
            double cruiseAltitude,
            double velocity
    ) {
    }
}
