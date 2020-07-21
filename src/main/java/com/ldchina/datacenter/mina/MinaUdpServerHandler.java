package com.ldchina.datacenter.mina;

import com.ldchina.datacenter.AppConfig;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

public class MinaUdpServerHandler extends IoHandlerAdapter {
    private final static Logger log = LoggerFactory.getLogger(MinaUdpServerHandler.class);
//    private UdpServer server;
//
//    public UdpServerHandler(UdpServer server) {
//        this.server = server;
//    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        cause.printStackTrace();
        session.close(true);
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        IoBuffer ioBuffer = (IoBuffer) message;
        byte[] bytes = new byte[ioBuffer.limit()];
        ioBuffer.get(bytes);
//        TxtUtil.getEncoding(bytes);
        ProcThread.procCachedThreadPool.execute(new ProcThread(session, bytes));
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
       if(ProcThread.ioSessionToStationId.get(session) != null){
           AppConfig.stationidTostationInfo
                   .get(ProcThread.ioSessionToStationId.get(session))
                   .ioSession = null;
           log.info("Udp Session Closed."+session.getRemoteAddress()+" stationId:"+ProcThread.ioSessionToStationId.get(session));
       }else{
           log.info("Udp Session Closed."+session.getRemoteAddress().toString());
       }
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {

        System.out.println("Session created...");

        SocketAddress remoteAddress = session.getRemoteAddress();
        System.out.println(remoteAddress);
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        System.out.println("Session idle...");
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        log.info("Udp Session Opened."+session.getRemoteAddress().toString());
    }
}