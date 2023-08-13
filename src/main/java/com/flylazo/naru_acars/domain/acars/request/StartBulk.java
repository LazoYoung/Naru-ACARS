package com.flylazo.naru_acars.domain.acars.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flylazo.naru_acars.domain.Aircraft;
import com.flylazo.naru_acars.domain.FlightPlan;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class StartBulk extends Bulk {
    @JsonProperty
    public boolean scheduled;

    @JsonProperty
    public Map<String, String> flightplan;

    public StartBulk(FlightPlan plan, boolean scheduled) {
        this.flightplan = new HashMap<>();
        this.scheduled = scheduled;
        this.setFlightPlan(plan);
    }

    private void setFlightPlan(FlightPlan plan) {
        var aircraft = Optional.ofNullable(plan.getAircraft())
                .map(Aircraft::getIcaoCode)
                .orElse(null);
        var offBlock = Optional.ofNullable(plan.getBlockOff())
                .map(Instant::toString)
                .orElse(null);
        var onBlock = Optional.ofNullable(plan.getBlockOn())
                .map(Instant::toString)
                .orElse(null);
        this.flightplan.put("callsign", plan.getCallsign());
        this.flightplan.put("aircraft", aircraft);
        this.flightplan.put("origin", plan.getDepartureCode());
        this.flightplan.put("destination", plan.getArrivalCode());
        this.flightplan.put("alternate", plan.getAlternateCode());
        this.flightplan.put("off_block", offBlock);
        this.flightplan.put("on_block", onBlock);
        this.flightplan.put("route", plan.getRoute());
        this.flightplan.put("remarks", plan.getRemarks());
        this.flightplan.values().removeAll(Collections.singleton(null));
    }
}
