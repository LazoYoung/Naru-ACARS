package com.flylazo.naru_acars.servlet.service.socket;

import java.net.http.WebSocket;
import java.util.ArrayList;
import java.util.List;

/**
 * SocketContext retains messaging context throughout the socket life-time.
 * It offers convenience function such as {@link SocketContext#buildMessage()}
 */
public class SocketContext {
    private final List<Runnable> observerList;
    private final SocketListener listener;
    private final WebSocket socket;
    private int ident = 1;

    public SocketContext(WebSocket socket, SocketListener listener) {
        this.observerList = new ArrayList<>();
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

    public void terminate() {
        this.socket.abort();
    }

    public SocketMessage buildMessage() {
        String ident = String.valueOf(this.ident++);
        return new SocketMessage(ident, this);
    }

    public void observeUpdate(Runnable run) {
        this.observerList.add(run);
    }

    private void notifyUpdate() {
        this.observerList.forEach(Runnable::run);
    }
}
