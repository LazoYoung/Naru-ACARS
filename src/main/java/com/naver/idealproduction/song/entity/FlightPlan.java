package com.naver.idealproduction.song.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.Map;
import java.util.regex.Pattern;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FlightPlan {
    private static FlightPlan instance;
    private String airline;
    private String callsign;
    private Aircraft aircraft;
    private String departureCode;
    private String arrivalCode;
    private String route;

    // todo ETE, ETA
    public static FlightPlan getInstance() {
        if (instance == null) {
            instance = new FlightPlan();
        }
        return instance;
    }

    public FlightPlan() {}

    public FlightPlan(String callsign, Aircraft aircraft, String departureCode, String arrivalCode, String route) {
        if (callsign != null) {
            var matcher = Pattern.compile("^([a-zA-Z]+)").matcher(callsign);
            if (matcher.find()) {
                var end = matcher.end();
                this.airline = callsign.substring(0, end);
            }
        }
        this.callsign = callsign;
        this.aircraft = aircraft;
        this.departureCode = departureCode;
        this.arrivalCode = arrivalCode;
        this.route = route;
    }

    public static void submit(FlightPlan newPlan) {
        instance = newPlan;
    }

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
    public void setGeneral(Map<String, Object> general) {
        this.route = (String) general.get("route");
        this.airline = (String) general.get("icao_airline");
        this.callsign = (String) general.get("icao_airline") + general.get("flight_number");
    }

    public String getAirline() {
        return (airline != null) ? airline : "";
    }

    public String getCallsign() {
        return (callsign != null) ? callsign : "";
    }

    public Aircraft getAircraft() {
        return aircraft;
    }

    public String getDepartureCode() {
        return (departureCode != null) ? departureCode : "";
    }

    public String getArrivalCode() {
        return (arrivalCode != null) ? arrivalCode : "";
    }

    public String getRoute() {
        return (route != null) ? route : "";
    }
}
