package com.naver.idealproduction.song;

import com.mouseviator.fsuipc.FSUIPC;
import com.mouseviator.fsuipc.FSUIPCWrapper;
import com.mouseviator.fsuipc.IFSUIPCListener;
import com.mouseviator.fsuipc.datarequest.IDataRequest;
import com.naver.idealproduction.song.entity.unit.Length;
import com.naver.idealproduction.song.entity.unit.Speed;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class SimTracker implements IFSUIPCListener {

    private static final Logger logger = Logger.getLogger(SimOverlayNG.class.getName());
    private final FSUIPC fsuipc = FSUIPC.getInstance();
    private final List<Consumer<SimBridge>> listeners = new ArrayList<>();

    private final SimBridge data;
    private final int refreshRate;

    public SimTracker(int refreshRate) {
        data = new SimBridge();
        this.refreshRate = refreshRate;
    }

    public int getRefreshRate() {
        return refreshRate;
    }

    public SimBridge getData() {
        return data;
    }

    public void start() {
        Thread waiterThread = new Thread(() -> {
            boolean success = fsuipc.waitForConnection(FSUIPCWrapper.FSUIPCSimVersion.SIM_ANY, 5);

            if (!success) {
                fsuipc.connect(FSUIPCWrapper.FSUIPCSimVersion.SIM_ANY);
                logger.warning("Failed to open fsuipc connection!");
            } else {
                logger.info("Waiting for fsuipc connection...");
            }
        });

        fsuipc.addListener(this);
        waiterThread.start();
    }

    public void terminate() {
        fsuipc.disconnect();
        logger.info("Disconnected from fsuipc.");
    }

    public void addUpdateListener(Consumer<SimBridge> listener) {
        listeners.add(listener);
    }

    @Override
    public void onConnected() {
        logger.info("Connected to fsuipc!");
        logger.info("Detected simulator: " + fsuipc.getFSVersion());
        fsuipc.processRequests(refreshRate, true);
        notifyListeners();
    }

    @Override
    public void onDisconnected() {
        notifyListeners();
        terminate();
        start();
    }

    @Override
    public void onProcess(AbstractQueue<IDataRequest> arRequests) {
        try {
            notifyListeners();

            log("-- Simulator data --");
            log("Aircraft type: %s", data.getAircraftType());
            log("Aircraft name: %s", data.getAircraftName());
            log("Aircraft altitude: %d ft", data.getAltitude(Length.FEET));
            log("Aircraft true heading: %d", data.getHeading(false));
            log("Aircraft mag. heading: %d", data.getHeading(true));
            log("Aircraft airspeed: %d knots", data.getAirspeed(Speed.KNOT));
            log("Aircraft ground speed: %d knots", data.getGroundSpeed(Speed.KNOT));
            log("Aircraft latitude: %.9f", data.getLatitude());
            log("Aircraft longitude: %.9f", data.getLongitude());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void notifyListeners() {
        listeners.forEach(e -> e.accept(data));
    }

    @Override
    public void onFail(int lastResult) {
        String msg = FSUIPC.FSUIPC_ERROR_MESSAGES.get(FSUIPCWrapper.FSUIPCResult.get(lastResult));
        logger.warning("Fsuipc error: " + msg);
    }

    private void log(String format, Object... args) {
        logger.info(String.format(format, args));
    }
}
