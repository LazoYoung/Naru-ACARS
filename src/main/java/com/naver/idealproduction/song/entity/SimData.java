package com.naver.idealproduction.song.entity;

import com.mouseviator.fsuipc.FSUIPC;
import com.mouseviator.fsuipc.datarequest.IDataRequest;
import com.mouseviator.fsuipc.datarequest.primitives.DoubleRequest;
import com.mouseviator.fsuipc.datarequest.primitives.FloatRequest;
import com.mouseviator.fsuipc.datarequest.primitives.IntRequest;
import com.mouseviator.fsuipc.datarequest.primitives.StringRequest;
import com.mouseviator.fsuipc.helpers.SimHelper;
import com.mouseviator.fsuipc.helpers.aircraft.AircraftHelper;
import com.mouseviator.fsuipc.helpers.avionics.GPSHelper;
import com.naver.idealproduction.song.entity.repository.AirportRepository;
import com.naver.idealproduction.song.entity.unit.Length;
import com.naver.idealproduction.song.entity.unit.Speed;

import java.time.LocalTime;
import java.util.Optional;

public class SimData {

    private final AirportRepository airportRepo = new AirportRepository();
    private final FSUIPC fsuipc = FSUIPC.getInstance();
    private final IDataRequest<Float> fps;
    private final StringRequest aircraftType;
    private final StringRequest aircraftName;
    private final DoubleRequest altitude;
    private final FloatRequest headingTrue;
    private final DoubleRequest headingMag;
    private final FloatRequest airspeed;
    private final IDataRequest<Double> groundSpeed;
    private final FloatRequest verticalSpeed;
    private final IntRequest localTime;
    private final DoubleRequest aircraftLatitude;
    private final DoubleRequest aircraftLongitude;
// These are not available in MSFS
//    private final IDataRequest<String> destination;
//    private final IDataRequest<Double> routeDistanceMeters;
//    private final IDataRequest<Integer> ete;
//    private final IDataRequest<Integer> eta;

    @SuppressWarnings("unchecked")
    public SimData() {
        var aircraft = new AircraftHelper();
        var gps = new GPSHelper();
        var sim = new SimHelper();
        fps = fsuipc.addContinualRequest(sim.getFrameRate());
        aircraftLatitude = (DoubleRequest) fsuipc.addContinualRequest(aircraft.getLatitude());
        aircraftLongitude = (DoubleRequest) fsuipc.addContinualRequest(aircraft.getLongitude());
        aircraftType = (StringRequest) fsuipc.addContinualRequest(new StringRequest(0x0618, 16));
        aircraftName = (StringRequest) fsuipc.addContinualRequest(new StringRequest(0x3D00, 256));
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
    }

    public boolean isConnected() {
        return fsuipc.isConnected();
    }

    public Optional<Airport> getAirport(String icao) {
        return Optional.ofNullable(airportRepo.get(icao));
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

//    public int getRouteDistance(Length unit) {
//        double value = routeDistanceMeters.getValue();
//        float converted = Length.METER.convertTo(unit, value).floatValue();
//        return Math.round(converted);
//    }
//
//    public int getETE() {
//        return ete.getValue();
//    }
//
//    public int getETA() {
//        return eta.getValue();
//    }

}
