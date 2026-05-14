package com.example.flightinformationtracker.entity;

/**
 * 机场基础信息，用于国内模拟航班生成。
 */
public class AirportInfo {
    private String code;
    private String name;
    private Double lon;
    private Double lat;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }
}
