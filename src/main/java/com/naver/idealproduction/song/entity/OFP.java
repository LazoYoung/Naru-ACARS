package com.naver.idealproduction.song.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.Map;

public class OFP {
    private String icaoAirline;
    private int flightNumber;
    private Aircraft aircraft;
    private String departureCode;
    private String arrivalCode;
    private String route;

    @JsonSetter("icao_airline")
    public void setIcaoAirline(String icaoAirline) {
        this.icaoAirline = icaoAirline;
    }

    @JsonSetter("flight_number")
    public void setFlightNumber(int flightNumber) {
        this.flightNumber = flightNumber;
    }

    @JsonSetter("aircraft")
    public void setAircraft(Aircraft aircraft) {
        this.aircraft = aircraft;
    }

    @JsonProperty("origin")
    public void setDepartureCode(Map<String, String> origin) {
        this.departureCode = origin.get("icao_code");
    }

    @JsonProperty("destination")
    public void setArrivalCode(Map<String, Object> dest) {
        this.arrivalCode = (String) dest.get("icao_code");
    }

    @JsonProperty("general")
    public void setRoute(Map<String, Object> general) {
        this.route = (String) general.get("route");
    }

    public String getCallsign() {
        return icaoAirline + flightNumber;
    }

    public Aircraft getAircraft() {
        return aircraft;
    }

    public String getDeparture() {
        return departureCode;
    }

    public String getArrival() {
        return arrivalCode;
    }

    public String getRoute() {
        return route;
    }
}
