package com.naver.idealproduction.song.servlet.service;

import com.naver.idealproduction.song.SimOverlayNG;
import com.naver.idealproduction.song.servlet.bridge.*;
import com.naver.idealproduction.song.servlet.repository.AirportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

@Service
public class SimTracker implements BridgeListener {

    private static final Logger logger = Logger.getLogger(SimOverlayNG.class.getName());
    private final List<Consumer<SimBridge>> listeners = new ArrayList<>();
    private final List<SimBridge> bridgeList = new ArrayList<>();
    private SimBridge activeBridge;

    @Autowired
    public SimTracker(AirportRepository airportRepo) {
        var offlineBridge = new OfflineBridge(this, airportRepo);
        var fsuipcBridge = new FSUIPCBridge(this, airportRepo);
        var xpcBridge = new XPCBridge(this, airportRepo);

        this.activeBridge = offlineBridge;
        bridgeList.add(offlineBridge);
        bridgeList.add(fsuipcBridge);
        bridgeList.add(xpcBridge);
    }

    public int getRefreshRate() {
        return 500;
    }

    public SimBridge getBridge() {
        return activeBridge;
    }

    public void hookBridges() {
        bridgeList.forEach(SimBridge::hook);
    }

    public void addUpdateListener(Consumer<SimBridge> listener) {
        listeners.add(listener);
    }

    @Override
    public void onConnected(SimBridge newBridge) {
        notifyProcessListeners();
        this.activeBridge = newBridge;

        for (var bridge : bridgeList) {
            if (bridge != newBridge) {
                bridge.release();
            }
        }
    }

    @Override
    public void onDisconnected() {
        notifyProcessListeners();
        hookBridges();
    }

    @Override
    public void onProcess() {
        notifyProcessListeners();
    }

    private void notifyProcessListeners() {
        listeners.forEach(e -> e.accept(activeBridge));
    }

    @Override
    public void onFail(String message) {
        logger.warning("Fsuipc error: " + message);
    }

}
