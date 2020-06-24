package com.ldchina.datacenter.sensor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FRTMODBUS_CHANNEL {
    //public static final byte FRT_MODBUS_CHANNEL_TYPE_
    public static Map<Integer, ChannelInfo> type2datalenMap = new HashMap<Integer, ChannelInfo>();
    public static List<ChannelInfo> channelInfoList;
    static{
        // channelInfoList= JsonUtil.getAllJavaObjInDir("./config/protocol/FRT",ChannelInfo.class);
    }

//    public static final Map<Byte,Integer> type2datalenMap = new HashMap<Byte, Integer>(){
//        //.put()
//    };

    public String name;
    public byte mainid;
    public short index;
    public byte type;

}
