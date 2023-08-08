package com.flylazo.naru_acars.domain.acars.response.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.flylazo.naru_acars.domain.acars.response.ErrorResponse;

import java.io.IOException;

public class ErrorResponseDeserializer extends StdDeserializer<ErrorResponse> {
    public ErrorResponseDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public ErrorResponse deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        var response = new ErrorResponse(node);
        response.setResponse(node.get("response").asText());
        return response;
    }
}
