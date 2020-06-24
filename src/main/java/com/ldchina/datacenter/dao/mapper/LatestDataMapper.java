package com.ldchina.datacenter.dao.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.ldchina.datacenter.types.DataInfo;

@Mapper
public interface LatestDataMapper {
    @Select("SELECT * FROM `qx_latest` WHERE `stationid`=#{stationId}")
    List<DataInfo> getLatestDataById(@Param("stationId")String stationId);
    @Select("SELECT * FROM `qx_latest`")
    List<DataInfo> getLatestDataAll();
}
