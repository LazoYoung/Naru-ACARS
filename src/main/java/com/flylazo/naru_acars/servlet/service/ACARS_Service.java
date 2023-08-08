package com.flylazo.naru_acars.servlet.service;

import com.flylazo.naru_acars.domain.acars.VirtualAirline;
import com.flylazo.naru_acars.servlet.service.socket.SocketConnector;
import com.flylazo.naru_acars.servlet.service.socket.SocketContext;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Service;

@Service
public class ACARS_Service {
    private SocketContext context;
    private String server = null;

    public boolean isConnected() {
        return this.context.isOpen();
    }

    @Nullable
    public String getServer() {
        return this.server;
    }

    public SocketConnector getConnector(VirtualAirline airline) {
        if (this.context != null) {
            throw new IllegalStateException("Connection is already established!");
        }

        return new SocketConnector(airline.getUri())
                .whenSuccess(this::updateContext);
    }

    public SocketContext getContext() {
        return this.context;
    }

    private void updateContext(SocketContext context) {
        this.context = context;
    }
}
