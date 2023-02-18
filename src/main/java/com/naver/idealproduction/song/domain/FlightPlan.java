package com.naver.idealproduction.song.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.annotation.Nullable;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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
    private String route;
    private Instant blockOut;
    private Instant blockOff;
    private Instant blockOn;
    private Instant blockIn;
    private ZoneId origZone;
    private ZoneId destZone;

    public static FlightPlan getInstance() {
        if (instance == null) {
            instance = new FlightPlan();
        }
        return instance;
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

    @JsonProperty("times")
    public void setBlockTime(Map<String, Object> times) {
        int out = Integer.parseInt((String) times.get("sched_out"));
        int off = Integer.parseInt((String) times.get("sched_off"));
        int on = Integer.parseInt((String) times.get("sched_on"));
        int in = Integer.parseInt((String) times.get("sched_in"));
        int orig = Integer.parseInt((String) times.get("orig_timezone"));
        int dest = Integer.parseInt((String) times.get("dest_timezone"));
        this.blockOut = Instant.ofEpochSecond(out);
        this.blockOff = Instant.ofEpochSecond(off);
        this.blockOn = Instant.ofEpochSecond(on);
        this.blockIn = Instant.ofEpochSecond(in);
        this.origZone = ZoneOffset.ofHours(orig);
        this.destZone = ZoneOffset.ofHours(dest);
    }

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

    public String getRoute() {
        return (route != null) ? route : "";
    }

    @Nullable
    public ZonedDateTime getBlockTime(int type, boolean local) {
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
    public String getBlockTimeFormatted(int type, boolean local) {
        var dateTime = getBlockTime(type, local);

        if (dateTime != null) {
            return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        } else {
            return null;
        }
    }
}
