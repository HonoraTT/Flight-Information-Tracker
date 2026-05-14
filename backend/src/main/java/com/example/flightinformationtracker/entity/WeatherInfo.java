package com.example.flightinformationtracker.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;


@Data
public class WeatherInfo {

    @JsonProperty("name")
    private String cityName;

    @JsonProperty("main")
    private MainData main;

    @JsonProperty("wind")
    private WindData wind;

    @JsonProperty("clouds")
    private CloudsData clouds;

    @JsonProperty("visibility")
    private Integer visibility;

    @JsonProperty("weather")
    private List<WeatherDescription> weather;

    @JsonProperty("rain")
    private RainData rain;

    @JsonProperty("snow")
    private SnowData snow;

    @JsonProperty("coord")
    private Coord coord;

    @JsonProperty("dt")
    private Long dt;  // 数据时间戳

    @JsonProperty("sys")
    private Sys sys;

    @Data
    public static class MainData {
        private Double temp;
        @JsonProperty("feels_like")
        private Double feelsLike;
        private Integer pressure;
        private Integer humidity;
    }

    @Data
    public static class WindData {
        private Double speed;
        private Integer deg;
        private Double gust;
    }

    @Data
    public static class CloudsData {
        @JsonProperty("all")
        private Integer coverage;
    }

    @Data
    public static class WeatherDescription {
        private String main;
        private String description;
        private String icon;
    }

    @Data
    public static class RainData {
        @JsonProperty("1h")
        private Double oneHour;
    }

    @Data
    public static class SnowData {
        @JsonProperty("1h")
        private Double oneHour;
    }

    @Data
    public static class Coord {
        private Double lon;
        private Double lat;
    }

    @Data
    public static class Sys {
        private String country;
        private Long sunrise;
        private Long sunset;
    }

    // ========== 辅助方法，方便前端获取常用字段 ==========

    public Double getTemperature() {
        return main != null ? main.getTemp() : null;
    }

    public Double getFeelsLike() {
        return main != null ? main.getFeelsLike() : null;
    }

    public Integer getPressure() {
        return main != null ? main.getPressure() : null;
    }

    public Integer getHumidity() {
        return main != null ? main.getHumidity() : null;
    }

    public Double getWindSpeed() {
        return wind != null ? wind.getSpeed() : null;
    }

    public Integer getWindDeg() {
        return wind != null ? wind.getDeg() : null;
    }

    public Double getWindGust() {
        return wind != null ? wind.getGust() : null;
    }

    public Integer getClouds() {
        return clouds != null ? clouds.getCoverage() : null;
    }

    public Double getPrecipitation() {
        if (rain != null && rain.getOneHour() != null) return rain.getOneHour();
        if (snow != null && snow.getOneHour() != null) return snow.getOneHour();
        return 0.0;
    }

    public String getWindScale() {
        Double speed = getWindSpeed();
        if (speed == null) return "未知";
        if (speed < 0.3) return "0级（无风）";
        if (speed < 1.6) return "1级（软风）";
        if (speed < 3.4) return "2级（轻风）";
        if (speed < 5.5) return "3级（微风）";
        if (speed < 8.0) return "4级（和风）";
        if (speed < 10.8) return "5级（清风）";
        if (speed < 13.9) return "6级（强风）";
        return "7级以上（劲风）";
    }

    public String getWeatherDescription() {
        if (weather != null && !weather.isEmpty()) {
            return weather.get(0).getDescription();
        }
        return "未知";
    }

    public String getWeatherIcon() {
        if (weather != null && !weather.isEmpty()) {
            return weather.get(0).getIcon();
        }
        return "01d";
    }
}