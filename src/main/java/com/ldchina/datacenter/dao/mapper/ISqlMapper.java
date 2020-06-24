package com.ldchina.datacenter.dao.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;
@Mapper
public interface ISqlMapper {
        @Select("${sql}")
        List<Map<String,Object>> sqlget(@Param("sql") String sql);
        @Insert("${sql}")
        int sqlput(@Param("sql") String sql);        
        
        @Select("select count(*) from information_schema.TABLES where table_name = #{tableName}")
        boolean isTableExit(@Param("tableName")String tableName);
        
        
}
