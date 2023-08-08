package com.flylazo.naru_acars.domain.acars.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.flylazo.naru_acars.domain.FlightPlan;
import com.flylazo.naru_acars.domain.acars.response.deserializer.BookingResponseDeserializer;

import java.time.Instant;

@JsonDeserialize(using = BookingResponseDeserializer.class)
public class BookingResponse extends Response {
    private FlightPlan flightPlan;
    private Instant departTime;

    public static BookingResponse deserialize(String json) throws JsonProcessingException {
        return new ObjectMapper().readValue(json, BookingResponse.class);
    }

    public BookingResponse(JsonNode node) {
        super(node);
    }

    public FlightPlan getFlightPlan() {
        return flightPlan;
    }

    public void setFlightPlan(FlightPlan flightPlan) {
        this.flightPlan = flightPlan;
    }

    public Instant getDepartTime() {
        return departTime;
    }

    public void setDepartTime(Instant departTime) {
        this.departTime = departTime;
    }
}
