package com.ldchina.datacenter.types;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ldchina.datacenter.AppConfig;
import com.ldchina.datacenter.dao.entity.WebConfig;
import com.ldchina.datacenter.sensor.ChannelInfo;

public class Layui extends HashMap<String, Object> {

    //public Layui()

    public int code;
    public String msg;
    public int count;
    public List<Map<String, String>> data;

    //public static String cols = null;
    private static Map<String, String> stationidToListcols = new HashMap<>();


    public static Layui data(String msg, Integer count, List<?> data) {
        Layui r = new Layui();
        r.put("code", 0);
        r.put("msg", msg);
        r.put("count", count);
        r.put("data", data);
        return r;
    }

    public static String getListColsByStationid(String stationid) {
        return stationidToListcols.get(stationid);
    }

    public static void initListCols() {

        String tmpcols = "[[" +
                "{field: 'stationid', title: '站点', width: 64}" +
                ",{field: 'obtime', width: 150, title: '时间',templet: function(d){if( d['obtime']== 0) return '---'; return (new Date(d['obtime'])).Format('yyyy-MM-dd hh:mm');  }}" +
                ",";

        for(Map.Entry<String,Map<String, WebConfig>> stationidToWebConfig:AppConfig.keyToWebconfigByStationid.entrySet()){
            if(stationidToListcols.get(stationidToWebConfig.getKey()) == null){

                    String colString = tmpcols;
                    for (Map.Entry<String, WebConfig> webConfigEntry : stationidToWebConfig.getValue().entrySet()) {
                        String mainKey = webConfigEntry.getKey().split("_")[1];
                        String subKey = webConfigEntry.getKey().split("_")[2];
                        try{
                            ChannelInfo channelInfo = AppConfig.keyMainSubToChannelInfoByProtocol
                                    .get(AppConfig.stationidTostationStatus.get(stationidToWebConfig.getKey()).stationInfo.protocol)
                                    .get(mainKey)
                                    .get(subKey);
                            if (channelInfo.unit == null || channelInfo.unit.equals("")) {
                                colString += "{ field:'" + channelInfo.key + "',title:'" + channelInfo.name + "'},";
                            } else {
                                colString += "{ field:'" + channelInfo.key + "',title:'" + channelInfo.name + "(" + channelInfo.unit + ")'},";
                            }
                        }catch (NullPointerException ex){
                            System.out.println("初始化列表出错mainKey:"+mainKey+"subKey:"+subKey);
                            ex.printStackTrace();
                            return;
                        }
                    }
                    colString += "{field: 'ps', title: '电压(V)'}]]";
                    stationidToListcols.put(stationidToWebConfig.getKey(), colString);

            }
        }

    }
}
