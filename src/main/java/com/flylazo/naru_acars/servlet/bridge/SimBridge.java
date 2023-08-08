package com.flylazo.naru_acars.servlet.bridge;

import com.flylazo.naru_acars.NaruACARS;
import com.flylazo.naru_acars.domain.Airport;
import com.flylazo.naru_acars.domain.FlightPlan;
import com.flylazo.naru_acars.domain.unit.Length;
import com.flylazo.naru_acars.domain.unit.Speed;
import com.flylazo.naru_acars.servlet.repository.AirportRepository;
import com.flylazo.naru_acars.servlet.service.SimTracker;

import java.time.LocalTime;
import java.util.Optional;
import java.util.logging.Logger;

public abstract class SimBridge {

    protected static final Logger logger = Logger.getLogger(NaruACARS.class.getName());
    protected final BridgeListener listener;
    protected final int refreshRate;
    protected final AirportRepository airportRepo;
    private final String name;

    public SimBridge(String name, SimTracker tracker, AirportRepository airportRepo) {
        this.name = name;
        this.listener = tracker;
        this.refreshRate = tracker.getRefreshRate();
        this.airportRepo = airportRepo;
    }

    public String getBridgeName() {
        return name;
    }

    public Optional<Airport> getAirport(String icao) {
        return airportRepo.find(icao);
    }

    public String getFlightPhase() {
        var plan = FlightPlan.getDispatched();

        if (plan == null || !isConnected()) {
            return null;
        }

        if (isOnGround()) {
            var depCode = plan.getDepartureCode();
            var arrCode = plan.getArrivalCode();
            var dep = airportRepo.find(depCode);
            var arr = airportRepo.find(arrCode);

            if (dep.isEmpty() || arr.isEmpty() || depCode.equals(arrCode)) {
                return "ON GROUND";
            }

            if (getEngineFuelFlow(1) < 1.0 && getEngineFuelFlow(2) < 1.0
                    && getEngineFuelFlow(3) < 1.0 && getEngineFuelFlow(4) < 1.0) {
                return "AT GATE";
            }

            var depLat = dep.get().getLatitude();
            var depLon = dep.get().getLongitude();
            var lat = getLatitude();
            var lon = getLongitude();
            var distance = Length.KILOMETER.getDistance(depLat, depLon, lat, lon);
            return (distance < 30.0) ? "DEPARTING" : "ARRIVED";
        }

        // 0 = Flaps up, 1 = Flaps full
        // float flaps = (flapsHandle.getValue() / 16383f);
        // boolean gearDown = (gearHandle.getValue() == 16383);
        int vs = getVerticalSpeed(Speed.FEET_PER_MIN);

        if (getFlapRatio() > 0.2f && vs < 100) {
            return isGearDown() ? "LANDING" : "APPROACHING";
        }

        if (vs > 300) {
            return "CLIMBING";
        } else if (vs < -300) {
            return "DESCENDING";
        } else {
            return "EN ROUTE";
        }
    }

    public abstract void hook();

    public abstract void release();

    public abstract boolean isConnected();

    public abstract boolean isOnGround();

    public abstract boolean isGearDown();

    public abstract float getFlapRatio();

    public abstract int getAltitude(Length unit);

    public abstract int getHeading(boolean magnetic);

    public abstract int getAirspeed(Speed unit);

    public abstract int getGroundSpeed(Speed unit);

    public abstract int getVerticalSpeed(Speed unit);

    public abstract LocalTime getLocalTime();

    public abstract double getLatitude();

    public abstract double getLongitude();

    public abstract String getSimulator();

    public abstract int getFPS();

    public abstract double getEngineFuelFlow(int engine);
}
