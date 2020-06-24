package com.ldchina.datacenter.mina.protocol.protocols.lufft_heartbeat;




public class LufftHeartBeat {
    public static final byte SOF = 0x7B;
    public static final byte EOF = 0x7B;
    public static final byte type = 0x01;


    public short len;
    public byte[] id = new byte[11];
    public int ip;
    public short port;
}
