package com.ldchina.datacenter.types;

import com.ldchina.datacenter.sensor.ChannelInfo;

import java.util.Map;

public class Sensor {
    public String name;
    public Map<String, Map<String, ChannelInfo>> channel;
}
