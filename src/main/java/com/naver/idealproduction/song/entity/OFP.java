package com.naver.idealproduction.song.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OFP {
    private String callsign;
    private Aircraft aircraft;
    private String departureCode;
    private String arrivalCode;
    private String route;

    @JsonSetter("aircraft")
    public void setAircraft(Aircraft aircraft) {
        this.aircraft = aircraft;
    }

    @JsonProperty("origin")
    public void setDepartureCode(Map<String, Object> origin) {
        this.departureCode = (String) origin.get("icao_code");
    }

    @JsonProperty("destination")
    public void setArrivalCode(Map<String, Object> dest) {
        this.arrivalCode = (String) dest.get("icao_code");
    }

    @JsonProperty("general")
    public void setRoute(Map<String, Object> general) {
        this.route = (String) general.get("route");
        this.callsign = (String) general.get("icao_airline") + general.get("flight_number");
    }

    public String getCallsign() {
        return (callsign != null) ? callsign : "";
    }

    public Aircraft getAircraft() {
        return (aircraft != null) ? aircraft : new Aircraft();
    }

    public String getDeparture() {
        return (departureCode != null) ? departureCode : "";
    }

    public String getArrival() {
        return (arrivalCode != null) ? arrivalCode : "";
    }

    public String getRoute() {
        return (route != null) ? route : "";
    }
}
