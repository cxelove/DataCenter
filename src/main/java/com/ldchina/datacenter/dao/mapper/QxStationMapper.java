package com.ldchina.datacenter.dao.mapper;

import com.ldchina.datacenter.dao.entity.StationState;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;


import java.util.Date;
import java.util.List;

@Mapper
public interface QxStationMapper {
    @Select("SELECT * FROM `qx_station` WHERE stationId=#{stationId} LIMIT 1")
    StationState getStationLastObtime(@Param("stationId")String stationId);
    @Select("SELECT * FROM `qx_station` ORDER BY protocol asc")
    List<StationState> getAllStations();
    @Update("UPDATE `qx_station` set obTime=#{obtime} WHERE stationId=#{stationid}")
    void updateObtime(@Param("obtime") Date obtime,@Param("stationid") String stationid);
    @Insert("MERGE into `qx_station` (stationId,obTime,type)values(#{stationid},#{obtime},#{type})")
    void savestationState(StationState stationState);
    @Select("SELECT * FROM `qx_station` WHERE stationId=#{stationId}")
    StationState getstationStateById(@Param("stationId")String stationId);
    @Update("UPDATE `qx_station` set alias=#{alias},lng=#{lng},lat=#{lat},protocol=#{protocol} WHERE stationId=#{stationid}")
    int updatestationStateById(StationState stationState);
    @Delete("DELETE FROM `qx_station` WHERE stationid=#{stationid}")
    int deleteStationById(@Param("stationid")String stationid);
    @Select("SELECT TOP 1 `types` FROM `qx_station`")
    String getStationSensorType();
}
