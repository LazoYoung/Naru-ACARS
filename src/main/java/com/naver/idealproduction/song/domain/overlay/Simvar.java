package com.naver.idealproduction.song.domain.overlay;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import org.eclipse.persistence.oxm.annotations.XmlPath;

@XmlType(name = "simvar")
public class Simvar {
    public enum Type {
        LOCAL_TIME,
        ZULU_TIME,
        BLOCK_RAMP_OUT_LOCAL,
        BLOCK_RAMP_OUT_ZULU,
        BLOCK_TAKE_OFF_LOCAL,
        BLOCK_TAKE_OFF_ZULU,
        BLOCK_TOUCH_DOWN_LOCAL,
        BLOCK_TOUCH_DOWN_ZULU,
        BLOCK_RAMP_IN_LOCAL,
        BLOCK_RAMP_IN_ZULU,
        ROUTE,
        DTG_KM,
        DTG_NM,
        DEPARTURE_ICAO,
        DEPARTURE_IATA,
        DEPARTURE_NAME,
        DEPARTURE_CITY,
        ARRIVAL_ICAO,
        ARRIVAL_IATA,
        ARRIVAL_NAME,
        ARRIVAL_CITY,
        AIRLINE_ICAO,
        AIRLINE_IATA,
        AIRLINE_NAME,
        AIRLINE_CALLSIGN,
        AIRCRAFT_ICAO,
        AIRCRAFT_NAME,
        LATITUDE,
        LONGITUDE,
        ALTITUDE_FEET,
        ALTITUDE_METER,
        HEADING_MAG,
        HEADING_TRUE,
        AIRSPEED_KNOT,
        AIRSPEED_KPH,
        AIRSPEED_MPH,
        GROUND_SPEED_KNOT,
        GROUND_SPEED_KPH,
        GROUND_SPEED_MPH,
        VERTICAL_SPEED,
        ENGINE1_FUEL_FLOW,
        ENGINE2_FUEL_FLOW,
        ENGINE3_FUEL_FLOW,
        ENGINE4_FUEL_FLOW,
        CALLSIGN,
        PHASE
    }

    private String prefix;
    private String suffix;
    private int roundScale;
    private String variable;

    @XmlAttribute(name = "prefix")
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @XmlAttribute(name = "suffix")
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    @XmlAttribute(name = "round-scale")
    public void setRoundScale(int roundScale) {
        this.roundScale = roundScale;
    }

    @XmlPath("text()")
    public void setVariable(String variable) {
        this.variable = variable;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public int getRoundScale() {
        return roundScale;
    }

    public String getVariable() {
        return variable;
    }
}