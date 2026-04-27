CREATE DATABASE IF NOT EXISTS flight_tracker
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE flight_tracker;

-- 历史轨迹点表
CREATE TABLE IF NOT EXISTS flight_track (
                                            id              BIGINT AUTO_INCREMENT PRIMARY KEY,
                                            icao24          VARCHAR(10)  NOT NULL COMMENT '飞机唯一ID',
    callsign        VARCHAR(20)           COMMENT '呼号（可能为空）',
    longitude       DOUBLE       NOT NULL COMMENT '经度',
    latitude        DOUBLE       NOT NULL COMMENT '纬度',
    baro_altitude   DOUBLE                COMMENT '气压高度（米）',
    velocity        DOUBLE                COMMENT '地速（米/秒）',
    heading         DOUBLE                COMMENT '航向（0-360度）',
    on_ground       TINYINT(1)   DEFAULT 0 COMMENT '是否在地面',
    origin_country  VARCHAR(60)           COMMENT '出发国',
    record_time     DATETIME     NOT NULL COMMENT '记录时间（后端写入）',
    INDEX idx_icao24 (icao24),
    INDEX idx_record_time (record_time),
    INDEX idx_icao24_time (icao24, record_time)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='历史轨迹点表';

-- 收藏表（选做，但你要求先建好）
CREATE TABLE IF NOT EXISTS user_favorite (
                                             id           BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             session_id   VARCHAR(64) NOT NULL COMMENT '前端会话ID',
    icao24       VARCHAR(10) NOT NULL COMMENT '关注飞机ICAO24',
    callsign     VARCHAR(20)          COMMENT '呼号备注',
    added_time   DATETIME    NOT NULL COMMENT '添加时间',
    INDEX idx_session (session_id),
    INDEX idx_session_icao24 (session_id, icao24)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';