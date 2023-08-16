package com.flylazo.naru_acars.servlet.service;

import com.flylazo.naru_acars.NaruACARS;
import com.flylazo.naru_acars.domain.FlightPlan;
import com.flylazo.naru_acars.domain.Phase;
import com.flylazo.naru_acars.servlet.bridge.*;
import com.flylazo.naru_acars.servlet.repository.AirportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class SimTracker implements BridgeListener {

    private final Logger logger = NaruACARS.logger;
    private final List<Consumer<SimBridge>> listeners = new ArrayList<>();
    private final List<Consumer<Phase>> phaseListener = new ArrayList<>();
    private final List<SimBridge> bridgeList = new ArrayList<>();
    private final ACARS_Service service;
    private SimBridge activeBridge;
    private ScheduledFuture<?> phaseTask;

    @Autowired
    public SimTracker(ACARS_Service service, AirportRepository airportRepo) {
        var offlineBridge = new OfflineBridge(this, airportRepo);
        var fsuipcBridge = new FSUIPC_Bridge(this, airportRepo);
        var xpcBridge = new XPC_Bridge(this, airportRepo);
        this.activeBridge = offlineBridge;
        this.service = service;
        this.bridgeList.add(offlineBridge);
        this.bridgeList.add(fsuipcBridge);
        this.bridgeList.add(xpcBridge);
        FlightPlan.observeDispatch(plan -> this.resetFlightPhase());
    }

    /**
     * @return tracking frequency in milliseconds
     */
    public int getRefreshRate() {
        return 500;
    }

    public SimBridge getBridge() {
        return this.activeBridge;
    }

    public void hookBridges() {
        this.bridgeList.forEach(SimBridge::hook);
    }

    public void addUpdateListener(Consumer<SimBridge> listener) {
        this.listeners.add(listener);
    }

    public void addPhaseChangeListener(Consumer<Phase> listener) {
        this.phaseListener.add(listener);
    }

    @Override
    public void onConnected(SimBridge newBridge) {
        notifyUpdate();
        updateBridges(newBridge);
        startPhaseTask();
    }

    @Override
    public void onDisconnected() {
        notifyUpdate();
        hookBridges();
        stopPhaseTask();
    }

    @Override
    public void onProcess() {
        notifyUpdate();
    }

    private void notifyUpdate() {
        this.listeners.forEach(e -> e.accept(this.activeBridge));
    }

    private void notifyPhaseChange(Phase phase) {
        this.phaseListener.forEach(e -> e.accept(phase));
    }

    @Override
    public void onFail(String message) {
        this.logger.warning("Fsuipc error: " + message);
    }

    private void updateBridges(SimBridge newBridge) {
        this.activeBridge = newBridge;

        for (var bridge : this.bridgeList) {
            if (bridge != newBridge) {
                bridge.release();
            }
        }
    }

    private void startPhaseTask() {
        long delay = getRefreshRate();
        var executor = Executors.newSingleThreadScheduledExecutor();
        this.phaseTask = executor.scheduleWithFixedDelay(this::runPhaseTask, 0L, delay, TimeUnit.MILLISECONDS);
    }

    private void stopPhaseTask() {
        this.phaseTask.cancel(true);
    }

    private void runPhaseTask() {
        try {
            Phase prev = this.activeBridge.getFlightPhase();
            Phase phase = this.activeBridge.computeFlightPhase();

            if (prev != phase) {
                if (this.service.isConnected()) {
                    var context = this.service.getContext();
                    this.service.reportPhase(context, phase);
                }
                this.activeBridge.setFlightPhase(phase);
                this.notifyPhaseChange(phase);
            }
        } catch (Throwable t) {
            this.logger.log(Level.SEVERE, "Phase task has thrown an exception!", t);
        }
    }

    private void resetFlightPhase() {
        var phase = Phase.PREFLIGHT;
        this.activeBridge.setFlightPhase(phase);
        this.notifyPhaseChange(phase);
    }

}
