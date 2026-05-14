package com.example.flightinformationtracker.controller;

import com.example.flightinformationtracker.entity.WeatherInfo;
import com.example.flightinformationtracker.service.WeatherService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    /**
     * 根据经纬度获取天气（前端地图点击时调用）
     * GET /api/weather/coordinates?lat=35.68&lon=139.76
     */
    @GetMapping("/coordinates")
    public ResponseEntity<WeatherInfo> getWeatherByCoordinates(
            @RequestParam double lat,
            @RequestParam double lon) {

        WeatherInfo weather = weatherService.getWeatherByCoordinates(lat, lon);
        if (weather == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(weather);
    }

    /**
     * 根据城市名称获取天气
     * GET /api/weather/city?name=Tokyo
     */
    @GetMapping("/city")
    public ResponseEntity<WeatherInfo> getWeatherByCity(@RequestParam String name) {
        WeatherInfo weather = weatherService.getWeatherByCityName(name);
        if (weather == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(weather);
    }

    /**
     * 批量获取多个城市的天气
     */
    @PostMapping("/batch")
    public ResponseEntity<Map<String, WeatherInfo>> getBatchWeather(
            @RequestBody List<WeatherRequest> requests) {

        Map<String, WeatherInfo> result = new HashMap<>();
        for (WeatherRequest req : requests) {
            WeatherInfo weather;
            if (req.getLat() != null && req.getLon() != null) {
                weather = weatherService.getWeatherByCoordinates(req.getLat(), req.getLon());
            } else if (req.getCityName() != null) {
                weather = weatherService.getWeatherByCityName(req.getCityName());
            } else {
                continue;
            }
            result.put(req.getKey() != null ? req.getKey() : (req.getCityName() != null ? req.getCityName() : req.getLat() + "," + req.getLon()), weather);
        }
        return ResponseEntity.ok(result);
    }

    @Data
    public static class WeatherRequest {
        private Double lat;
        private Double lon;
        private String cityName;
        private String key;  // 自定义返回的key
    }
}