package com.ldchina.datacenter.sensor;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Map;

public class ChannelInfo {
    public String name;

    @JSONField(serialize=false)
    public int len;

    @JSONField(serialize=false)
    public int type = 3;

    @JSONField(serialize=false)
    public int scale = 1;

    public String unit;

    //LMDS4专有项
    public String key;

    @JSONField(serialize=false)
    public String content;

    @JSONField(serialize=false)
    public Map<String,String> mapcode;

    public String _SensorName;

}
