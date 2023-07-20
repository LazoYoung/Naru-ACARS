package com.flylazo.naru_acars.gui.panel;

import com.flylazo.naru_acars.gui.Window;
import com.flylazo.naru_acars.servlet.bridge.SimBridge;
import com.flylazo.naru_acars.servlet.service.SimTracker;

import javax.swing.*;
import java.awt.*;

public class SimulatorBoard extends Board {
    private final SimTracker simTracker;
    private final String NOT_AVAIL = "N/A";
    private final int simulatorLabel;
    private final int fpsLabel;
    private final int refreshLabel;

    public SimulatorBoard(Window window) {
        super(window);

        simTracker = window.getServiceFactory().getBean(SimTracker.class);
        simulatorLabel = addLabel("Simulator", NOT_AVAIL);
        fpsLabel = addLabel("FPS", NOT_AVAIL);
        refreshLabel = addLabel("Refresh rate", NOT_AVAIL);

        simTracker.addUpdateListener(this::onUpdate);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.white);
        setPreferredSize(new Dimension(250, 0));
        updateContentPane(true);
    }

    @Override
    protected String getTitle() {
        return simTracker.getBridge().getBridgeName();
    }

    @Override
    protected String getOfflineText() {
        return "No simulator detected";
    }

    private void onUpdate(SimBridge data) {
        boolean oldState = isConnected();
        boolean newState = data.isConnected();

        setConnected(newState);

        if (newState) {
            var fps = data.getFPS();
            setValue(simulatorLabel, data.getSimulator());
            setValue(fpsLabel, (fps < 0 || fps > 500) ? NOT_AVAIL : String.valueOf(fps));
            setValue(refreshLabel, simTracker.getRefreshRate() + "ms");
        }
        SwingUtilities.invokeLater(() -> {
            boolean draw = (newState != oldState);
            updateContentPane(draw);
        });
    }

}
