package com.ldchina.datacenter.mina.protocol.protocols.lufft_heartbeat;

import com.ldchina.datacenter.mina.protocol.NetMessage;
import com.ldchina.datacenter.mina.protocol.NetMessageType;
import com.ldchina.datacenter.mina.protocol.protocols.lufft_comoncmd.LufftCommonCmd;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

class Context {
    private IoBuffer ioBuffer = IoBuffer.allocate(4096).setAutoExpand(true);

    public IoBuffer getIoBuffer() {
        return ioBuffer;
    }
}

/**
 *  编码器
 */
public class LufftHeartBeatDecoder extends CumulativeProtocolDecoder {
    private final AttributeKey CONTEXT = new AttributeKey(getClass(), "context");

    @Override
    protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput decoderOutput)
            throws Exception {
        IoBuffer ioBuffer = getContext(session).getIoBuffer();
        while(in.hasRemaining()){
            byte b = in.get();
            ioBuffer.put(b);
            if(ioBuffer.position() < 22) continue;
            if(b!= LufftHeartBeat.EOF){
                continue;
            }
            int len = ioBuffer.position();
            byte[] bytes = new byte[len];
            ioBuffer.position(0);
            ioBuffer.get(bytes, 0, len);
            ioBuffer.get(bytes, 0, len);
            int datalen = bytes[2] << 8| bytes[3];
            if (bytes[0] != LufftHeartBeat.EOF
                    || (datalen+ LufftCommonCmd.FRAME_MIN_LENGHT) != len) {
                ioBuffer.position(len);
                continue;
            }
            LufftHeartBeat lufftHeartBeat = new LufftHeartBeat();
            lufftHeartBeat.len = (short)datalen;
            System.arraycopy(bytes, 4, lufftHeartBeat.id, 0, 11);
            lufftHeartBeat.port = (short)(bytes[19]<<8|bytes[20]);

            System.out.println("正常包." + NetMessageType.Lufft_HeartBeat);
            NetMessage netMessage = new NetMessage(NetMessageType.Lufft_HeartBeat, lufftHeartBeat);

            decoderOutput.write(netMessage);
            ioBuffer.clear();
            return true;
        }
        return false;

    }
    public Context getContext(IoSession session) {
        Context ctx = (Context) session.getAttribute(CONTEXT);
        if (ctx == null) {
            ctx = new Context();
            session.setAttribute(CONTEXT, ctx);
        }
        return ctx;
    }


/**
 * 将byte[]转换为short
 * @param buf byte数组
 * @param asc true
 * @return
 */
        public final static short getShort(byte[] buf, boolean asc) {
            if (buf == null) {
                throw new IllegalArgumentException("byte array is null!");
            }
//            if (buf.length > 2) {
//                throw new IllegalArgumentException("byte array size > 2 !");
//            }
            short r = 0;
            if (asc)
                for (int i = buf.length - 1; i >= 0; i--) {
                    r <<= 8;
                    r |= (buf[i] & 0x00ff);
                }
            else
                for (int i = 0; i < buf.length; i++) {
                    r <<= 8;
                    r |= (buf[i] & 0x00ff);
                }
            return r;
        }

}
