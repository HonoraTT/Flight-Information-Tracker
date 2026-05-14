package com.example.flightinformationtracker.service;

import com.example.flightinformationtracker.dto.FlightState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 定时拉取 OpenSky 数据并广播到 WebSocket。
 */
@Service
public class FlightBroadcastService {
    private static final Logger log = LoggerFactory.getLogger(FlightBroadcastService.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final OpenSkyService openSkyService;
    private final FlightMergeService flightMergeService;

    public FlightBroadcastService(SimpMessagingTemplate messagingTemplate,
                                  OpenSkyService openSkyService,
                                  FlightMergeService flightMergeService) {
        this.messagingTemplate = messagingTemplate;
        this.openSkyService = openSkyService;
        this.flightMergeService = flightMergeService;
    }

    @Scheduled(fixedDelayString = "#{@openSkyService.getPollingIntervalMillis()}")
    public void broadcastFlights() {
        try {
            List<FlightState> flights = flightMergeService.fetchMergedFlights();
            messagingTemplate.convertAndSend("/topic/flights", flights);
            log.info("广播 {} 架飞机数据", flights.size());
        } catch (Exception e) {
            log.error("广播失败：{}", e.getMessage(), e);
        }
    }
}
