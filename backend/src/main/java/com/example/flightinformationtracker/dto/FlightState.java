package com.example.flightinformationtracker.dto;

/**
 * 实时航班状态数据契约（A/B/C/D共享）。
 */
public class FlightState {
    /** 飞机唯一标识（ICAO24） */
    private String icao24;

    /** 航班呼号（如 CCA1234，可能为空） */
    private String callsign;

    /** 飞机注册所属国家/来源国家 */
    private String originCountry;

    /** 经度（WGS84） */
    private Double longitude;

    /** 纬度（WGS84） */
    private Double latitude;

    /** 气压高度（单位：米） */
    private Double baroAltitude;

    /** 地速（单位：米/秒） */
    private Double velocity;

    /** 航向角（单位：度，0-360） */
    private Double heading;

    /** 是否在地面（true=地面，false=空中） */
    private Boolean onGround;

    /** 位置时间戳（Unix 秒） */
    private Long timePosition;

    /** 最后联系时间戳（Unix 秒） */
    private Long lastContact;

    /**
     * 数据是否为过期缓存。
     * true 表示当前为降级使用的缓存数据（如429/上游异常）；
     * false 表示新鲜实时数据。
     */
    private Boolean stale;

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

    public String getOriginCountry() {
        return originCountry;
    }

    public void setOriginCountry(String originCountry) {
        this.originCountry = originCountry;
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

    public Long getTimePosition() {
        return timePosition;
    }

    public void setTimePosition(Long timePosition) {
        this.timePosition = timePosition;
    }

    public Long getLastContact() {
        return lastContact;
    }

    public void setLastContact(Long lastContact) {
        this.lastContact = lastContact;
    }

    public Boolean getStale() {
        return stale;
    }

    public void setStale(Boolean stale) {
        this.stale = stale;
    }
}
