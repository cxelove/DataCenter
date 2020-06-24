package com.ldchina.datacenter.dao.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.ldchina.datacenter.dao.entity.WebConfig;

import java.util.List;

@Mapper
public interface WebConfigMapper {
    @Select("SELECT * FROM `web_config` ORDER BY `id` ASC")
    List<WebConfig> selectAll();
    //@Update("UPDATE `web_config` SET mapDisplay = #{mapDisplay},listDisplay = #{listDisplay} WHERE id=#{id}")
   // int updateOne(WebConfig webConfig);
}
