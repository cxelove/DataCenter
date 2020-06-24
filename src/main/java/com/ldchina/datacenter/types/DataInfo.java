package com.ldchina.datacenter.types;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;

public class DataInfo implements Cloneable {
    public Date obTime;
    public String stationId;
    public String ps;
    
    public Map<String,String> val = new HashMap<String,String>();
    
    @SuppressWarnings("unchecked")
	public void setData(String jsonData) {
		this.val = (Map<String, String>)JSON.parse(jsonData);  
	}
    public DataInfo(){

    }
	public DataInfo(String stationid){
        this.stationId = stationid;
        this.obTime = new Date(0);
    }

    @Override
    public DataInfo clone() {
    	DataInfo o = null;
        try {
            o = (DataInfo) super.clone();
        } catch (CloneNotSupportedException e) {
        }
        return o;
    }

}
