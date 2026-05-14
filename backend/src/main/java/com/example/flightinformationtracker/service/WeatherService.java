package com.example.flightinformationtracker.service;

import com.example.flightinformationtracker.entity.WeatherInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    @Qualifier("weatherWebClientBuilder")
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.api.base-url:http://api.openweathermap.org/data/2.5}")
    private String baseUrl;

    /**
     * 根据经纬度获取当前天气（使用 2.5/weather 接口）
     * @param lat 纬度
     * @param lon 经度
     * @return 天气信息
     */
    @Cacheable(value = "weather", key = "'weather_' + #lat + '_' + #lon", unless = "#result == null")
    public WeatherInfo getWeatherByCoordinates(double lat, double lon) {
        log.info("调用 OpenWeatherMap 2.5/weather API，坐标：{}, {}", lat, lon);

        try {
            WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();

            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/weather")
                            .queryParam("lat", lat)
                            .queryParam("lon", lon)
                            .queryParam("appid", apiKey)
                            .queryParam("units", "metric")  // 摄氏度
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response == null) {
                log.warn("天气API返回空响应");
                return null;
            }

            // 直接使用 ObjectMapper 将 JSON 映射到我们定义好的 WeatherInfo 类
            WeatherInfo weatherInfo = objectMapper.readValue(response, WeatherInfo.class);

            log.info("成功获取天气数据，城市：{}，温度：{}℃",
                    weatherInfo.getCityName(), weatherInfo.getTemperature());

            return weatherInfo;

        } catch (WebClientResponseException e) {
            log.error("天气API请求失败，状态码：{}，响应：{}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            return null;
        } catch (Exception e) {
            log.error("获取天气数据失败：{}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 根据城市名称获取天气（使用 2.5/weather 接口）
     * @param cityName 城市名称（英文或中文拼音，如 Beijing, London）
     * @return 天气信息
     */
    @Cacheable(value = "weather", key = "'weather_city_' + #cityName", unless = "#result == null")
    public WeatherInfo getWeatherByCityName(String cityName) {
        log.info("调用 OpenWeatherMap 2.5/weather API，城市：{}", cityName);

        try {
            WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();

            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/weather")
                            .queryParam("q", cityName)
                            .queryParam("appid", apiKey)
                            .queryParam("units", "metric")  // 摄氏度
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response == null) {
                log.warn("天气API返回空响应");
                return null;
            }

            WeatherInfo weatherInfo = objectMapper.readValue(response, WeatherInfo.class);
            log.info("成功获取天气数据，城市：{}，温度：{}℃",
                    weatherInfo.getCityName(), weatherInfo.getTemperature());

            return weatherInfo;

        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                log.warn("未找到城市：{}", cityName);
            } else {
                log.error("天气API请求失败，状态码：{}", e.getStatusCode(), e);
            }
            return null;
        } catch (Exception e) {
            log.error("获取天气数据失败：{}", e.getMessage(), e);
            return null;
        }
    }
}