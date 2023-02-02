package com.naver.idealproduction.song;

import com.mouseviator.fsuipc.FSUIPC;
import com.mouseviator.fsuipc.FSUIPCWrapper;
import com.mouseviator.fsuipc.IFSUIPCListener;
import com.mouseviator.fsuipc.datarequest.IDataRequest;
import com.mouseviator.fsuipc.datarequest.primitives.DoubleRequest;
import com.mouseviator.fsuipc.datarequest.primitives.FloatRequest;
import com.mouseviator.fsuipc.helpers.aircraft.AircraftHelper;

import java.util.AbstractQueue;
import java.util.logging.Logger;

public class SimMonitor implements IFSUIPCListener {

    private static final Logger logger = Logger.getLogger(SimOverlayNG.class.getName());
    private final FSUIPC fsuipc;
    private final IDataRequest<String> aircraftType;
    private final DoubleRequest altitude;
    private final FloatRequest airspeed;

    @SuppressWarnings("unchecked")
    public SimMonitor() {
        var aircraftHelper = new AircraftHelper();
        fsuipc = FSUIPC.getInstance();
        aircraftType = fsuipc.addContinualRequest(aircraftHelper.getATCAircraftType());
        altitude = (DoubleRequest) fsuipc.addContinualRequest(aircraftHelper.getAltitude(true));
        airspeed = (FloatRequest) fsuipc.addContinualRequest(aircraftHelper.getIAS());
    }

    public void start(int ms) {
        Thread waiterThread = new Thread(() -> {
            boolean success = fsuipc.waitForConnection(FSUIPCWrapper.FSUIPCSimVersion.SIM_ANY, 5);

            if (!success) {
                fsuipc.connect(FSUIPCWrapper.FSUIPCSimVersion.SIM_ANY);
                logger.warning("Failed to open FSUIPC connection!");
            } else {
                logger.info("Waiting for FSUIPC connection...");
            }
        });

        fsuipc.addListener(this);
        waiterThread.start();
        fsuipc.processRequests(ms, true);
    }

    @Override
    public void onConnected() {
        logger.info("Connected to FSUIPC!");
        logger.info("Detected simulator: " + fsuipc.getFSVersion());
    }

    @Override
    public void onDisconnected() {
        logger.info("Disconnected from FSUIPC.");
    }

    @Override
    public void onProcess(AbstractQueue<IDataRequest> arRequests) {
        logger.info("-- Aircraft data report --");
        logger.info(String.format("Aircraft type: %s", aircraftType.getValue()));
        logger.info(String.format("Aircraft altitude: %.0f ft", altitude.getValue()));
        logger.info(String.format("Aircraft airspeed: %.0f knots", airspeed.getValue()));
    }

    @Override
    public void onFail(int lastResult) {
        String msg = FSUIPC.FSUIPC_ERROR_MESSAGES.get(FSUIPCWrapper.FSUIPCResult.get(lastResult));
        logger.warning("FSUIPC error: " + msg);
    }

    public void terminate() {
        fsuipc.disconnect();
    }
}
