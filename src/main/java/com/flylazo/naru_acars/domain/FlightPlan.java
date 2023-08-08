package com.flylazo.naru_acars.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.annotation.Nullable;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Pattern;

import static java.time.ZoneOffset.UTC;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FlightPlan {
    public static final int BLOCK_OUT = 0;
    public static final int BLOCK_OFF = 1;
    public static final int BLOCK_ON = 2;
    public static final int BLOCK_IN = 3;
    private static FlightPlan instance;
    private String airline;
    private String callsign;
    private Aircraft aircraft;
    private String departureCode;
    private String arrivalCode;
    private String alternateCode;
    private String route;
    private String remarks;
    private Duration blockTime;
    private Instant blockOut;
    private Instant blockOff;
    private Instant blockOn;
    private Instant blockIn;
    private ZoneId origZone;
    private ZoneId destZone;

    public static FlightPlan getDispatched() {
        if (instance == null) {
            instance = new FlightPlan();
        }
        return instance;
    }

    public static void submit(FlightPlan newPlan) {
        instance = newPlan;
    }

    // --- JSON setters --- //

    @JsonSetter("aircraft")
    public void setAircraft(Aircraft aircraft) {
        this.aircraft = aircraft;
    }

    @JsonProperty("origin")
    public void setDepartureCode(Map<String, Object> map) {
        this.departureCode = (String) map.get("icao_code");
    }

    @JsonProperty("destination")
    public void setArrivalCode(Map<String, Object> map) {
        this.arrivalCode = (String) map.get("icao_code");
    }

    @JsonProperty("alternate")
    public void setAlternateCode(Map<String, Object> map) {
        this.alternateCode = (String) map.get("icao_code");
    }

    @JsonProperty("general")
    public void setGeneral(Map<String, Object> map) {
        this.route = (String) map.get("route");
        this.remarks = (String) map.get("dx_rmk");
        this.airline = (String) map.get("icao_airline");
        this.callsign = (String) map.get("icao_airline") + map.get("flight_number");
    }

    @JsonProperty("times")
    public void setTimes(Map<String, Object> map) {
        int out = Integer.parseInt((String) map.get("sched_out"));
        int off = Integer.parseInt((String) map.get("sched_off"));
        int on = Integer.parseInt((String) map.get("sched_on"));
        int in = Integer.parseInt((String) map.get("sched_in"));
        int block = Integer.parseInt((String) map.get("sched_block"));
        int orig = Integer.parseInt((String) map.get("orig_timezone"));
        int dest = Integer.parseInt((String) map.get("dest_timezone"));
        this.blockTime = Duration.ofSeconds(block);
        this.blockOut = Instant.ofEpochSecond(out);
        this.blockOff = Instant.ofEpochSecond(off);
        this.blockOn = Instant.ofEpochSecond(on);
        this.blockIn = Instant.ofEpochSecond(in);
        this.origZone = ZoneOffset.ofHours(orig);
        this.destZone = ZoneOffset.ofHours(dest);
    }

    // --- Native setters --- //

    public void setCallsign(String callsign) {
        this.callsign = callsign;

        if (callsign != null) {
            var matcher = Pattern.compile("^([a-zA-Z]+)").matcher(callsign);
            if (matcher.find()) {
                var end = matcher.end();
                this.airline = callsign.substring(0, end);
            }
        }
    }

    public void setDepartureCode(String code) {
        this.departureCode = code;
    }

    public void setArrivalCode(String code) {
        this.arrivalCode = code;
    }

    public void setAlternateCode(String code) {
        this.alternateCode = code;
    }

    public void setBlockTime(Duration time) {
        this.blockTime = time;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getAirline() {
        return (airline != null) ? airline : "";
    }

    public String getCallsign() {
        return (callsign != null) ? callsign : "";
    }

    @Nullable
    public Aircraft getAircraft() {
        return aircraft;
    }

    public String getDepartureCode() {
        return (departureCode != null) ? departureCode : "";
    }

    public String getArrivalCode() {
        return (arrivalCode != null) ? arrivalCode : "";
    }

    public String getAlternateCode() {
        return (alternateCode != null) ? alternateCode : "";
    }

    public String getRoute() {
        return (route != null) ? route : "";
    }

    public String getRemarks() {
        return (remarks != null) ? remarks : "";
    }

    public Duration getBlockTime() {
        return this.blockTime;
    }

    @Nullable
    public ZonedDateTime getTime(int type, boolean local) {
        if (blockOut == null || blockOff == null || blockOn == null || blockIn == null) {
            return null;
        }
        return switch (type) {
            case BLOCK_OUT -> blockOut.atZone(local ? origZone : UTC);
            case BLOCK_OFF -> blockOff.atZone(local ? origZone : UTC);
            case BLOCK_ON -> blockOn.atZone(local ? destZone : UTC);
            case BLOCK_IN -> blockIn.atZone(local ? destZone : UTC);
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    @Nullable
    public String getTimeSchedule(int type, boolean local) {
        var dateTime = getTime(type, local);

        if (dateTime != null) {
            return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        } else {
            return null;
        }
    }
}
