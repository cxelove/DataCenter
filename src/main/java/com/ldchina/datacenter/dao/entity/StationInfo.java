package com.ldchina.datacenter.dao.entity;

import java.util.Date;


public class StationInfo implements Comparable<StationInfo>{
    @Override
    public int compareTo(StationInfo stationInfo){
       return (int)this.getSTATIONID().charAt(0)*100+(int)this.getSTATIONID().charAt(1)*10+(int)this.getSTATIONID().charAt(2)*1
               - (int) stationInfo.getSTATIONID().charAt(0)*100+(int) stationInfo.getSTATIONID().charAt(1)*10+(int) stationInfo.getSTATIONID().charAt(2)*1;
    }
    public String STATIONID;

    public Date OBTIME;

    public Date commtime = new Date(0);
//    public String type;
//public String getType() {
//    return type;
//}
//
//    public char[] getTypes(){return type.toCharArray();}
//
//    public void setType(String type) {
//        this.type = type == null ? null : type.trim();
//    }


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
    
    public String getSTATIONID() {
        return STATIONID;
    }

    public void setSTATIONID(String STATIONID) {
        this.STATIONID = STATIONID == null ? null : STATIONID.trim();
    }

    public Date getOBTIME() {
        return OBTIME;
    }

    public void setOBTIME(Date OBTIME) {
        this.OBTIME = OBTIME;
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