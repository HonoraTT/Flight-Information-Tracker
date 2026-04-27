package com.example.flightinformationtracker.service;

import com.example.flightinformationtracker.dto.FlightState;
import com.example.flightinformationtracker.entity.FlightTrack;
import com.example.flightinformationtracker.mapper.FlightTrackMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackStorageService {
    @Autowired
    private FlightTrackMapper trackMapper;
    //异步批量保存实时航班轨迹数据
    @Async("taskExecutor")
    public void batchSaveAsync(List<FlightState> flights) {
        if (flights == null || flights.isEmpty()) return;

        int inserted = 0;
        for (FlightState f : flights) {
            if (f.getLatitude() == null || f.getLongitude() == null) continue;
            // 建议只存在空飞机
            if (Boolean.TRUE.equals(f.getOnGround())) continue;

            FlightTrack t = new FlightTrack();
            t.setIcao24(f.getIcao24());
            t.setCallsign(f.getCallsign() == null ? null : f.getCallsign().trim());
            t.setLongitude(f.getLongitude());
            t.setLatitude(f.getLatitude());
            t.setBaroAltitude(f.getBaroAltitude());
            t.setVelocity(f.getVelocity());
            t.setHeading(f.getHeading());
            t.setOnGround(f.getOnGround());
            t.setOriginCountry(f.getOriginCountry());
            t.setRecordTime(LocalDateTime.now());

            trackMapper.insert(t);
            inserted++;
        }
//        log.info("track batch saved, input={}, inserted={}", flights.size(), inserted);
    }
    //定时清理过期的历史轨迹数据
    public int clearBeforeDays(int days) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(days);
        return trackMapper.deleteBefore(threshold);
    }
}