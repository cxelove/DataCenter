package com.ldchina.datacenter.mina.protocol.protocols.lufft_heartbeat;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class LufftHeartBeatCodecFactory implements ProtocolCodecFactory {

    private final ProtocolEncoder encoder;
    private final ProtocolDecoder decoder;

//    public LufftUdpCodecFactory() {
//       // this(Charset.forName("UTF-8"));
//    }

    // 构造方法注入编解码器
    public LufftHeartBeatCodecFactory() {
        this.encoder = new LufftHeartBeatEncoder();
        this.decoder=new LufftHeartBeatDecoder();
    }
    @Override
    public ProtocolDecoder getDecoder(IoSession session) throws Exception {
        return decoder;
    }

    @Override
    public ProtocolEncoder getEncoder(IoSession session) throws Exception {
        return encoder;
    }
}