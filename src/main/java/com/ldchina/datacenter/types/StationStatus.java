package com.ldchina.datacenter.types;

import com.ldchina.datacenter.dao.entity.StationInfo;
import com.ldchina.datacenter.service.WebSocket;
import org.apache.mina.core.session.IoSession;

public class StationStatus{
	public DataInfo dataInfo;
	public StationInfo stationInfo;
	public IoSession ioSession;
	public WebSocket webSocket;

	public StationStatus( DataInfo dataInfo) {

		this.dataInfo = dataInfo;
	}
	public StationStatus( DataInfo dataInfo, StationInfo stationInfo) {
		this.dataInfo = dataInfo;
		this.stationInfo = stationInfo;
	}
}
