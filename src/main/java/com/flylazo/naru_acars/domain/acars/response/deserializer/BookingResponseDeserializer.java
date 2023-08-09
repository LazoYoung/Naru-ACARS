package com.flylazo.naru_acars.domain.acars.response.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.flylazo.naru_acars.NaruACARS;
import com.flylazo.naru_acars.domain.Airport;
import com.flylazo.naru_acars.domain.FlightPlan;
import com.flylazo.naru_acars.domain.acars.response.BookingResponse;
import com.flylazo.naru_acars.servlet.repository.AircraftRepository;
import com.flylazo.naru_acars.servlet.repository.AirportRepository;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

public class BookingResponseDeserializer extends StdDeserializer<BookingResponse> {

    private final AircraftRepository acfRepo;
    private final AirportRepository aptRepo;

    public BookingResponseDeserializer() {
        this(BookingResponse.class);
    }

    public BookingResponseDeserializer(Class<?> vc) {
        super(vc);

        var factory = NaruACARS.getServiceFactory();
        this.acfRepo = factory.getBean(AircraftRepository.class);
        this.aptRepo = factory.getBean(AirportRepository.class);
    }

    @Override
    public BookingResponse deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        try {
            JsonNode node = parser.getCodec().readTree(parser);
            var response = new BookingResponse(node);
            var flightPlan = parseFlightPlan(node);
            var depart = node.get("response").get("schedule").get("depart").asText();
            var departTime = Instant.parse(depart);
            response.setFlightPlan(flightPlan);
            response.setDepartTime(departTime);
            return response;
        } catch (Exception e) {
            throw new IOException(e);
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
        var departure = this.aptRepo.find(depValue).map(Airport::getName).orElse(null);
        var arrival = this.aptRepo.find(arrValue).map(Airport::getName).orElse(null);
        var alternate = this.aptRepo.find(altValue).map(Airport::getName).orElse(null);
        var offBlock = Instant.parse(offValue);
        var onBlock = Instant.parse(onValue);
        var blockTime = Duration.between(offBlock, onBlock).abs();
        var route = node.get("route").asText();
        var remarks = node.get("remarks").asText();

        flightPlan.setCallsign(callsign);
        flightPlan.setAircraft(aircraft);
        flightPlan.setDepartureCode(departure);
        flightPlan.setArrivalCode(arrival);
        flightPlan.setAlternateCode(alternate);
        flightPlan.setBlockTime(blockTime);
        flightPlan.setBlockOff(offBlock);
        flightPlan.setBlockOn(onBlock);
        flightPlan.setRoute(route);
        flightPlan.setRemarks(remarks);
        return flightPlan;
    }

}
