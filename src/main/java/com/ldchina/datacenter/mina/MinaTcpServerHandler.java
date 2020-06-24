package com.ldchina.datacenter.mina;


import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ldchina.datacenter.types.Sessions;

//@Service
public class MinaTcpServerHandler extends IoHandlerAdapter {

    private final static Logger log = LoggerFactory.getLogger(MinaTcpServerHandler.class);
    
    public static Map<String, Sessions> sessionsMap = new HashMap<>();
  //  public static Map<IoSession, ReceiveThread> receiveThreadMap = new HashMap<>();

    // 由底层决定是否创建一个session
    @Override
    public void sessionCreated(IoSession session) throws Exception {
        super.sessionCreated(session);
    }

    // 创建了session 后会回调sessionOpened
    @Override
    public void sessionOpened(IoSession session) throws Exception {
//    	 ReceiveThread receiveThread = new ReceiveThread();
//         receiveThread.ioSession = session;
//         receiveThread.start();
//         receiveThreadMap.put(session, receiveThread);
       
        super.sessionOpened(session);
    }

    /**
     * 收到客户端发送消息以后会回调这个函数
     */
    @Override
    public void messageReceived(IoSession session, Object message)
            throws Exception {

        ProcThread.procCachedThreadPool.execute(new ProcThread(session, message));

//    	 String msg = message.toString();
//         log.info("Mina Rec: " + msg);
//         char fistChar = 0;
//         String stationId=null;
//         try {
//        	 fistChar = msg.charAt(0);
//        	 //消息是英文
//        	 if(fistChar =='#') {
//        		  stationId= msg.substring(1, 4);
//                 if (stationId.matches("^[a-zA-Z0-9]+$"))
//                 {
//                     receiveThreadMap.get(session).stationId = stationId;
//                     Sessions _sessions = sessionsMap.get(stationId);
//                     if (_sessions == null) {
//                         Sessions sessionUnion = new Sessions();
//                         sessionUnion.ioSession = session;
//                         receiveThreadMap.get(session).setName(stationId);
//
//                         sessionUnion.receiveThread = receiveThreadMap.get(session);
//                         sessionsMap.put(stationId, sessionUnion);
//                     } else {
//                         _sessions.ioSession = session;
//                     }
//                 }
//        	 }
//         }catch (StringIndexOutOfBoundsException e) {
//			// TODO: handle exception
//        	 //出错则消息首字符是中文
//
//		}
//         if(0==receiveThreadMap.get(session).linkedBlockingQueue.remainingCapacity()) {
//        	 log.error("队列容量满,关闭连接["+stationId+"].");
//        	 clearThreadResource(session);
//         }else {
//        	 receiveThreadMap.get(session).linkedBlockingQueue.put(msg);
//         }
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        super.messageSent(session, message);
    }

    /**
     * session 关闭的时候被调用
     */
    @Override
    public void sessionClosed(IoSession session) throws Exception {
    	clearThreadResource(session);
        super.sessionClosed(session);
    }

    /**
     * session 空闲的时候被调用
     */
    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        super.sessionIdle(session, status);
        session.close(true);
    }

    /**
     * 异常捕捉的时候被调用
     */
    @Override
    public void exceptionCaught(IoSession session, Throwable cause)
            throws Exception {
        //super.exceptionCaught(session, cause);
        session.close(true);
    }
    private void clearThreadResource(IoSession ioSession) {
//        try {
//            log.info("clear resource");
//            String stationId = receiveThreadMap.get(ioSession).stationId;
//            receiveThreadMap.get(ioSession).exit = true;
//            if (null == sessionsMap.get(stationId).webSocket) {
//                sessionsMap.remove(stationId);
//            }
//            receiveThreadMap.remove(ioSession);
//        } catch (Exception e) {
//            //  e.printStackTrace();
//        }
    }

}
