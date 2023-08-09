package com.flylazo.naru_acars.servlet.socket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.flylazo.naru_acars.Helper;
import com.flylazo.naru_acars.NaruACARS;
import com.flylazo.naru_acars.domain.acars.VirtualAirline;
import com.flylazo.naru_acars.domain.acars.request.AuthBulk;
import com.flylazo.naru_acars.domain.acars.request.Request;
import com.flylazo.naru_acars.domain.acars.response.Response;
import com.flylazo.naru_acars.domain.acars.response.Status;

import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.channels.ClosedChannelException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketConnector {
    private final Logger logger;
    private final SocketListener listener;
    private final List<Consumer<SocketContext>> successObs;
    private final List<Consumer<SocketError>> errorObs;
    private final VirtualAirline server;
    private String apiKey;

    public SocketConnector(VirtualAirline server, SocketListener listener) {
        this.logger = NaruACARS.logger;
        this.listener = listener;
        this.successObs = new ArrayList<>();
        this.errorObs = new ArrayList<>();
        this.server = server;
    }

    public SocketConnector withAPIKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public SocketConnector whenSuccess(Consumer<SocketContext> callback) {
        this.successObs.add(callback);
        return this;
    }

    public SocketConnector whenError(Consumer<SocketError> callback) {
        this.errorObs.add(callback);
        return this;
    }

    public void connect() {
        HttpClient.newHttpClient()
                .newWebSocketBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .buildAsync(this.server.getUri(), listener)
                .exceptionallyAsync(this::handleException)
                .thenApplyAsync(socket -> new SocketContext(this.server, socket, listener))
                .thenAcceptAsync(this::authenticate);
    }

    private void authenticate(SocketContext context) {
        Request request = new Request();
        request.setIntent("auth");
        request.setBulk(new AuthBulk(this.apiKey));

        try {
            context.buildMessage()
                    .withObserver(r -> handleAuthResponse(r, context))
                    .send(request);
        } catch (JsonProcessingException e) {
            context.terminate();
            notifyError(SocketError.FATAL_ERROR);
            this.logger.log(Level.SEVERE, "Exception while handling socket communication", e);
        }
    }

    private WebSocket handleException(Throwable t) {
        var cause = Helper.getRootCause(t);

        if (cause instanceof TimeoutException) {
            notifyError(SocketError.OFFLINE);
        } else if (cause instanceof ClosedChannelException) {
            notifyError(SocketError.OFFLINE);
        } else {
            notifyError(SocketError.FATAL_ERROR);
            this.logger.log(Level.SEVERE, "Exception while handling socket communication", t);
        }
        return null;
    }

    private void handleAuthResponse(Response response, SocketContext context) {
        var status = response.getStatus();

        if (status == Status.SUCCESS) {
            this.successObs.forEach(obs -> obs.accept(context));
        } else {
            SocketError error = switch (status) {
                case TIMEOUT -> SocketError.OFFLINE;
                case FORBIDDEN -> SocketError.API_KEY_IN_USE;
                case NOT_FOUND -> SocketError.API_KEY_INVALID;
                default -> SocketError.FATAL_ERROR;
            };
            this.errorObs.forEach(obs -> obs.accept(error));
        }
    }

    private void notifyError(SocketError error) {
        this.errorObs.forEach(obs -> obs.accept(error));
    }
}
