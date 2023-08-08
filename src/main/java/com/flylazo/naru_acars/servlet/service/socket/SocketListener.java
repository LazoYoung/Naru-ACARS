package com.flylazo.naru_acars.servlet.service.socket;

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
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketListener implements WebSocket.Listener {

    private final Logger logger;
    private final Map<String, Request> reqMap;
    private final Map<String, Consumer<Response>> obsMap;
    private StringBuilder builder;

    public SocketListener() {
        this.logger = NaruACARS.logger;
        this.reqMap = new HashMap<>();
        this.obsMap = new HashMap<>();
        this.builder = new StringBuilder();
    }

    public void observeMessage(Request request, Consumer<Response> callback) {
        this.reqMap.put(request.getIdent(), request);
        this.obsMap.put(request.getIdent(), callback);
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        WebSocket.Listener.super.onOpen(webSocket);
        this.logger.info("Socket connection opened.");
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

    private void processText() throws IOException {
        String json = this.builder.toString();
        var response = Response.get(json);
        var intent = response.getIntent();
        var ident = response.getIdent();

        if (intent.equals("response")) {
            Request request = this.reqMap.get(ident);
            processResponse(json, request);
        } else {
            logger.warning("Received message with bad intent: " + intent);
        }
    }

    private void processResponse(String json, Request request) throws IOException {
        String intent = request.getIntent();
        var observer = this.obsMap.get(intent);
        Response response = Response.get(json);

        if (response.getStatus() != Status.SUCCESS) {
            response = getErrorResponse(json);
        } else if (intent.equals("fetch")) {
            response = getFetchResponse(json, (FetchBulk) request.getBulk());
        }

        if (observer != null) {
            observer.accept(response);
            this.obsMap.remove(intent);
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
