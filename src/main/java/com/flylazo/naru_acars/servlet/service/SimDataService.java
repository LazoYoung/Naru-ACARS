package com.flylazo.naru_acars.servlet.service;

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
        var plan = FlightPlan.getInstance();
        var dep = (plan != null) ? airportService.get(plan.getDepartureCode()) : null;
        var arr = (plan != null) ? airportService.get(plan.getArrivalCode()) : null;
        var airline = (plan != null) ? airlineRepo.get(plan.getAirline()) : null;
        var acf = (plan != null) ? plan.getAircraft() : null;
        var lat = simBridge.getLatitude();
        var lon = simBridge.getLongitude();
        boolean isOnline = (Math.abs(lat) > 0.05 || Math.abs(lon) > 0.05);
        double distKM = -1;
        double distNM = -1;

        if (isOnline && arr != null) {
            distKM = Length.KILOMETER.getDistance(lat, lon, arr.getLatitude(), arr.getLongitude());
            distNM = Length.NAUTICAL_MILE.getDistance(lat, lon, arr.getLatitude(), arr.getLongitude());
        }

        data.put(Simvar.Type.FRAME_PER_SEC, simBridge.getFPS());
        data.put(Simvar.Type.LOCAL_TIME, simBridge.getLocalTime().format(timeFormat));
        data.put(Simvar.Type.ZULU_TIME, ZonedDateTime.now(ZoneOffset.UTC).format(timeFormat));
        data.put(Simvar.Type.BLOCK_RAMP_OUT_LOCAL, (plan != null) ? plan.getBlockTimeFormatted(FlightPlan.BLOCK_OUT, true) : null);
        data.put(Simvar.Type.BLOCK_RAMP_OUT_ZULU, (plan != null) ? plan.getBlockTimeFormatted(FlightPlan.BLOCK_OUT, false) : null);
        data.put(Simvar.Type.BLOCK_TAKE_OFF_LOCAL, (plan != null) ? plan.getBlockTimeFormatted(FlightPlan.BLOCK_OFF, true) : null);
        data.put(Simvar.Type.BLOCK_TAKE_OFF_ZULU, (plan != null) ? plan.getBlockTimeFormatted(FlightPlan.BLOCK_OFF, false) : null);
        data.put(Simvar.Type.BLOCK_TOUCH_DOWN_LOCAL, (plan != null) ? plan.getBlockTimeFormatted(FlightPlan.BLOCK_ON, true) : null);
        data.put(Simvar.Type.BLOCK_TOUCH_DOWN_ZULU, (plan != null) ? plan.getBlockTimeFormatted(FlightPlan.BLOCK_ON, false) : null);
        data.put(Simvar.Type.BLOCK_RAMP_IN_LOCAL, (plan != null) ? plan.getBlockTimeFormatted(FlightPlan.BLOCK_IN, true) : null);
        data.put(Simvar.Type.BLOCK_RAMP_IN_ZULU, (plan != null) ? plan.getBlockTimeFormatted(FlightPlan.BLOCK_IN, false) : null);
        data.put(Simvar.Type.ROUTE, (plan != null) ? plan.getRoute() : null);
        data.put(Simvar.Type.DTG_KM, (distKM >= 0) ? distKM : null);
        data.put(Simvar.Type.DTG_NM, (distNM >= 0) ? distNM : null);
        data.put(Simvar.Type.DEPARTURE_ICAO, (dep != null) ? dep.getIcao() : null);
        data.put(Simvar.Type.DEPARTURE_IATA, (dep != null) ? dep.getIata() : null);
        data.put(Simvar.Type.DEPARTURE_NAME, (dep != null) ? dep.getName() : null);
        data.put(Simvar.Type.DEPARTURE_CITY, (dep != null) ? dep.getCity() : null);
        data.put(Simvar.Type.ARRIVAL_ICAO, (arr != null) ? arr.getIcao() : null);
        data.put(Simvar.Type.ARRIVAL_IATA, (arr != null) ? arr.getIata() : null);
        data.put(Simvar.Type.ARRIVAL_NAME, (arr != null) ? arr.getName() : null);
        data.put(Simvar.Type.ARRIVAL_CITY, (arr != null) ? arr.getCity() : null);
        data.put(Simvar.Type.AIRLINE_ICAO, (airline != null) ? airline.getIcao() : null);
        data.put(Simvar.Type.AIRLINE_IATA, (airline != null) ? airline.getIata() : null);
        data.put(Simvar.Type.AIRLINE_NAME, (airline != null) ? airline.getName() : null);
        data.put(Simvar.Type.AIRLINE_CALLSIGN, (airline != null) ? airline.getCallsign() : null);
        data.put(Simvar.Type.AIRCRAFT_ICAO, (acf != null) ? acf.getIcaoCode() : null);
        data.put(Simvar.Type.AIRCRAFT_NAME, (acf != null) ? acf.getName() : null);
        data.put(Simvar.Type.LATITUDE, simBridge.getLatitude());
        data.put(Simvar.Type.LONGITUDE, simBridge.getLongitude());
        data.put(Simvar.Type.ALTITUDE_FEET, simBridge.getAltitude(Length.FEET));
        data.put(Simvar.Type.ALTITUDE_METER, simBridge.getAltitude(Length.METER));
        data.put(Simvar.Type.HEADING_MAG, simBridge.getHeading(true));
        data.put(Simvar.Type.HEADING_TRUE, simBridge.getHeading(false));
        data.put(Simvar.Type.AIRSPEED_KNOT, simBridge.getAirspeed(Speed.KNOT));
        data.put(Simvar.Type.AIRSPEED_KPH, simBridge.getAirspeed(Speed.KILOMETER_PER_HOUR));
        data.put(Simvar.Type.AIRSPEED_MPH, simBridge.getAirspeed(Speed.MILE_PER_HOUR));
        data.put(Simvar.Type.GROUND_SPEED_KNOT, simBridge.getGroundSpeed(Speed.KNOT));
        data.put(Simvar.Type.GROUND_SPEED_KPH, simBridge.getGroundSpeed(Speed.KILOMETER_PER_HOUR));
        data.put(Simvar.Type.GROUND_SPEED_MPH, simBridge.getGroundSpeed(Speed.MILE_PER_HOUR));
        data.put(Simvar.Type.VERTICAL_SPEED, simBridge.getVerticalSpeed(Speed.FEET_PER_MIN));
        data.put(Simvar.Type.ENGINE1_FUEL_FLOW, simBridge.getEngineFuelFlow(1));
        data.put(Simvar.Type.ENGINE2_FUEL_FLOW, simBridge.getEngineFuelFlow(2));
        data.put(Simvar.Type.ENGINE3_FUEL_FLOW, simBridge.getEngineFuelFlow(3));
        data.put(Simvar.Type.ENGINE4_FUEL_FLOW, simBridge.getEngineFuelFlow(4));
        data.put(Simvar.Type.CALLSIGN, (plan != null) ? plan.getCallsign() : null);
        data.put(Simvar.Type.PHASE, isOnline ? getFlightPhase() : null);

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
