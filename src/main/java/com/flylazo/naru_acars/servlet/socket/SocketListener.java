package com.flylazo.naru_acars.servlet.socket;

import com.flylazo.naru_acars.Helper;
import com.flylazo.naru_acars.NaruACARS;
import com.flylazo.naru_acars.domain.acars.request.FetchBulk;
import com.flylazo.naru_acars.domain.acars.request.Request;
import com.flylazo.naru_acars.domain.acars.response.BookingResponse;
import com.flylazo.naru_acars.domain.acars.response.ErrorResponse;
import com.flylazo.naru_acars.domain.acars.response.Response;
import com.flylazo.naru_acars.domain.acars.response.Status;

import java.io.IOException;
import java.net.http.WebSocket;
import java.util.HashMap;
import java.util.LinkedList;
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
    private final Map<String, Consumer<ErrorResponse>> errorObs;
    private final List<Runnable> openObs;
    private final List<Runnable> estObs;
    private final List<Runnable> closeObs;
    private final List<Consumer<Throwable>> socketErrorObs;
    private StringBuilder builder;

    public SocketListener() {
        this.logger = NaruACARS.logger;
        this.reqMap = new HashMap<>();
        this.reqObs = new HashMap<>();
        this.errorObs = new HashMap<>();
        this.openObs = new LinkedList<>();
        this.estObs = new LinkedList<>();
        this.closeObs = new LinkedList<>();
        this.socketErrorObs = new LinkedList<>();
        this.builder = new StringBuilder();
    }

    public void observeMessage(Request request, Consumer<Response> reqObs, Consumer<ErrorResponse> errorObs) {
        this.reqMap.put(request.getIdent(), request);
        this.reqObs.put(request.getIdent(), reqObs);
        this.errorObs.put(request.getIdent(), errorObs);
    }

    public void observeOpen(Runnable callback) {
        this.openObs.add(callback);
    }

    public void observeEstablish(Runnable callback) {
        this.estObs.add(callback);
    }

    public void observeClose(Runnable callback) {
        this.closeObs.add(callback);
    }

    public void observeSocketError(Consumer<Throwable> callback) {
        this.socketErrorObs.add(callback);
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
    public void onError(WebSocket socket, Throwable error) {
        Throwable t = Helper.getRootCause(error);
        notifySocketError(t);
        socket.abort();
        notifyClose();
        this.logger.log(Level.SEVERE, "Socket error received!", t);
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
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to process socket message!", e);
        } finally {
            this.builder = new StringBuilder();
            socket.request(1);
        }
        return null;
    }

    public void notifyEstablish() {
        this.estObs.forEach(Runnable::run);
    }

    private void notifySocketError(Throwable t) {
        this.socketErrorObs.forEach(c -> c.accept(t));
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

            if (request == null) {
                throw new RuntimeException("Request observer is lost.");
            }
            if (response.getStatus() == Status.SUCCESS) {
                processResponse(json, request);
            } else {
                processErrorResponse(json, ident);
            }
            this.reqMap.remove(ident);
            this.reqObs.remove(ident);
            this.errorObs.remove(ident);
        }
        logger.info(String.format("Received message: %s", json));
    }

    private void processResponse(String json, Request request) throws IOException {
        String intent = request.getIntent();
        String ident = request.getIdent();
        Response response = Response.get(json);

        if (intent.equals("fetch")) {
            response = processFetchResponse(json, (FetchBulk) request.getBulk());
        }

        notifyRequester(response, ident);
    }

    private Response processFetchResponse(String json, FetchBulk bulk) throws IOException {
        String type = bulk.type;

        if (type.equals("booking")) {
            return BookingResponse.deserialize(json);
        } else {
            throw new IOException("Unexpected fetch type: " + type);
        }
    }

    private void processErrorResponse(String json, String ident) throws IOException {
        ErrorResponse response = ErrorResponse.deserialize(json);
        var observer = this.errorObs.get(ident);
        if (observer != null) {
            observer.accept(response);
        }
    }

    private void notifyRequester(Response response, String ident) {
        var observer = this.reqObs.get(ident);
        if (observer != null) {
            observer.accept(response);
        }
    }

}
