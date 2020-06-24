package com.ldchina.datacenter.dao.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.ldchina.datacenter.dao.entity.StationInfo;

import java.util.Date;
import java.util.List;

@Mapper
public interface QxStationMapper {
    @Select("SELECT * FROM `qx_station` WHERE stationId=#{stationId} LIMIT 1")
    StationInfo getStationLastObtime(@Param("stationId")String stationId);
    @Select("SELECT * FROM `qx_station` ORDER BY stationId")
    List<StationInfo> getAllStations();
    @Update("UPDATE `qx_station` set obTime=#{obtime} WHERE stationId=#{stationid}")
    void updateObtime(@Param("obtime") Date obtime,@Param("stationid") String stationid);
    @Insert("MERGE into `qx_station` (stationId,obTime,type)values(#{stationid},#{obtime},#{type})")
    void saveStationInfo(StationInfo stationInfo);
    @Select("SELECT * FROM `qx_station` WHERE stationId=#{stationId}")
    StationInfo getStationInfoById(@Param("stationId")String stationId);
    @Update("UPDATE `qx_station` set alias=#{alias},lng=#{lng},lat=#{lat},protocol=#{protocol} WHERE stationId=#{stationid}")
    int updateStationInfoById(StationInfo stationInfo);
    @Delete("DELETE FROM `qx_station` WHERE stationid=#{stationid}")
    int deleteStationById(@Param("stationid")String stationid);
    @Select("SELECT TOP 1 `types` FROM `qx_station`")
    String getStationSensorType();
}
