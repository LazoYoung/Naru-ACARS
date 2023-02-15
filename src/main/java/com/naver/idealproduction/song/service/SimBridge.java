package com.naver.idealproduction.song.service;

import com.mouseviator.fsuipc.FSUIPC;
import com.mouseviator.fsuipc.datarequest.IDataRequest;
import com.mouseviator.fsuipc.datarequest.primitives.DoubleRequest;
import com.mouseviator.fsuipc.datarequest.primitives.FloatRequest;
import com.mouseviator.fsuipc.datarequest.primitives.IntRequest;
import com.mouseviator.fsuipc.datarequest.primitives.StringRequest;
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
    private final FSUIPC fsuipc = FSUIPC.getInstance();
    private final IDataRequest<Float> fps;
    private final StringRequest aircraftType;
    private final StringRequest aircraftName;
    private final DoubleRequest altitude;
    private final IDataRequest<Double> groundAltitude;
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
    private final DoubleRequest eng1FF;
    private final DoubleRequest eng2FF;
    private final DoubleRequest eng3FF;
    private final DoubleRequest eng4FF;

    @SuppressWarnings("unchecked")
    @Autowired
    public SimBridge(AirportService airportService) {
        this.airportService = airportService;
        var aircraft = new AircraftHelper();
        var gps = new GPSHelper();
        var sim = new SimHelper();
        fps = fsuipc.addContinualRequest(sim.getFrameRate());
        aircraftType = (StringRequest) fsuipc.addContinualRequest(new StringRequest(0x0618, 16));
        aircraftName = (StringRequest) fsuipc.addContinualRequest(new StringRequest(0x3D00, 256));
        altitude = (DoubleRequest) fsuipc.addContinualRequest(aircraft.getAltitude(true));
        groundAltitude = fsuipc.addContinualRequest(sim.getGroundAltitude(true));
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
        eng1FF = (DoubleRequest) fsuipc.addContinualRequest(new Engine1Helper().getFuelFlow());
        eng2FF = (DoubleRequest) fsuipc.addContinualRequest(new Engine2Helper().getFuelFlow());
        eng3FF = (DoubleRequest) fsuipc.addContinualRequest(new Engine3Helper().getFuelFlow());
        eng4FF = (DoubleRequest) fsuipc.addContinualRequest(new Engine4Helper().getFuelFlow());
    }

    public boolean isConnected() {
        return fsuipc.isConnected();
    }

    public Optional<Airport> getAirport(String icao) {
        return Optional.ofNullable(airportService.get(icao));
    }

    public String getAircraftType() {
        return aircraftType.getValue();
    }

    public String getAircraftName() {
        return aircraftName.getValue();
    }

    public int getAltitude(Length unit) {
        double value = altitude.getValue();
        float converted = Length.FEET.convertTo(unit, value).floatValue();
        return Math.round(converted);
    }

//    public int getAGL(Length unit) {
//        var value = groundAltitude.getValue();
//        int converted = Math.round(Length.FEET.convertTo(unit, value).floatValue());
//        return getAltitude(unit) - converted;
//    }

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
            var dep = airportService.get(depCode);
            var arr = airportService.get(arrCode);
            if (dep == null || arr == null || depCode.equals(arrCode)) {
                return "ON GROUND";
            }

            boolean powered = (eng1FF.getValue() > 5.0) || (eng2FF.getValue() > 5.0) || (eng3FF.getValue() > 5.0) || (eng4FF.getValue() > 5.0);
            if (!powered) {
                // todo aircraft always !powered
                return "AT GATE";
            }

            var distance = Length.KILOMETER.getDistance(dep.getLatitude(), dep.getLongitude(), getLatitude(), getLongitude());
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

}
