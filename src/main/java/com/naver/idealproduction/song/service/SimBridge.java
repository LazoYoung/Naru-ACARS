package com.naver.idealproduction.song.service;

import com.mouseviator.fsuipc.FSUIPC;
import com.mouseviator.fsuipc.datarequest.IDataRequest;
import com.mouseviator.fsuipc.datarequest.primitives.*;
import com.mouseviator.fsuipc.helpers.SimHelper;
import com.mouseviator.fsuipc.helpers.aircraft.*;
import com.mouseviator.fsuipc.helpers.avionics.GPSHelper;
import com.naver.idealproduction.song.entity.Airport;
import com.naver.idealproduction.song.entity.FlightPlan;
import com.naver.idealproduction.song.entity.unit.Length;
import com.naver.idealproduction.song.entity.unit.Speed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Optional;

@Service
public class SimBridge {
    private final AirportService airportService;
    private final IDataRequest<Float> fps;
    private final DoubleRequest altitude;
    private final FloatRequest headingTrue;
    private final DoubleRequest headingMag;
    private final FloatRequest airspeed;
    private final IDataRequest<Double> groundSpeed;
    private final FloatRequest verticalSpeed;
    private final IntRequest localTime;
    private final DoubleRequest aircraftLatitude;
    private final DoubleRequest aircraftLongitude;
    private final IDataRequest<Short> onGround;
    private final IDataRequest<Integer> flapsHandle;
    private final IDataRequest<Integer> gearHandle;
    private final DoubleRequest jetEng1FF;
    private final DoubleRequest jetEng2FF;
    private final DoubleRequest jetEng3FF;
    private final DoubleRequest jetEng4FF;
    private final DoubleRequest pistonEng1FF;
    private final DoubleRequest pistonEng2FF;
    private final DoubleRequest pistonEng3FF;
    private final DoubleRequest pistonEng4FF;
    private final FSUIPC fsuipc = FSUIPC.getInstance();
    private final int refreshRate = 500;
    private int phaseSkipCounter = 0;
    private String phase;

    @SuppressWarnings("unchecked")
    @Autowired
    public SimBridge(AirportService airportService) {
        this.airportService = airportService;
        var aircraft = new AircraftHelper();
        var gps = new GPSHelper();
        var sim = new SimHelper();
        fps = fsuipc.addContinualRequest(sim.getFrameRate());
        altitude = (DoubleRequest) fsuipc.addContinualRequest(aircraft.getAltitude(true));
        headingTrue = (FloatRequest) fsuipc.addContinualRequest(aircraft.getHeading());
        headingMag = (DoubleRequest) fsuipc.addContinualRequest(new DoubleRequest() {
            {
                this.dataBuffer = new byte[BUFFER_LENGTH_DOUBLE];
                this.offset = 0x2B00;
            }
        });
        airspeed = (FloatRequest) fsuipc.addContinualRequest(aircraft.getIAS());
        groundSpeed = (IDataRequest<Double>) fsuipc.addContinualRequest(gps.getGroundSpeed(true));
        verticalSpeed = (FloatRequest) fsuipc.addContinualRequest(aircraft.getVerticalSpeed(true));
        localTime = (IntRequest) fsuipc.addContinualRequest(sim.getLocalTime());
        aircraftLatitude = (DoubleRequest) fsuipc.addContinualRequest(aircraft.getLatitude());
        aircraftLongitude = (DoubleRequest) fsuipc.addContinualRequest(aircraft.getLongitude());
        onGround = fsuipc.addContinualRequest(aircraft.getOnGround());
        flapsHandle = fsuipc.addContinualRequest(new IntRequest(0x0BDC));
        gearHandle = fsuipc.addContinualRequest(new GearHelper().getControlLever());
        pistonEng1FF = (DoubleRequest) fsuipc.addContinualRequest(new DoubleRequest(0x0918));
        pistonEng2FF = (DoubleRequest) fsuipc.addContinualRequest(new DoubleRequest(0x09B0));
        pistonEng3FF = (DoubleRequest) fsuipc.addContinualRequest(new DoubleRequest(0x0A48));
        pistonEng4FF = (DoubleRequest) fsuipc.addContinualRequest(new DoubleRequest(0x0AE0));
        jetEng1FF = (DoubleRequest) fsuipc.addContinualRequest(new DoubleRequest(0x2020));
        jetEng2FF = (DoubleRequest) fsuipc.addContinualRequest(new DoubleRequest(0x2120));
        jetEng3FF = (DoubleRequest) fsuipc.addContinualRequest(new DoubleRequest(0x2220));
        jetEng4FF = (DoubleRequest) fsuipc.addContinualRequest(new DoubleRequest(0x2320));
    }

    public boolean isConnected() {
        return fsuipc.isConnected();
    }

    public int getRefreshRate() {
        return refreshRate;
    }

    public Optional<Airport> getAirport(String icao) {
        return Optional.ofNullable(airportService.get(icao));
    }

    public int getAltitude(Length unit) {
        double value = altitude.getValue();
        float converted = Length.FEET.convertTo(unit, value).floatValue();
        return Math.round(converted);
    }

    public int getHeading(boolean magnetic) {
        var value = magnetic ? headingMag.getValue().floatValue() : headingTrue.getValue();
        return Math.round(value);
    }

    public int getAirspeed(Speed unit) {
        double value = airspeed.getValue();
        float converted = Speed.KNOT.convertTo(unit, value).floatValue();
        return Math.round(converted);
    }

    public int getGroundSpeed(Speed unit) {
        double value = groundSpeed.getValue();
        float converted = Speed.KNOT.convertTo(unit, value).floatValue();
        return Math.round(converted);
    }

    public int getVerticalSpeed(Speed unit) {
        double value = verticalSpeed.getValue();
        float converted = Speed.FEET_PER_MIN.convertTo(unit, value).floatValue();
        return Math.round(converted);
    }

    public LocalTime getLocalTime() {
        return LocalTime.ofSecondOfDay(localTime.getValue());
    }

    public double getLatitude() {
        return aircraftLatitude.getValue();
    }

    public double getLongitude() {
        return aircraftLongitude.getValue();
    }

    public String getSimulator() {
        return fsuipc.getFSVersion();
    }

    public int getFramerate() {
        return Math.round(fps.getValue());
    }

    public String getFlightPhase() {
        var plan = FlightPlan.getInstance();

        if (plan == null || !isConnected()) {
            phase = null;
            return null;
        }

        if (onGround.getValue() == 1) {
            var depCode = plan.getDepartureCode();
            var arrCode = plan.getArrivalCode();
            var dep = airportService.get(depCode);
            var arr = airportService.get(arrCode);

            if (dep == null || arr == null || depCode.equals(arrCode)) {
                phase = "ON GROUND";
                return phase;
            }

            if (getEngineFuelFlow(1) < 1.0 && getEngineFuelFlow(2) < 1.0
                    && getEngineFuelFlow(3) < 1.0 && getEngineFuelFlow(4) < 1.0) {
                phase = "AT GATE";
                return phase;
            }

            var depLat = dep.getLatitude();
            var depLon = dep.getLongitude();
            var lat = getLatitude();
            var lon = getLongitude();
            var distance = Length.KILOMETER.getDistance(depLat, depLon, lat, lon);
            phase = (distance < 30.0) ? "DEPARTING" : "ARRIVED";
            return phase;
        }

        // 0 = Flaps up, 1 = Flaps full
        float flaps = (flapsHandle.getValue() / 16383f);
        int vs = getVerticalSpeed(Speed.FEET_PER_MIN);

        if (flaps > 0.2f && vs < 100) {
            phase = (gearHandle.getValue() == 16383) ? "LANDING" : "APPROACHING";
            return phase;
        }

        if (vs > 300) {
            phase = "CLIMBING";
        } else if (vs < 300) {
            phase = "DESCENDING";
        } else {
            phase = "EN ROUTE";
        }
        return phase;
    }

    public double getEngineFuelFlow(int engine) {
        double piston, jet;

        switch (engine) {
            case 1 -> {
                piston = pistonEng1FF.getValue();
                jet = jetEng1FF.getValue();
            }
            case 2 -> {
                piston = pistonEng2FF.getValue();
                jet = jetEng2FF.getValue();
            }
            case 3 -> {
                piston = pistonEng3FF.getValue();
                jet = jetEng3FF.getValue();
            }
            case 4 -> {
                piston = pistonEng4FF.getValue();
                jet = jetEng4FF.getValue();
            }
            default -> {
                piston = 0.0;
                jet = 0.0;
            }
        }

        return Math.max(piston, jet);
    }
}
