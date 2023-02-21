package com.naver.idealproduction.song.servlet.service;

import com.mouseviator.fsuipc.FSUIPC;
import com.mouseviator.fsuipc.FSUIPCWrapper;
import com.mouseviator.fsuipc.IFSUIPCListener;
import com.mouseviator.fsuipc.datarequest.IDataRequest;
import com.naver.idealproduction.song.SimOverlayNG;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

@Service
public class SimTracker implements IFSUIPCListener {

    private static final Logger logger = Logger.getLogger(SimOverlayNG.class.getName());
    private final FSUIPC fsuipc = FSUIPC.getInstance();
    private final List<Consumer<SimBridge>> listeners = new ArrayList<>();
    private final SimBridge bridge;
    private final int refreshRate = 500;

    @Autowired
    public SimTracker(SimBridge bridge) {
        this.bridge = bridge;
    }

    public int getRefreshRate() {
        return refreshRate;
    }

    public SimBridge getBridge() {
        return bridge;
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
        if (fsuipc.isConnected()) {
            fsuipc.disconnect();
        }
    }

    public void addProcessListener(Consumer<SimBridge> listener) {
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
        logger.info("Disconnected from fsuipc.");
        notifyListeners();
        terminate();
        start();
    }

    @Override
    public void onProcess(AbstractQueue<IDataRequest> arRequests) {
        notifyListeners();
    }

    private void notifyListeners() {
        listeners.forEach(e -> e.accept(bridge));
    }

    @Override
    public void onFail(int lastResult) {
        String msg = FSUIPC.FSUIPC_ERROR_MESSAGES.get(FSUIPCWrapper.FSUIPCResult.get(lastResult));
        logger.warning("Fsuipc error: " + msg);
    }

}
