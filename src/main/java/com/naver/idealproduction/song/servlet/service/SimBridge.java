package com.naver.idealproduction.song.servlet.service;

import com.mouseviator.fsuipc.FSUIPC;
import com.mouseviator.fsuipc.datarequest.IDataRequest;
import com.mouseviator.fsuipc.datarequest.primitives.*;
import com.mouseviator.fsuipc.helpers.SimHelper;
import com.mouseviator.fsuipc.helpers.aircraft.*;
import com.mouseviator.fsuipc.helpers.avionics.GPSHelper;
import com.naver.idealproduction.song.domain.Airport;
import com.naver.idealproduction.song.domain.FlightPlan;
import com.naver.idealproduction.song.servlet.repository.AirportRepository;
import com.naver.idealproduction.song.domain.unit.Length;
import com.naver.idealproduction.song.domain.unit.Speed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Optional;

@Service
public class SimBridge {
    private final AirportRepository airportRepo;
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

    @SuppressWarnings("unchecked")
    @Autowired
    public SimBridge(AirportRepository airportRepo) {
        this.airportRepo = airportRepo;
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

    public Optional<Airport> getAirport(String icao) {
        return Optional.ofNullable(airportRepo.get(icao));
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
            return null;
        }

        if (onGround.getValue() == 1) {
            var depCode = plan.getDepartureCode();
            var arrCode = plan.getArrivalCode();
            var dep = airportRepo.get(depCode);
            var arr = airportRepo.get(arrCode);

            if (dep == null || arr == null || depCode.equals(arrCode)) {
                return "ON GROUND";
            }

            if (getEngineFuelFlow(1) < 1.0 && getEngineFuelFlow(2) < 1.0
                    && getEngineFuelFlow(3) < 1.0 && getEngineFuelFlow(4) < 1.0) {
                return "AT GATE";
            }

            var depLat = dep.getLatitude();
            var depLon = dep.getLongitude();
            var lat = getLatitude();
            var lon = getLongitude();
            var distance = Length.KILOMETER.getDistance(depLat, depLon, lat, lon);
            return (distance < 30.0) ? "DEPARTING" : "ARRIVED";
        }

        // 0 = Flaps up, 1 = Flaps full
        float flaps = (flapsHandle.getValue() / 16383f);
        int vs = getVerticalSpeed(Speed.FEET_PER_MIN);

        if (flaps > 0.2f && vs < 100) {
            return (gearHandle.getValue() == 16383) ? "LANDING" : "APPROACHING";
        }

        if (vs > 300) {
            return "CLIMBING";
        } else if (vs < 300) {
            return "DESCENDING";
        } else {
            return "EN ROUTE";
        }
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
