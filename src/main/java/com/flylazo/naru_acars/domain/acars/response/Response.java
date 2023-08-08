package com.flylazo.naru_acars.domain.acars.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class Response {
    public String intent;
    public String ident;
    public Status status;
    public String message;

    public Response(JsonNode node) {
        JsonNode intent = node.get("intent");
        JsonNode ident = node.get("ident");
        JsonNode status = node.get("status");
        JsonNode message = node.get("message");
        this.intent = intent.asText();
        this.ident = ident.asText();
        this.status = Status.byCode(status.asInt());
        this.message = message.asText();
    }

    public Response(String ident, Status status, String message) {
        this.intent = "response";
        this.ident = ident;
        this.status = status;
        this.message = message;
    }

    public static Response get(String json) throws IOException {
        var mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(json);
        return new Response(node);
    }

    public String getIntent() {
        return intent;
    }

    public String getIdent() {
        return ident;
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

}
