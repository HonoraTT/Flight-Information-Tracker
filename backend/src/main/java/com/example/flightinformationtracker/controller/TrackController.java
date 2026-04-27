package com.example.flightinformationtracker.controller;

import com.example.flightinformationtracker.entity.FlightTrack;
import com.example.flightinformationtracker.mapper.FlightTrackMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/tracks")
@RequiredArgsConstructor
public class TrackController {
    @Autowired
    private FlightTrackMapper trackMapper;
    //根据传入的航班编号 + 时间范围，返回该航班的历史轨迹点列表
    @GetMapping("/{icao24}")
    public ResponseEntity<List<FlightTrack>> getTrack(
            @PathVariable(value = "icao24") String icao24,
            @RequestParam(value = "start", required = false) Long start,   // epoch seconds
            @RequestParam(value = "end", required = false) Long end,     // epoch seconds
            @RequestParam(value = "seconds", required = false, defaultValue = "3600") Long seconds) {

        LocalDateTime endTime;
        LocalDateTime startTime;

        if (start != null && end != null) {
            startTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(start), ZoneId.systemDefault());
            endTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(end), ZoneId.systemDefault());
        } else {
            endTime = LocalDateTime.now();
            startTime = endTime.minusSeconds(seconds == null ? 3600 : seconds);
        }

        List<FlightTrack> tracks = trackMapper.selectByIcao24AndTimeRange(icao24, startTime, endTime);
        return ResponseEntity.ok(tracks);
    }
}