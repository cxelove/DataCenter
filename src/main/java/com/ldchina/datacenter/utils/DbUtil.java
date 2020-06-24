package com.ldchina.datacenter.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.ldchina.datacenter.dao.mapper.ISqlMapper;
import com.ldchina.datacenter.dao.mapper.LatestDataMapper;
import com.ldchina.datacenter.dao.mapper.QxReuploadMapper;
import com.ldchina.datacenter.dao.mapper.QxStationMapper;
import com.ldchina.datacenter.dao.mapper.WebConfigMapper;

@Component
public class DbUtil {
	public static DbUtil dbMapperUtil;
	
	@Autowired
	public ISqlMapper iSqlMapper;
	@Autowired
	public QxReuploadMapper qxReuploadMapper;
	@Autowired
	public QxStationMapper qxStationMapper;
    @Autowired
    public WebConfigMapper webConfigMapper;
    @Autowired
    public LatestDataMapper latestDataMapper;
	
//	@PostConstruct
    @Bean(name="DbUtil")
	public void init(){
		dbMapperUtil = this;
	}
}
