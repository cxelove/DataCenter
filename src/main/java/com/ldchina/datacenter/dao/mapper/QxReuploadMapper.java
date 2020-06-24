package com.ldchina.datacenter.dao.mapper;

import java.util.Date;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface QxReuploadMapper {
    @Insert("INSERT INTO qx_reupload (stationId,obTime)values(#{stationid},#{obtime})")
    int save(@Param("stationid") String stationid,@Param("obtime")Date obtime);
    @Delete("DELETE FROM qx_reupload WHERE stationId=#{stationid} AND obTime=#{obtime}")
    int delete(@Param("stationid")String stationid, @Param("obtime")Date obtime);
    @Select("SELECT TOP 1 obTime FROM qx_reupload WHERE stationId=#{stationId} ORDER BY obTime DESC")
    Date getReupload(@Param("stationId")String stationId);
}
