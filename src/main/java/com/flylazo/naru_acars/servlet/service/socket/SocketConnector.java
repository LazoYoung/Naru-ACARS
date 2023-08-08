package com.flylazo.naru_acars.servlet.service.socket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.flylazo.naru_acars.NaruACARS;
import com.flylazo.naru_acars.domain.acars.request.AuthBulk;
import com.flylazo.naru_acars.domain.acars.request.Request;
import com.flylazo.naru_acars.domain.acars.response.Response;
import com.flylazo.naru_acars.domain.acars.response.Status;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class SocketConnector {
    private final Logger logger = NaruACARS.logger;
    private final List<Consumer<SocketContext>> successObs = new ArrayList<>();
    private final List<Consumer<SocketError>> errorObs = new ArrayList<>();
    private final URI uri;
    private String apiKey;

    public SocketConnector(URI uri) {
        this.uri = uri;
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
        final var listener = new SocketListener();
        HttpClient.newHttpClient()
                .newWebSocketBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .buildAsync(this.uri, listener)
                .exceptionallyAsync(this::handleException)
                .thenApplyAsync(socket -> new SocketContext(socket, listener))
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
            this.logger.severe("Exception while handling socket communication");
            this.logger.severe(e.getMessage());
        }
    }

    private WebSocket handleException(Throwable t) {
        if (t instanceof TimeoutException) {
            notifyError(SocketError.TIMEOUT);
        } else {
            notifyError(SocketError.FATAL_ERROR);
            this.logger.severe("Exception while handling socket communication");
            this.logger.severe(t.getMessage());
        }
        return null;
    }

    private void handleAuthResponse(Response response, SocketContext context) {
        var status = response.getStatus();

        if (status == Status.SUCCESS) {
            this.successObs.forEach(obs -> obs.accept(context));
        } else {
            SocketError error = switch (status) {
                case TIMEOUT -> SocketError.TIMEOUT;
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
