package com.flylazo.naru_acars.domain.acars.response.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.flylazo.naru_acars.NaruACARS;
import com.flylazo.naru_acars.domain.FlightPlan;
import com.flylazo.naru_acars.domain.acars.response.BookingResponse;
import com.flylazo.naru_acars.servlet.repository.AircraftRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

public class BookingResponseDeserializer extends StdDeserializer<BookingResponse> {

    private final AircraftRepository acfRepo;
    private final Logger logger;

    public BookingResponseDeserializer() {
        this(BookingResponse.class);
    }

    public BookingResponseDeserializer(Class<?> vc) {
        super(vc);

        var factory = NaruACARS.getServiceFactory();
        this.acfRepo = factory.getBean(AircraftRepository.class);
        this.logger = NaruACARS.logger;
    }

    @Override
    public BookingResponse deserialize(JsonParser parser, DeserializationContext context) {
        try {
            JsonNode node = parser.getCodec().readTree(parser);
            var response = new BookingResponse(node);
            var flightPlan = parseFlightPlan(node);
            response.setFlightPlan(flightPlan);
            return response;
        } catch (Exception e) {
            this.logger.log(SEVERE, "Failed to deserialize JSON!", e);
            return null;
        }
    }

    private FlightPlan parseFlightPlan(JsonNode parentNode) {
        var flightPlan = new FlightPlan();
        JsonNode node = parentNode.get("response").get("flightplan");
        var callsign = node.get("callsign").asText();
        var acfValue = node.get("aircraft").asText();
        var depValue = node.get("origin").asText();
        var arrValue = node.get("destination").asText();
        var altValue = node.get("alternate").asText();
        var offValue = node.get("off_block").asText();
        var onValue = node.get("on_block").asText();
        var aircraft = this.acfRepo.find(acfValue).orElse(null);
        var offBlock = Instant.parse(offValue);
        var onBlock = Instant.parse(onValue);
        var blockTime = Duration.between(offBlock, onBlock).abs();
        var route = node.get("route").asText();
        var remarks = node.get("remarks").asText();

        flightPlan.setCallsign(callsign);
        flightPlan.setAircraft(aircraft);
        flightPlan.setDepartureCode(depValue);
        flightPlan.setArrivalCode(arrValue);
        flightPlan.setAlternateCode(altValue);
        flightPlan.setBlockTime(blockTime);
        flightPlan.setBlockOff(offBlock);
        flightPlan.setBlockOn(onBlock);
        flightPlan.setRoute(route);
        flightPlan.setRemarks(remarks);
        return flightPlan;
    }

}
