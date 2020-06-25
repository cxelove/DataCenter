package com.ldchina.datacenter.service;

import com.ldchina.datacenter.AppConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/websocket")
@Component
public class WebSocket {
    private final static Logger log = LoggerFactory.getLogger(WebSocket.class);
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;

    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
    private static CopyOnWriteArraySet<WebSocket> webSocketSet = new CopyOnWriteArraySet<WebSocket>();

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    private String stationId = null;
   // private Sessions sessionUnion = null;

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocket.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocket.onlineCount--;
    }

    /**
     * 连接建立成功调用的方法
     *
     * @param session 可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        webSocketSet.add(this);     //加入set中
        addOnlineCount();           //在线数加1
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        webSocketSet.remove(this);  //从set中删除
        subOnlineCount();           //在线数减1

        if (stationId != null) {
            AppConfig.stationidTostationStatus
                    .get(stationId).webSocketSession = null;
            if(AppConfig.stationidTostationStatus
                    .get(stationId).updateBin != null){
                AppConfig.stationidTostationStatus
                        .get(stationId).updateBin.cleanUp();
                AppConfig.stationidTostationStatus
                        .get(stationId).updateBin=null;
            }
        }

        log.info("OnClosed");
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     * @param session 可选的参数
     */
    @OnMessage
    public void onMessage(String message, Session session) {
//        log.info("Get Msg From Browser: "+ message);
        if (message.charAt(0) == '*') {
            switch (message.charAt(1)) {
                case '0':
                    stationId = message.substring(2);
                    try {
                        if (AppConfig.stationidTostationStatus.get(stationId).ioSession != null) {
                            session.getBasicRemote().sendText("*success");
                            AppConfig.stationidTostationStatus.get(stationId).webSocketSession = session;
                            //sessionUnion = MinaTcpServerHandler.sessionsMap.get(stationId);
                        } else {
                            session.getBasicRemote().sendText("*fail");
                        }
                    } catch (IOException e) {

                    }
                    break;
                default:
                    break;
            }
        } else {
            if (stationId != null) {
                if (AppConfig.stationidTostationStatus.get(stationId).ioSession != null) {
                AppConfig.stationidTostationStatus.get(stationId).ioSession.write(message);
                    log.info("SendToClient: " + stationId + ": " + message);
                }
            }
        }
    }

    /**
     * 发生错误时调用
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) throws IOException {
        session.close();
    }

//    /**
//     * 这个方法与上面几个方法不一样。没有用注解，是根据自己需要添加的方法。
//     *
//     * @param message
//     * @throws IOException
//     */
//    public void sendMessage(String message) throws IOException {
//        this.session.getBasicRemote().sendText(message);
//    }
}