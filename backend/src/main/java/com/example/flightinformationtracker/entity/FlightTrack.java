package com.example.flightinformationtracker.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 航班轨迹实体类
 */
@Data
public class FlightTrack {

    /** 主键ID，自增 */
    private Long id;

    /** 飞机唯一24位ICAO编码（全球唯一标识） */
    private String icao24;

    /** 航班呼号/航班号（如 CCA101） */
    private String callsign;

    /** 经度 */
    private Double longitude;

    /** 纬度 */
    private Double latitude;

    /** 气压高度（单位：米） */
    private Double baroAltitude;

    /** 飞行速度（单位：米/秒） */
    private Double velocity;

    /** 飞行航向/角度（0-360度） */
    private Double heading;

    /** 是否在地面上 true-地面 false-空中 */
    private Boolean onGround;

    /** 飞机所属国家/起源国 */
    private String originCountry;

    /** 数据记录时间（入库时间） */
    private LocalDateTime recordTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIcao24() {
        return icao24;
    }

    public void setIcao24(String icao24) {
        this.icao24 = icao24;
    }

    public String getCallsign() {
        return callsign;
    }

    public void setCallsign(String callsign) {
        this.callsign = callsign;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getBaroAltitude() {
        return baroAltitude;
    }

    public void setBaroAltitude(Double baroAltitude) {
        this.baroAltitude = baroAltitude;
    }

    public Double getVelocity() {
        return velocity;
    }

    public void setVelocity(Double velocity) {
        this.velocity = velocity;
    }

    public Double getHeading() {
        return heading;
    }

    public void setHeading(Double heading) {
        this.heading = heading;
    }

    public Boolean getOnGround() {
        return onGround;
    }

    public void setOnGround(Boolean onGround) {
        this.onGround = onGround;
    }

    public String getOriginCountry() {
        return originCountry;
    }

    public void setOriginCountry(String originCountry) {
        this.originCountry = originCountry;
    }

    public LocalDateTime getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(LocalDateTime recordTime) {
        this.recordTime = recordTime;
    }
}