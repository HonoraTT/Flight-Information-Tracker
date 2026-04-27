package com.example.flightinformationtracker.mapper;

import com.example.flightinformationtracker.entity.FlightTrack;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

//@Mapper
//public interface FlightTrackMapper extends BaseMapper<FlightTrack> {
//    /**
//     * 根据航班ICAO码和时间范围，查询该航班在指定时间段内的所有轨迹数据
//     * @param icao24 航班唯一24位ICAO编码
//     * @param start  查询开始时间
//     * @param end    查询结束时间
//     * @return 按记录时间升序排列的航班轨迹列表
//     */
//    List<FlightTrack> selectByIcao24AndTimeRange(@Param("icao24") String icao24,
//                                                 @Param("start") LocalDateTime start,
//                                                 @Param("end") LocalDateTime end);
//    /**
//     * 按每10分钟分桶统计航班流量，返回每个时间段内的航班记录数量
//     * @param start 统计开始时间
//     * @param end   统计结束时间
//     * @return 时间段统计结果列表
//     */
//    List<FlightTrack> selectTimelineCount(@Param("start") LocalDateTime start,
//                                          @Param("end") LocalDateTime end);
//    /**
//     * 删除指定时间阈值之前的历史航班轨迹数据（用于数据清理）
//     * @param threshold 时间阈值，删除此时间之前的所有记录
//     * @return 被删除的数据条数
//     */
//    int deleteBefore(@Param("threshold") LocalDateTime threshold);
//}
@Mapper
public interface FlightTrackMapper {

    int insert(FlightTrack flightTrack);

    FlightTrack selectById(Long id);

    List<FlightTrack> selectAll();

    List<FlightTrack> selectByIcao24AndTimeRange(@Param("icao24") String icao24,
                                                 @Param("start") LocalDateTime start,
                                                 @Param("end") LocalDateTime end);

    List<FlightTrack> selectTimelineCount(@Param("start") LocalDateTime start,
                                                    @Param("end") LocalDateTime end);

    int deleteBefore(@Param("threshold") LocalDateTime threshold);

    List<FlightTrack> findByCallsign(@Param("callsign") String callsign);

    List<FlightTrack> findByIcao24(@Param("icao24") String icao24);
}