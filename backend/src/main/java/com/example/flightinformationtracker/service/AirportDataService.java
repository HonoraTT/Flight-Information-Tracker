package com.example.flightinformationtracker.service;

import com.example.flightinformationtracker.entity.AirportInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 机场基础数据加载服务。
 */
@Service
public class AirportDataService {
    private static final Logger log = LoggerFactory.getLogger(AirportDataService.class);

    private final ObjectMapper objectMapper;
    private List<AirportInfo> airports = Collections.emptyList();

    public AirportDataService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void loadAirports() {
        try (InputStream inputStream = new ClassPathResource("data/china-airports.json").getInputStream()) {
            List<AirportInfo> loaded = objectMapper.readValue(inputStream, new TypeReference<List<AirportInfo>>() {});
            airports = Collections.unmodifiableList(new ArrayList<>(loaded));
            log.info("加载国内机场数据 {} 个", airports.size());
        } catch (Exception e) {
            airports = Collections.emptyList();
            log.error("加载国内机场数据失败：{}", e.getMessage(), e);
        }
    }

    public List<AirportInfo> getAirports() {
        return airports;
    }
}
