package com.flylazo.naru_acars.servlet.socket;

import com.flylazo.naru_acars.NaruACARS;
import com.flylazo.naru_acars.domain.acars.request.FetchBulk;
import com.flylazo.naru_acars.domain.acars.request.Request;
import com.flylazo.naru_acars.domain.acars.response.BookingResponse;
import com.flylazo.naru_acars.domain.acars.response.ErrorResponse;
import com.flylazo.naru_acars.domain.acars.response.Response;
import com.flylazo.naru_acars.domain.acars.response.Status;

import java.io.IOException;
import java.net.http.WebSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketListener implements WebSocket.Listener {

    private final Logger logger;
    private final Map<String, Request> reqMap;
    private final Map<String, Consumer<Response>> reqObs;
    private final List<Runnable> openObs;
    private final List<Runnable> closeObs;
    private final List<Consumer<Throwable>> errorObs;
    private StringBuilder builder;

    public SocketListener() {
        this.logger = NaruACARS.logger;
        this.reqMap = new HashMap<>();
        this.reqObs = new HashMap<>();
        this.openObs = new ArrayList<>();
        this.closeObs = new ArrayList<>();
        this.errorObs = new ArrayList<>();
        this.builder = new StringBuilder();
    }

    public void observeMessage(Request request, Consumer<Response> callback) {
        this.reqMap.put(request.getIdent(), request);
        this.reqObs.put(request.getIdent(), callback);
    }

    public void observeOpen(Runnable callback) {
        this.openObs.add(callback);
    }

    public void observeClose(Runnable callback) {
        this.closeObs.add(callback);
    }

    public void observeError(Consumer<Throwable> callback) {
        this.errorObs.add(callback);
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        notifyOpen();
        this.logger.info("Socket opened.");
        webSocket.request(1);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket socket, int statusCode, String reason) {
        notifyClose();
        this.logger.info("Socket closed with status " + statusCode);
        return null;
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        notifyError(error);
        this.logger.log(Level.SEVERE, "Socket error received!", error);
    }

    @Override
    public CompletionStage<?> onText(WebSocket socket, CharSequence data, boolean last) {
        this.builder.append(data);

        if (!last) {
            socket.request(1);
            return null;
        }

        try {
            processText();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to process socket message!", e);
        } finally {
            this.builder = new StringBuilder();
            socket.request(1);
        }
        return null;
    }

    private void notifyError(Throwable t) {
        this.errorObs.forEach(c -> c.accept(t));
    }

    private void notifyOpen() {
        this.openObs.forEach(Runnable::run);
    }

    private void notifyClose() {
        this.closeObs.forEach(Runnable::run);
    }

    private void processText() throws IOException {
        String json = this.builder.toString();
        var response = Response.get(json);
        var intent = response.getIntent();
        var ident = response.getIdent();

        if (intent.equals("response")) {
            Request request = this.reqMap.get(ident);
            if (request != null) {
                processResponse(json, request);
            }
        }

        logger.info(String.format("Received message: %s", json));
    }

    private void processResponse(String json, Request request) throws IOException {
        String intent = request.getIntent();
        String ident = request.getIdent();
        var observer = this.reqObs.get(ident);
        Response response = Response.get(json);

        if (response.getStatus() != Status.SUCCESS) {
            response = getErrorResponse(json);
        } else if (intent.equals("fetch")) {
            response = getFetchResponse(json, (FetchBulk) request.getBulk());
        }

        if (observer != null) {
            observer.accept(response);
            this.reqObs.remove(intent);
        }
    }

    private ErrorResponse getErrorResponse(String json) throws IOException {
        return ErrorResponse.deserialize(json);
    }

    private BookingResponse getFetchResponse(String json, FetchBulk bulk) throws IOException {
        String type = bulk.type;

        if (type.equals("booking")) {
            return BookingResponse.deserialize(json);
        } else {
            throw new IOException("Unexpected fetch type: " + type);
        }
    }

}
