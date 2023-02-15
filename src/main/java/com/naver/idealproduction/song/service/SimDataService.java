package com.naver.idealproduction.song.service;

import com.naver.idealproduction.song.entity.FlightPlan;
import com.naver.idealproduction.song.entity.overlay.SimData;
import com.naver.idealproduction.song.entity.unit.Length;
import com.naver.idealproduction.song.entity.unit.Simvar;
import com.naver.idealproduction.song.entity.unit.Speed;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.naver.idealproduction.song.entity.unit.Simvar.*;

@Service
public class SimDataService {
    private final SimData data = new SimData();
    private final List<Runnable> listeners = new ArrayList<>();
    private final AirportService airportService;
    private final AirlineService airlineService;
    private final SimTracker simTracker;

    public SimDataService(AirportService airportService, AirlineService airlineService, SimTracker simTracker) {
        this.airportService = airportService;
        this.airlineService = airlineService;
        this.simTracker = simTracker;
        simTracker.addUpdateListener(this::update);
    }

    public Object getVariable(Simvar simvar) {
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
        var notAvail = "N/A";
        var timeFormat = DateTimeFormatter.ofPattern("HH:mm");
        var plan = FlightPlan.getInstance();
        var dep = (plan != null) ? airportService.get(plan.getDepartureCode()) : null;
        var arr = (plan != null) ? airportService.get(plan.getArrivalCode()) : null;
        var airline = (plan != null) ? airlineService.get(plan.getAirline()) : null;
        var acf = (plan != null) ? plan.getAircraft() : null;
        var dist = (arr != null) ? Length.KILOMETER.getDistance(simBridge.getLatitude(), simBridge.getLongitude(), arr.getLatitude(), arr.getLongitude()) : null;

        data.put(LOCAL_TIME, simBridge.getLocalTime().format(timeFormat));
        data.put(ZULU_TIME, ZonedDateTime.now(ZoneOffset.UTC).format(timeFormat) + "z");
        data.put(DTG_KM, (dist != null) ? dist : notAvail);
        data.put(DTG_NM, (dist != null) ? Length.KILOMETER.convertTo(Length.NAUTICAL_MILE, dist) : notAvail);
        data.put(DEPARTURE_ICAO, (dep != null) ? dep.getIcao() : notAvail);
        data.put(DEPARTURE_IATA, (dep != null) ? dep.getIata() : notAvail);
        data.put(DEPARTURE_NAME, (dep != null) ? dep.getName() : notAvail);
        data.put(DEPARTURE_CITY, (dep != null) ? dep.getCity() : notAvail);
        data.put(ARRIVAL_ICAO, (arr != null) ? arr.getIcao() : notAvail);
        data.put(ARRIVAL_IATA, (arr != null) ? arr.getIata() : notAvail);
        data.put(ARRIVAL_NAME, (arr != null) ? arr.getName() : notAvail);
        data.put(ARRIVAL_CITY, (arr != null) ? arr.getCity() : notAvail);
        data.put(AIRLINE_ICAO, (airline != null) ? airline.getIcao() : notAvail);
        data.put(AIRLINE_IATA, (airline != null) ? airline.getIata() : notAvail);
        data.put(AIRLINE_NAME, (airline != null) ? airline.getName() : notAvail);
        data.put(AIRLINE_CALLSIGN, (airline != null) ? airline.getCallsign() : notAvail);
        data.put(AIRCRAFT_ICAO, (acf != null) ? acf.getIcaoCode() : notAvail);
        data.put(AIRCRAFT_NAME, (acf != null) ? acf.getName() : notAvail);
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
        // todo FF data needs to be verified
        data.put(ENGINE1_FUEL_FLOW, simBridge.getEngineFuelFlow(1));
        data.put(ENGINE1_FUEL_FLOW, simBridge.getEngineFuelFlow(2));
        data.put(ENGINE1_FUEL_FLOW, simBridge.getEngineFuelFlow(3));
        data.put(ENGINE1_FUEL_FLOW, simBridge.getEngineFuelFlow(4));
        data.put(CALLSIGN, (plan != null) ? plan.getCallsign() : notAvail);
        data.put(PHASE, simBridge.getFlightPhase());

        notifyListeners();
    }

    private void notifyListeners() {
        listeners.forEach(Runnable::run);
    }
}
