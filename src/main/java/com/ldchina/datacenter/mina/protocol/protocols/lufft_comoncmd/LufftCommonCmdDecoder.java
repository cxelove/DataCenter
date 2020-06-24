package com.ldchina.datacenter.mina.protocol.protocols.lufft_comoncmd;

import com.ldchina.datacenter.mina.protocol.NetMessageType;
import com.ldchina.datacenter.mina.protocol.NetMessage;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.*;

class Context {
    private IoBuffer ioBuffer = IoBuffer.allocate(1024).setAutoExpand(true);

    public IoBuffer getIoBuffer() {
        return ioBuffer;
    }
}
/**
 *  编码器
 */
public class LufftCommonCmdDecoder extends CumulativeProtocolDecoder {
    private final AttributeKey CONTEXT = new AttributeKey(getClass(), "context");

    @Override
    protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput decoderOutput)
            throws Exception {
        // CharsetDecoder de = Charset.forName("utf-8").newDecoder();

        IoBuffer ioBuffer = getContext(session).getIoBuffer();

        while(in.hasRemaining()) {
            byte b = in.get();
            ioBuffer.put(b);
        }
        int position = ioBuffer.position();
        if(ioBuffer.position() > LufftCommonCmd.FRAME_MIN_LENGHT) {
            ioBuffer.position(0);
            while (ioBuffer.hasRemaining()) {
                byte soh = ioBuffer.get();
                if(soh != LufftCommonCmd._soh){
                    continue;
                }
                int secposition = ioBuffer.position();
                byte[] bytes = new byte[LufftCommonCmd.FRAME_HEAD_LENGHT];
                if(ioBuffer.remaining() < LufftCommonCmd.FRAME_HEAD_LENGHT) break;
                ioBuffer.position(secposition -1);
                ioBuffer.get(bytes, 0, LufftCommonCmd.FRAME_HEAD_LENGHT);
                ioBuffer.position(secposition);
                if(bytes[8] != LufftCommonCmd._stx)
                {
                    continue;
                }
                int datalen = bytes[6] << 8 | bytes[7];
                if(datalen > 512 || datalen > ioBuffer.capacity()) continue;
                int framelen = LufftCommonCmd.FRAME_MIN_LENGHT + datalen;
                byte[] frame = new byte[framelen];
                ioBuffer.position(secposition-1);
                ioBuffer.get(frame, 0, framelen);
                if(frame[framelen-1] != LufftCommonCmd._eot
                ||frame[framelen-4] != LufftCommonCmd._stx){
                    ioBuffer.position(secposition);
                    continue;
                }

                LufftCommonCmd lufftMsg = new LufftCommonCmd(frame, framelen);
                if (lufftMsg.checkAndParse()) {
                    System.out.println("正常包." + NetMessageType.Lufft_CommonCmd);
                    NetMessage netMessage = new NetMessage(NetMessageType.Lufft_CommonCmd, lufftMsg);

                    decoderOutput.write(netMessage);
                    ioBuffer.clear();
                    return true;
                } else {
                    ioBuffer.clear();
                    return false;
                }
            }
        }
//        if(ioBuffer.position() >= 22){
//            while (ioBuffer.hasRemaining()){
//                int datalen = bytes[2] << 8| bytes[3];
//                if (bytes[0] != LufftHeartBeat.EOF
//                        || (datalen+ LufftCommonCmd.FRAME_MIN_LENGHT) != len) {
//                    ioBuffer.position(len);
//                    continue;
//                }
//                LufftHeartBeat lufftHeartBeat = new LufftHeartBeat();
//                lufftHeartBeat.len = (short)datalen;
//                System.arraycopy(bytes, 4, lufftHeartBeat.id, 0, 11);
//                lufftHeartBeat.port = (short)(bytes[19]<<8|bytes[20]);
//
//                System.out.println("正常包." + NetMessageType.Lufft_HeartBeat);
//                NetMessage netMessage = new NetMessage(NetMessageType.Lufft_HeartBeat, lufftHeartBeat);
//
//                decoderOutput.write(netMessage);
//                ioBuffer.clear();
//                return true;
 //           }
    //    }

  //      ioBuffer.position(position);
        return false;






        /**
         * 这里，如果长度不够20字节，就认为这条消息还没有累积完成
         */
//        if (buffer.remaining() < 13) {
//            /**
//             *      停止调用decode，但如果还有数据没有读取完，将含有剩余数据的IoBuffer保存到IoSession中，
//             *  当有新数据包来的时候再和新的合并后再调用decoder解码
//             *      注意：没有消费任何数据时不能返回true，否则会抛出异常
//             */
//            return false;
//
//        } else {
//            buffer.mark();
//            byte[] headBytes = new byte[LufftMsg_udp.FRAME_HEAD_LENGHT];
//            buffer.get(headBytes, 0, LufftMsg_udp.FRAME_HEAD_LENGHT);
//
//            if(headBytes[0] != LufftMsg_udp._soh
//            ||headBytes[8] != LufftMsg_udp._stx){
//                buffer.reset();
//                return false;
//            }
//
//            int datalenght = headBytes[6] << 8 | headBytes[7];
//
//            byte[] framebuffer = new byte[LufftMsg_udp.FRAME_MIN_LENGHT+ datalenght];
//            System.arraycopy(headBytes,0,framebuffer,0,headBytes.length);
//            buffer.get(framebuffer, headBytes.length, datalenght+LufftMsg_udp.FRAME_END_LENGHT);
//
//            if(framebuffer[framebuffer.length-1] != LufftMsg_udp._eot
//                    ||framebuffer[framebuffer.length-4] != LufftMsg_udp._etx){
//                buffer.reset();
//                return false;
//            }
//
//            LufftMsg_udp lufftMsg = new LufftMsg_udp(framebuffer);
//            if(lufftMsg.check()){
//
//            }else{
//                buffer.reset();
//                return false;
//            }

//            int length = buffer.getInt();
//
//            LufftMsg_udp msg = new LufftMsg_udp();
//            byte[] b = new byte[buffer.];
//            msg.setBuffer(buffer, buffer.getLong());
//
//
//            msg.setSender(buffer.getLong());
//            msg.setReceiver(buffer.getLong());
//
//            //  注意：20 = 消息长度的字节 + 发送人和接收人的字节
//            msg.setContent(buffer.getString(length - 20, de));
//
//            decoderOutput.write(msg);

            /**
             *  CumulativeProtocolDecoder会再次调用decoder，并把剩余的数据发下来继续解码
             */
 //           return true;
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
