package com.ldchina.datacenter.sensor;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Map;

public class ChannelInfo {
    /*通道测量要素名称，比如温度*/
    public String name;
    /*缩放倍数*/
    @JSONField(serialize = false)
    public int scale = 1;
    /*单位*/
    public String unit;
    /*要素关键字，也是数据库列名*/
    public String key;

    /*通道测量要素分类，比如温度类、能见度类或者报文时间等特殊标记*/
    public String channelType;

    /*FRT-MODBUS协议字段长度*/
    @JSONField(serialize = false)
    public int len;
    /*FRT-MODBUS协议字段类型*/
    @JSONField(serialize = false)
    public int type = 3;

    /*转换码。一般用于路况等*/
    @JSONField(serialize = false)
    public Map<String, String> mapcode;

    /*传感器名称*/
    public String _SensorName;


}
