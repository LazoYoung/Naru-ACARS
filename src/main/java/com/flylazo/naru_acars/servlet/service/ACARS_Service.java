package com.flylazo.naru_acars.servlet.service;

import com.flylazo.naru_acars.domain.acars.VirtualAirline;
import com.flylazo.naru_acars.servlet.socket.SocketConnector;
import com.flylazo.naru_acars.servlet.socket.SocketContext;
import com.flylazo.naru_acars.servlet.socket.SocketListener;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Service;

@Service
public class ACARS_Service {
    private final SocketListener listener;
    private SocketContext context;

    public ACARS_Service() {
        this.listener = new SocketListener();
    }

    public boolean isConnected() {
        return (this.context != null) && this.context.isOpen();
    }

    @Nullable
    public String getServerName() {
        return (this.context != null) ? this.context.getServer().toString() : null;
    }

    public SocketConnector getConnector(VirtualAirline airline) {
        if (isConnected()) {
            throw new IllegalStateException("Connection is already established!");
        }

        if (this.context != null) {
            this.context.terminate();
        }

        return new SocketConnector(airline, this.listener)
                .whenSuccess(this::updateContext);
    }

    public SocketListener getListener() {
        return listener;
    }

    public SocketContext getContext() {
        return this.context;
    }

    private void updateContext(SocketContext context) {
        this.context = context;
    }
}
