package com.ldchina.datacenter.types;

import org.apache.mina.core.session.IoSession;

import com.ldchina.datacenter.mina.UpdateBin;
import com.ldchina.datacenter.service.WebSocket;

public class Sessions {
    public IoSession ioSession = null;
    public WebSocket webSocket = null;
    public UpdateBin updateBin =null;
}
