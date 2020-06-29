package com.ldchina.datacenter.types;

import com.ldchina.datacenter.dao.entity.StationInfo;
import com.ldchina.datacenter.mina.UpdateBin;
import com.ldchina.datacenter.service.WebSocket;
import org.apache.mina.core.session.IoSession;

import javax.websocket.Session;
import java.util.Date;

public class StationStatus{
	public DataInfo dataInfo;
	public StationInfo stationInfo;
	public IoSession ioSession;
	public Session webSocketSession;
	public UpdateBin updateBin;

	public Date _Runtime_LastReuploadTime = new Date();
	public int _Runtime_ReuploadTryTimes = 0;


	public StationStatus( DataInfo dataInfo) {
		this.dataInfo = dataInfo;
	}
	public StationStatus( DataInfo dataInfo, StationInfo stationInfo) {
		this.dataInfo = dataInfo;
		this.stationInfo = stationInfo;
	}
}
