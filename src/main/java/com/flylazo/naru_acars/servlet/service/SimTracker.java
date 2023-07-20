package com.flylazo.naru_acars.servlet.service;

import com.flylazo.naru_acars.NaruACARS;
import com.flylazo.naru_acars.servlet.bridge.*;
import com.flylazo.naru_acars.servlet.repository.AirportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

@Service
public class SimTracker implements BridgeListener {

    private static final Logger logger = Logger.getLogger(NaruACARS.class.getName());
    private final List<Consumer<SimBridge>> listeners = new ArrayList<>();
    private final List<SimBridge> bridgeList = new ArrayList<>();
    private SimBridge activeBridge;

    @Autowired
    public SimTracker(AirportRepository airportRepo) {
        var offlineBridge = new OfflineBridge(this, airportRepo);
        var fsuipcBridge = new FSUIPC_Bridge(this, airportRepo);
        var xpcBridge = new XPC_Bridge(this, airportRepo);

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
        notifyUpdate();
        this.activeBridge = newBridge;

        for (var bridge : bridgeList) {
            if (bridge != newBridge) {
                bridge.release();
            }
        }
    }

    @Override
    public void onDisconnected() {
        notifyUpdate();
        hookBridges();
    }

    @Override
    public void onProcess() {
        notifyUpdate();
    }

    private void notifyUpdate() {
        listeners.forEach(e -> e.accept(activeBridge));
    }

    @Override
    public void onFail(String message) {
        logger.warning("Fsuipc error: " + message);
    }

}
