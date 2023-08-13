package com.flylazo.naru_acars.servlet.socket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.flylazo.naru_acars.NaruACARS;
import com.flylazo.naru_acars.domain.acars.request.Request;
import com.flylazo.naru_acars.domain.acars.response.ErrorResponse;
import com.flylazo.naru_acars.domain.acars.response.Response;
import com.flylazo.naru_acars.domain.acars.response.Status;

import java.net.http.WebSocket;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketMessage<T extends Response> {
    private final Logger logger;
    private final String ident;
    private final SocketListener listener;
    private final WebSocket socket;
    private Consumer<T> responseObs;
    private Consumer<ErrorResponse> errorObs;

    public SocketMessage(SocketContext context) {
        this.logger = NaruACARS.logger;
        this.ident = context.getNextIdent();
        this.listener = context.getListener();
        this.socket = context.getWebSocket();
    }

    @Deprecated
    protected SocketMessage(String ident, SocketContext context) {
        this.logger = NaruACARS.logger;
        this.ident = ident;
        this.listener = context.getListener();
        this.socket = context.getWebSocket();
    }

    public SocketMessage<T> whenSuccess(Consumer<T> callback) {
        this.responseObs = callback;
        return this;
    }

    public SocketMessage<T> whenError(Consumer<ErrorResponse> callback) {
        this.errorObs = callback;
        return this;
    }

    public void send(Request request) throws JsonProcessingException {
        request.setIdent(this.ident);
        this.socket.sendText(request.serialize(), true)
                .orTimeout(5, TimeUnit.SECONDS)
                .exceptionallyAsync(this::handleException)
                .thenAcceptAsync(socket -> attachObservers(request));
    }

    private WebSocket handleException(Throwable t) {
        Status status;
        if (t instanceof TimeoutException) {
            status = Status.TIMEOUT;
        } else {
            status = Status.CLIENT_ERROR;
        }

        ErrorResponse response = new ErrorResponse(this.ident, status, t.getMessage());
        this.errorObs.accept(response);
        this.logger.log(Level.SEVERE, "Failed to send socket message!", t);
        return this.socket;
    }

    @SuppressWarnings("unchecked")
    private void attachObservers(Request request) {
        this.listener.observeMessage(request, (Consumer<Response>) this.responseObs, this.errorObs);
    }
}
