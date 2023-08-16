package com.flylazo.naru_acars.servlet.bridge;

import com.flylazo.naru_acars.NaruACARS;
import com.flylazo.naru_acars.domain.Airport;
import com.flylazo.naru_acars.domain.FlightPlan;
import com.flylazo.naru_acars.domain.Phase;
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
    private Phase phase = Phase.PREFLIGHT;

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

    public Phase getFlightPhase() {
        return this.phase;
    }

    public void setFlightPhase(Phase phase) {
        this.phase = phase;
    }

    public Phase computeFlightPhase() {
        var plan = FlightPlan.getDispatched();

        if (plan == null || !isConnected()) {
            return Phase.PREFLIGHT;
        }

        return switch (this.phase) {
            case PREFLIGHT -> {
                if (isDoorOpen()) {
                    yield Phase.BOARDING;
                } else if (isEngineRunning()) {
                    yield Phase.DEPARTING;
                } else {
                    yield this.phase;
                }
            }
            case BOARDING -> {
                if (isEngineRunning()) {
                    yield Phase.DEPARTING;
                } else {
                    yield this.phase;
                }
            }
            case DEPARTING -> {
                if (isOnGround()) {
                    yield this.phase;
                } else {
                    yield Phase.CRUISING;
                }
            }
            case CRUISING -> {
                if (isOnGround()) {
                    yield Phase.LANDED;
                } else {
                    yield this.phase;
                }
            }
            case LANDED -> {
                if (isEngineRunning()) {
                    yield this.phase;
                } else {
                    yield Phase.ARRIVED;
                }
            }
            case ARRIVED -> this.phase;
        };
    }

    public boolean isEngineRunning() {
        return getEngineFuelFlow(1) > 1.0
                || getEngineFuelFlow(2) > 1.0
                || getEngineFuelFlow(3) > 1.0
                || getEngineFuelFlow(4) > 1.0;
    }

    public abstract void hook();

    public abstract void release();

    public abstract boolean isConnected();

    public abstract boolean isOnGround();

    public abstract boolean isGearDown();

    public abstract boolean isDoorOpen();

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
