package com.naver.idealproduction.song.servlet.service;

import com.naver.idealproduction.song.domain.FlightPlan;
import com.naver.idealproduction.song.domain.overlay.SimData;
import com.naver.idealproduction.song.servlet.repository.AirlineRepository;
import com.naver.idealproduction.song.servlet.repository.AirportRepository;
import com.naver.idealproduction.song.domain.unit.Length;
import com.naver.idealproduction.song.domain.overlay.Simvar;
import com.naver.idealproduction.song.domain.unit.Speed;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.naver.idealproduction.song.domain.overlay.Simvar.Type.*;

@Service
public class SimDataService {
    private final SimData data = new SimData();
    private final List<Runnable> listeners = new ArrayList<>();
    private final AirportRepository airportService;
    private final AirlineRepository airlineRepo;
    private final SimTracker simTracker;
    private final String notAvail = "N/A";
    private int phaseSkipCounter = 0;
    private String lastPhase = null;

    public SimDataService(AirportRepository airportService, AirlineRepository airlineRepo, SimTracker simTracker) {
        this.airportService = airportService;
        this.airlineRepo = airlineRepo;
        this.simTracker = simTracker;
        simTracker.addProcessListener(this::update);
    }

    public Object getVariable(Simvar.Type simvar) {
        return data.get(simvar);
    }

    public SimData getDataEntity() {
        return data;
    }

    public void addUpdateListener(Runnable run) {
        listeners.add(run);
    }

    public void requestUpdate() {
        update(simTracker.getBridge());
    }

    private void update(SimBridge simBridge) {
        var timeFormat = DateTimeFormatter.ofPattern("HH:mm");
        var plan = FlightPlan.getInstance();
        var dep = (plan != null) ? airportService.get(plan.getDepartureCode()) : null;
        var arr = (plan != null) ? airportService.get(plan.getArrivalCode()) : null;
        var airline = (plan != null) ? airlineRepo.get(plan.getAirline()) : null;
        var acf = (plan != null) ? plan.getAircraft() : null;
        var dist = (arr != null) ? Length.KILOMETER.getDistance(simBridge.getLatitude(), simBridge.getLongitude(), arr.getLatitude(), arr.getLongitude()) : null;

        data.put(LOCAL_TIME, simBridge.getLocalTime().format(timeFormat));
        data.put(ZULU_TIME, ZonedDateTime.now(ZoneOffset.UTC).format(timeFormat));
        data.put(BLOCK_RAMP_OUT_LOCAL, (plan != null) ? plan.getBlockTimeFormatted(BLOCK_OUT, true) : null);
        data.put(BLOCK_RAMP_OUT_ZULU, (plan != null) ? plan.getBlockTimeFormatted(BLOCK_OUT, false) : null);
        data.put(BLOCK_TAKE_OFF_LOCAL, (plan != null) ? plan.getBlockTimeFormatted(BLOCK_OFF, true) : null);
        data.put(BLOCK_TAKE_OFF_ZULU, (plan != null) ? plan.getBlockTimeFormatted(BLOCK_OFF, false) : null);
        data.put(BLOCK_TOUCH_DOWN_LOCAL, (plan != null) ? plan.getBlockTimeFormatted(BLOCK_ON, true) : null);
        data.put(BLOCK_TOUCH_DOWN_ZULU, (plan != null) ? plan.getBlockTimeFormatted(BLOCK_ON, false) : null);
        data.put(BLOCK_RAMP_IN_LOCAL, (plan != null) ? plan.getBlockTimeFormatted(BLOCK_IN, true) : null);
        data.put(BLOCK_RAMP_IN_ZULU, (plan != null) ? plan.getBlockTimeFormatted(BLOCK_IN, false) : null);
        data.put(ROUTE, (plan != null) ? plan.getRoute() : null);
        data.put(DTG_KM, (distKM >= 0) ? distKM : null);
        data.put(DTG_NM, (distNM >= 0) ? distNM : null);
        data.put(DEPARTURE_ICAO, (dep != null) ? dep.getIcao() : null);
        data.put(DEPARTURE_IATA, (dep != null) ? dep.getIata() : null);
        data.put(DEPARTURE_NAME, (dep != null) ? dep.getName() : null);
        data.put(DEPARTURE_CITY, (dep != null) ? dep.getCity() : null);
        data.put(ARRIVAL_ICAO, (arr != null) ? arr.getIcao() : null);
        data.put(ARRIVAL_IATA, (arr != null) ? arr.getIata() : null);
        data.put(ARRIVAL_NAME, (arr != null) ? arr.getName() : null);
        data.put(ARRIVAL_CITY, (arr != null) ? arr.getCity() : null);
        data.put(AIRLINE_ICAO, (airline != null) ? airline.getIcao() : null);
        data.put(AIRLINE_IATA, (airline != null) ? airline.getIata() : null);
        data.put(AIRLINE_NAME, (airline != null) ? airline.getName() : null);
        data.put(AIRLINE_CALLSIGN, (airline != null) ? airline.getCallsign() : null);
        data.put(AIRCRAFT_ICAO, (acf != null) ? acf.getIcaoCode() : null);
        data.put(AIRCRAFT_NAME, (acf != null) ? acf.getName() : null);
        data.put(LATITUDE, simBridge.getLatitude());
        data.put(LONGITUDE, simBridge.getLongitude());
        data.put(ALTITUDE_FEET, simBridge.getAltitude(Length.FEET));
        data.put(ALTITUDE_METER, simBridge.getAltitude(Length.METER));
        data.put(HEADING_MAG, simBridge.getHeading(true));
        data.put(HEADING_TRUE, simBridge.getHeading(false));
        data.put(AIRSPEED_KNOT, simBridge.getAirspeed(Speed.KNOT));
        data.put(AIRSPEED_KPH, simBridge.getAirspeed(Speed.KILOMETER_PER_HOUR));
        data.put(AIRSPEED_MPH, simBridge.getAirspeed(Speed.MILE_PER_HOUR));
        data.put(GROUND_SPEED_KNOT, simBridge.getGroundSpeed(Speed.KNOT));
        data.put(GROUND_SPEED_KPH, simBridge.getGroundSpeed(Speed.KILOMETER_PER_HOUR));
        data.put(GROUND_SPEED_MPH, simBridge.getGroundSpeed(Speed.MILE_PER_HOUR));
        data.put(VERTICAL_SPEED, simBridge.getVerticalSpeed(Speed.FEET_PER_MIN));
        data.put(ENGINE1_FUEL_FLOW, simBridge.getEngineFuelFlow(1));
        data.put(ENGINE2_FUEL_FLOW, simBridge.getEngineFuelFlow(2));
        data.put(ENGINE3_FUEL_FLOW, simBridge.getEngineFuelFlow(3));
        data.put(ENGINE4_FUEL_FLOW, simBridge.getEngineFuelFlow(4));
        data.put(CALLSIGN, (plan != null) ? plan.getCallsign() : notAvail);
        data.put(PHASE, getFlightPhase());

        notifyListeners();
    }

    private String getFlightPhase() {
        String phase;

        if (--phaseSkipCounter >= 0) {
            phase = lastPhase;
        } else {
            double heavyRefreshRate = 5000;
            int refreshRate = simTracker.getRefreshRate();
            phaseSkipCounter = (int) Math.ceil(heavyRefreshRate / refreshRate);
            phase = simTracker.getBridge().getFlightPhase();
            lastPhase = phase;
        }
        return (phase != null) ? phase : notAvail;
    }

    private void notifyListeners() {
        listeners.forEach(Runnable::run);
    }
}
