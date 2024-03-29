package com.flylazo.naru_acars.servlet.bridge;

import com.flylazo.naru_acars.domain.unit.Length;
import com.flylazo.naru_acars.domain.unit.Speed;
import com.flylazo.naru_acars.servlet.repository.AirportRepository;
import com.flylazo.naru_acars.servlet.service.SimTracker;

import java.time.LocalTime;

public class OfflineBridge extends SimBridge {
    public OfflineBridge(SimTracker tracker, AirportRepository airportRepo) {
        super("OFFLINE", tracker, airportRepo);
    }

    @Override
    public void hook() {
        // do nothing
    }

    @Override
    public void release() {
        // do nothing
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public boolean isOnGround() {
        return false;
    }

    @Override
    public boolean isGearDown() {
        return false;
    }

    @Override
    public boolean isDoorOpen() {
        return false;
    }

    @Override
    public float getFlapRatio() {
        return 0;
    }

    @Override
    public int getAltitude(Length unit) {
        return 0;
    }

    @Override
    public int getHeading(boolean magnetic) {
        return 0;
    }

    @Override
    public int getAirspeed(Speed unit) {
        return 0;
    }

    @Override
    public int getGroundSpeed(Speed unit) {
        return 0;
    }

    @Override
    public int getVerticalSpeed(Speed unit) {
        return 0;
    }

    @Override
    public LocalTime getLocalTime() {
        return LocalTime.now();
    }

    @Override
    public double getLatitude() {
        return 0;
    }

    @Override
    public double getLongitude() {
        return 0;
    }

    @Override
    public String getSimulator() {
        return "N/A";
    }

    @Override
    public int getFPS() {
        return 0;
    }

    @Override
    public double getEngineFuelFlow(int engine) {
        return 0;
    }
}
