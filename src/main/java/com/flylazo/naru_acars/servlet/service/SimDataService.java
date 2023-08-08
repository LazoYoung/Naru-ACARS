package com.flylazo.naru_acars.servlet.service;

import com.flylazo.naru_acars.domain.Aircraft;
import com.flylazo.naru_acars.domain.Airline;
import com.flylazo.naru_acars.domain.Airport;
import com.flylazo.naru_acars.domain.FlightPlan;
import com.flylazo.naru_acars.domain.overlay.SimData;
import com.flylazo.naru_acars.domain.overlay.Simvar;
import com.flylazo.naru_acars.domain.unit.Length;
import com.flylazo.naru_acars.domain.unit.Speed;
import com.flylazo.naru_acars.servlet.bridge.SimBridge;
import com.flylazo.naru_acars.servlet.repository.AirlineRepository;
import com.flylazo.naru_acars.servlet.repository.AirportRepository;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.flylazo.naru_acars.domain.overlay.Simvar.Type.*;

@Service
public class SimDataService {
    private final SimData data = new SimData();
    private final List<Runnable> listeners = new ArrayList<>();
    private final AirportRepository airportService;
    private final AirlineRepository airlineRepo;
    private final SimTracker simTracker;
    private int phaseSkipCounter = 0;
    private String lastPhase = null;

    public SimDataService(AirportRepository airportService, AirlineRepository airlineRepo, SimTracker simTracker) {
        this.airportService = airportService;
        this.airlineRepo = airlineRepo;
        this.simTracker = simTracker;
        simTracker.addUpdateListener(this::update);
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
        var plan = FlightPlan.getDispatched();
        Optional<Airport> dep = (plan != null) ? airportService.find(plan.getDepartureCode()) : Optional.empty();
        Optional<Airport> arr = (plan != null) ? airportService.find(plan.getArrivalCode()) : Optional.empty();
        Optional<Airline> airline = (plan != null) ? airlineRepo.find(plan.getAirline()) : Optional.empty();
        Optional<Aircraft> acf = (plan != null) ? Optional.ofNullable(plan.getAircraft()) : Optional.empty();
        var lat = simBridge.getLatitude();
        var lon = simBridge.getLongitude();
        boolean isOnline = (Math.abs(lat) > 0.05 || Math.abs(lon) > 0.05);
        double distKM = -1;
        double distNM = -1;

        if (isOnline && arr.isPresent()) {
            var apt = arr.get();
            distKM = Length.KILOMETER.getDistance(lat, lon, apt.getLatitude(), apt.getLongitude());
            distNM = Length.NAUTICAL_MILE.getDistance(lat, lon, apt.getLatitude(), apt.getLongitude());
        }

        data.put(FRAME_PER_SEC, simBridge.getFPS());
        data.put(LOCAL_TIME, simBridge.getLocalTime().format(timeFormat));
        data.put(ZULU_TIME, ZonedDateTime.now(ZoneOffset.UTC).format(timeFormat));
        data.put(BLOCK_RAMP_OUT_LOCAL, (plan != null) ? plan.getTimeSchedule(FlightPlan.BLOCK_OUT, true) : null);
        data.put(BLOCK_RAMP_OUT_ZULU, (plan != null) ? plan.getTimeSchedule(FlightPlan.BLOCK_OUT, false) : null);
        data.put(BLOCK_TAKE_OFF_LOCAL, (plan != null) ? plan.getTimeSchedule(FlightPlan.BLOCK_OFF, true) : null);
        data.put(BLOCK_TAKE_OFF_ZULU, (plan != null) ? plan.getTimeSchedule(FlightPlan.BLOCK_OFF, false) : null);
        data.put(BLOCK_TOUCH_DOWN_LOCAL, (plan != null) ? plan.getTimeSchedule(FlightPlan.BLOCK_ON, true) : null);
        data.put(BLOCK_TOUCH_DOWN_ZULU, (plan != null) ? plan.getTimeSchedule(FlightPlan.BLOCK_ON, false) : null);
        data.put(BLOCK_RAMP_IN_LOCAL, (plan != null) ? plan.getTimeSchedule(FlightPlan.BLOCK_IN, true) : null);
        data.put(BLOCK_RAMP_IN_ZULU, (plan != null) ? plan.getTimeSchedule(FlightPlan.BLOCK_IN, false) : null);
        data.put(ROUTE, (plan != null) ? plan.getRoute() : null);
        data.put(DTG_KM, (distKM >= 0) ? distKM : null);
        data.put(DTG_NM, (distNM >= 0) ? distNM : null);
        data.put(DEPARTURE_ICAO, dep.map(Airport::getIcao).orElse(null));
        data.put(DEPARTURE_IATA, dep.map(Airport::getIata).orElse(null));
        data.put(DEPARTURE_NAME, dep.map(Airport::getName).orElse(null));
        data.put(DEPARTURE_CITY, dep.map(Airport::getCity).orElse(null));
        data.put(ARRIVAL_ICAO, arr.map(Airport::getIcao).orElse(null));
        data.put(ARRIVAL_IATA, arr.map(Airport::getIata).orElse(null));
        data.put(ARRIVAL_NAME, arr.map(Airport::getName).orElse(null));
        data.put(ARRIVAL_CITY, arr.map(Airport::getCity).orElse(null));
        data.put(AIRLINE_ICAO, airline.map(Airline::getIcao).orElse(null));
        data.put(AIRLINE_IATA, airline.map(Airline::getIata).orElse(null));
        data.put(AIRLINE_NAME, airline.map(Airline::getName).orElse(null));
        data.put(AIRLINE_CALLSIGN, airline.map(Airline::getCallsign).orElse(null));
        data.put(AIRCRAFT_ICAO, acf.map(Aircraft::getIcaoCode).orElse(null));
        data.put(AIRCRAFT_NAME, acf.map(Aircraft::getName).orElse(null));
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
        data.put(CALLSIGN, (plan != null) ? plan.getCallsign() : null);
        data.put(PHASE, isOnline ? getFlightPhase() : null);

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
        return phase;
    }

    private void notifyListeners() {
        listeners.forEach(Runnable::run);
    }
}
