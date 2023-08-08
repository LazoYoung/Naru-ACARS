package com.flylazo.naru_acars.domain.acars.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.flylazo.naru_acars.domain.acars.response.deserializer.ErrorResponseDeserializer;

@JsonDeserialize(using = ErrorResponseDeserializer.class)
public class ErrorResponse extends Response {
    private String response;

    public static ErrorResponse deserialize(String json) throws JsonProcessingException {
        return new ObjectMapper().readValue(json, ErrorResponse.class);
    }

    public ErrorResponse(JsonNode node) {
        super(node);
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
