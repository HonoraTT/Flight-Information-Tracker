package com.example.flightinformationtracker.controller;

import com.example.flightinformationtracker.dto.FlightState;
import com.example.flightinformationtracker.entity.FlightTrack;
import com.example.flightinformationtracker.mapper.FlightTrackMapper;
import com.example.flightinformationtracker.service.OpenSkyService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {
    @Autowired
    private OpenSkyService openSkyService;
    @Autowired
    private FlightTrackMapper trackMapper;
    //实时航班概况统计
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        List<FlightState> flights = openSkyService.getCachedFlights();
        Map<String, Object> result = new HashMap<>();

        result.put("total", flights.size());

        Map<String, Long> byCountry = flights.stream()
                .collect(Collectors.groupingBy(
                        f -> (f.getOriginCountry() == null || f.getOriginCountry().isBlank()) ? "Unknown" : f.getOriginCountry(),
                        Collectors.counting()
                ));
        result.put("byCountry", byCountry);

        Map<String, Long> altitudeDistribution = new LinkedHashMap<>();
        altitudeDistribution.put("0-3km", 0L);
        altitudeDistribution.put("3-6km", 0L);
        altitudeDistribution.put("6-9km", 0L);
        altitudeDistribution.put("9km+", 0L);

        for (FlightState f : flights) {
            Double alt = f.getBaroAltitude();
            if (alt == null) continue;
            if (alt < 3000) altitudeDistribution.computeIfPresent("0-3km", (k, v) -> v + 1);
            else if (alt < 6000) altitudeDistribution.computeIfPresent("3-6km", (k, v) -> v + 1);
            else if (alt < 9000) altitudeDistribution.computeIfPresent("6-9km", (k, v) -> v + 1);
            else altitudeDistribution.computeIfPresent("9km+", (k, v) -> v + 1);
        }
        result.put("altitudeDistribution", altitudeDistribution);

        return ResponseEntity.ok(result);
    }
    //航班流量时间线
    @GetMapping("/timeline")
    public ResponseEntity<Map<String, Object>> getTimeline(@RequestParam(value = "hours", defaultValue = "1") Integer hours) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusHours(Math.max(1, hours));

        // 这里用了 mapper 里的聚合SQL（复用字段承载）
        List<FlightTrack> rows = trackMapper.selectTimelineCount(start, end);

        List<Map<String, Object>> points = new ArrayList<>();
        for (FlightTrack r : rows) {
            Map<String, Object> p = new HashMap<>();
            p.put("time", r.getCallsign()); // bucket 时间字符串
            p.put("count", r.getVelocity() == null ? 0 : r.getVelocity().longValue());
            points.add(p);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("hours", hours);
        res.put("intervalMinutes", 10);
        res.put("points", points);
        return ResponseEntity.ok(res);
    }
}