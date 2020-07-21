package com.ldchina.datacenter.dao.entity;

import java.util.Date;


public class StationState implements Comparable<StationState>{
    @Override
    public int compareTo(StationState stationState){
       return (int)this.getStationid().charAt(0)*100+(int)this.getStationid().charAt(1)*10+(int)this.getStationid().charAt(2)*1
               - (int) stationState.getStationid().charAt(0)*100+(int) stationState.getStationid().charAt(1)*10+(int) stationState.getStationid().charAt(2)*1;
    }
    public String stationid;

    public Date obtime;

    public Date commtime = new Date(0);

    public String alias;

    public Double lng;

    public Double lat;


    public Boolean noRealTime;

    public String protocol;

    public String getMeasure() {
        return measure;
    }

    public void setMeasure(String measure) {
        this.measure = measure;
    }

    public String measure;


    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    public Boolean getNoRealTime() {
    	return noRealTime;
    }

    public void setNoRealTime(Boolean noRealTime) {
    	this.noRealTime = noRealTime;
    }
    
    public String getStationid() {
        return stationid;
    }

    public void setStationid(String stationid) {
        this.stationid = stationid == null ? null : stationid.trim();
    }

    public Date getObtime() {
        return obtime;
    }

    public void setObtime(Date obtime) {
        this.obtime = obtime;
    }



    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias == null ? null : alias.trim();
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }
}