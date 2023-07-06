package com.naver.idealproduction.song.servlet.bridge;

import com.naver.idealproduction.song.domain.unit.Length;
import com.naver.idealproduction.song.domain.unit.Speed;
import com.naver.idealproduction.song.servlet.repository.AirportRepository;
import com.naver.idealproduction.song.servlet.service.SimTracker;

import java.time.LocalTime;

public class OfflineBridge extends SimBridge {
    public OfflineBridge(SimTracker tracker, AirportRepository airportRepo) {
        super(tracker, airportRepo);
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
    public String getFlightPhase() {
        return "N/A";
    }

    @Override
    public double getEngineFuelFlow(int engine) {
        return 0;
    }
}
