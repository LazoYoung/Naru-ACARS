package com.flylazo.naru_acars.servlet.bridge;

import com.flylazo.naru_acars.domain.unit.Length;
import com.flylazo.naru_acars.domain.unit.Speed;
import com.flylazo.naru_acars.servlet.service.SimTracker;
import com.mouseviator.fsuipc.FSUIPC;
import com.mouseviator.fsuipc.FSUIPCWrapper;
import com.mouseviator.fsuipc.IFSUIPCListener;
import com.mouseviator.fsuipc.datarequest.IDataRequest;
import com.mouseviator.fsuipc.datarequest.primitives.DoubleRequest;
import com.mouseviator.fsuipc.datarequest.primitives.FloatRequest;
import com.mouseviator.fsuipc.datarequest.primitives.IntRequest;
import com.mouseviator.fsuipc.helpers.SimHelper;
import com.mouseviator.fsuipc.helpers.aircraft.AircraftHelper;
import com.mouseviator.fsuipc.helpers.aircraft.GearHelper;
import com.mouseviator.fsuipc.helpers.avionics.GPSHelper;
import com.flylazo.naru_acars.servlet.repository.AirportRepository;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalTime;
import java.util.AbstractQueue;
import java.util.logging.Level;

public class FSUIPC_Bridge extends SimBridge implements IFSUIPCListener {
    private final FSUIPC fsuipc = FSUIPC.getInstance();
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
    private final IntRequest door;

    @SuppressWarnings("unchecked")
    public FSUIPC_Bridge(SimTracker tracker, AirportRepository airportRepository) {
        super("FSUIPC", tracker, airportRepository);
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
        door = (IntRequest) fsuipc.addContinualRequest(new IntRequest(0x3367));
        pistonEng1FF = (DoubleRequest) fsuipc.addContinualRequest(new DoubleRequest(0x0918));
        pistonEng2FF = (DoubleRequest) fsuipc.addContinualRequest(new DoubleRequest(0x09B0));
        pistonEng3FF = (DoubleRequest) fsuipc.addContinualRequest(new DoubleRequest(0x0A48));
        pistonEng4FF = (DoubleRequest) fsuipc.addContinualRequest(new DoubleRequest(0x0AE0));
        jetEng1FF = (DoubleRequest) fsuipc.addContinualRequest(new DoubleRequest(0x2020));
        jetEng2FF = (DoubleRequest) fsuipc.addContinualRequest(new DoubleRequest(0x2120));
        jetEng3FF = (DoubleRequest) fsuipc.addContinualRequest(new DoubleRequest(0x2220));
        jetEng4FF = (DoubleRequest) fsuipc.addContinualRequest(new DoubleRequest(0x2320));
    }

    @Override
    public void hook() {
        boolean success = fsuipc.waitForConnection(FSUIPCWrapper.FSUIPCSimVersion.SIM_ANY, 5);

        if (success) {
            fsuipc.addListener(this);
            logger.info("Waiting for FSUIPC connection...");
        } else {
            logger.warning("Failed to open fsuipc connection!");
        }
    }

    @Override
    public void release() {
        if (isConnected()) {
            fsuipc.disconnect();
        } else {
            try {
                Method m = fsuipc.getClass().getDeclaredMethod("cancelWaitForConnectionTask");
                m.setAccessible(true);
                m.invoke(fsuipc);
            } catch (NoSuchMethodException e) {
                logger.log(Level.SEVERE, "Failed to reflect method.", e);
            } catch (InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isConnected() {
        return fsuipc.isConnected();
    }

    @Override
    public boolean isOnGround() {
        return onGround.getValue() == 1;
    }

    @Override
    public boolean isGearDown() {
        return gearHandle.getValue() == 16383;
    }

    @Override
    public boolean isDoorOpen() {
        return door.getValue() > 0;
    }

    @Override
    public float getFlapRatio() {
        return flapsHandle.getValue() / 16383f;
    }

    @Override
    public int getAltitude(Length unit) {
        double value = altitude.getValue();
        float converted = Length.FEET.convertTo(unit, value).floatValue();
        return Math.round(converted);
    }

    @Override
    public int getHeading(boolean magnetic) {
        var value = magnetic ? headingMag.getValue().floatValue() : headingTrue.getValue();
        return Math.round(value);
    }

    @Override
    public int getAirspeed(Speed unit) {
        double value = airspeed.getValue();
        float converted = Speed.KNOT.convertTo(unit, value).floatValue();
        return Math.round(converted);
    }

    @Override
    public int getGroundSpeed(Speed unit) {
        double value = groundSpeed.getValue();
        float converted = Speed.KNOT.convertTo(unit, value).floatValue();
        return Math.round(converted);
    }

    @Override
    public int getVerticalSpeed(Speed unit) {
        double value = verticalSpeed.getValue();
        float converted = Speed.FEET_PER_MIN.convertTo(unit, value).floatValue();
        return Math.round(converted);
    }

    @Override
    public LocalTime getLocalTime() {
        return LocalTime.ofSecondOfDay(localTime.getValue());
    }

    @Override
    public double getLatitude() {
        return aircraftLatitude.getValue();
    }

    @Override
    public double getLongitude() {
        return aircraftLongitude.getValue();
    }

    @Override
    public String getSimulator() {
        var version = FSUIPCWrapper.FSUIPCSimVersion.get(FSUIPCWrapper.getFSVersion());

        return switch (version) {
            case SIM_ANY -> "Unknown";
            case SIM_FS98 -> "FS 98";
            case SIM_FS2K -> "FS 2000";
            case SIM_CFS2 -> "Combat FS 2";
            case SIM_CFS1 -> "Combat FS 1";
            case SIM_FLY -> "Fly!";
            case SIM_FS2K2 -> "FS 2002";
            case SIM_FS2K4 -> "FS 2004";
            case SIM_FSX -> "FSX";
            case SIM_ESP -> "ESP";
            case SIM_P3D -> "Prepar3D";
            case SIM_FSX64 -> "FSX (64-bit)";
            case SIM_P3D64 -> "Prepar3D (64-bit)";
            case SIM_MSFS -> "MSFS";
        };
    }

    @Override
    public int getFPS() {
        return Math.round(fps.getValue());
    }

    @Override
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

    @Override
    public void onConnected() {
        logger.info("Connected to FSUIPC.");
        logger.info("Detected simulator: " + fsuipc.getFSVersion());
        fsuipc.processRequests(refreshRate, true);
        listener.onConnected(this);
    }

    @Override
    public void onDisconnected() {
        logger.info("Disconnected from FSUIPC.");
        listener.onDisconnected();
    }

    @Override
    public void onProcess(AbstractQueue<IDataRequest> arRequests) {
        listener.onProcess();
    }

    @Override
    public void onFail(int lastResult) {
        String msg = FSUIPC.FSUIPC_ERROR_MESSAGES.get(FSUIPCWrapper.FSUIPCResult.get(lastResult));
        listener.onFail(msg);
    }
}
