package com.ldchina.datacenter.types;

import com.ldchina.datacenter.dao.entity.StationState;
import com.ldchina.datacenter.dao.entity.WebConfig;
import com.ldchina.datacenter.mina.UpdateBin;
import org.apache.mina.core.session.IoSession;

import javax.websocket.Session;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class StationInfo {
	public DataInfo dataInfo;
	public StationState stationState;
	public IoSession ioSession;
	public Session webSocketSession;
	public UpdateBin updateBin;
	/**
	 * 站点配置的要素
	 */
	public Map<String, WebConfig>  keyToWebconfig = new HashMap<>();

	public Date _Runtime_LastReuploadTime = new Date();
	public int _Runtime_ReuploadTryTimes = 0;


	public StationInfo(DataInfo dataInfo) {
		this.dataInfo = dataInfo;
	}
	public StationInfo(DataInfo dataInfo, StationState stationState) {
		this.dataInfo = dataInfo;
		this.stationState = stationState;
	}
}
