package com.flylazo.naru_acars.servlet.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.flylazo.naru_acars.NaruACARS;
import com.flylazo.naru_acars.domain.acars.VirtualAirline;
import com.flylazo.naru_acars.domain.acars.request.FetchBulk;
import com.flylazo.naru_acars.domain.acars.request.Request;
import com.flylazo.naru_acars.domain.acars.response.BookingResponse;
import com.flylazo.naru_acars.domain.acars.response.ErrorResponse;
import com.flylazo.naru_acars.servlet.socket.SocketConnector;
import com.flylazo.naru_acars.servlet.socket.SocketContext;
import com.flylazo.naru_acars.servlet.socket.SocketListener;
import com.flylazo.naru_acars.servlet.socket.SocketMessage;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class ACARS_Service {
    private final SocketListener listener;
    private final Logger logger;
    private SocketContext context;

    public ACARS_Service() {
        this.logger = NaruACARS.logger;
        this.listener = new SocketListener();
    }

    public boolean isConnected() {
        return (this.context != null) && this.context.isOpen();
    }

    @Nullable
    public String getServerName() {
        return (this.context != null) ? this.context.getServer().toString() : null;
    }

    public SocketConnector getConnector(VirtualAirline airline) throws IllegalStateException {
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

    /**
     * Fetch booking data from VA server
     * @param callback takes {@link BookingResponse} as a successful result
     * @param errorHandler takes {@link ErrorResponse} to handle any exceptions that may arise
     * @throws IllegalStateException thrown if socket connection is not established.
     */
    public void fetchBooking(Consumer<BookingResponse> callback, Consumer<ErrorResponse> errorHandler) {
        if (!this.isConnected()) {
            throw new IllegalStateException("ACARS is offline.");
        }

        var message = new SocketMessage<BookingResponse>(this.context);
        var request = new Request()
                .withIntent("fetch")
                .withBulk(new FetchBulk("booking"));

        try {
            message.fetchResponse(callback)
                    .whenError(errorHandler)
                    .send(request);
        } catch (JsonProcessingException e) {
            this.logger.log(Level.SEVERE, "Socket error!", e);
        }
    }

    private void updateContext(SocketContext context) {
        this.context = context;
    }
}
