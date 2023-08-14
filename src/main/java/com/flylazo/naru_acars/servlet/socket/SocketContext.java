package com.flylazo.naru_acars.servlet.socket;

import com.flylazo.naru_acars.domain.acars.VirtualAirline;
import com.flylazo.naru_acars.domain.acars.response.Response;

import java.net.http.WebSocket;

/**
 * SocketContext retains messaging context throughout the socket life-time.
 */
public class SocketContext {
    private final VirtualAirline server;
    private final SocketListener listener;
    private final WebSocket socket;
    private int ident = 1;

    public SocketContext(VirtualAirline server, WebSocket socket, SocketListener listener) {
        this.server = server;
        this.socket = socket;
        this.listener = listener;
    }

    public boolean isOpen() {
        return this.socket != null && !this.socket.isInputClosed() && !this.socket.isOutputClosed();
    }

    public SocketListener getListener() {
        return listener;
    }

    public WebSocket getWebSocket() {
        return socket;
    }

    public VirtualAirline getServer() {
        return server;
    }

    public void terminate() {
        this.socket.sendClose(WebSocket.NORMAL_CLOSURE, "closing datalink");
    }

    @Deprecated
    public SocketMessage<? extends Response> buildMessage() {
        String ident = String.valueOf(this.ident++);
        return new SocketMessage<>(ident, this);
    }

    protected String getNextIdent() {
        return String.valueOf(this.ident++);
    }

}
