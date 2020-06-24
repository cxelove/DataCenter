package com.ldchina.datacenter.mina.protocol;

public class NetMessage {
    public NetMessageType type;
    public Object message;
    public NetMessage(NetMessageType type, Object message){
        this.type = type;
        this.message = message;
    }
}
