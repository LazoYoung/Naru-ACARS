package com.flylazo.naru_acars.gui.panel;

import com.flylazo.naru_acars.gui.Window;
import com.flylazo.naru_acars.servlet.service.ACARS_Service;
import com.flylazo.naru_acars.servlet.service.SimTracker;

import javax.swing.*;
import java.awt.*;

public class ACARS_Board extends Board {
    private final ACARS_Service dataLink;
    private final SimTracker simTracker;
    private final String ONLINE = "Connected";
    private final String OFFLINE = "Disconnected";
    private final String NOT_AVAIL = "N/A";
    private final int serverLabel;
    private final int phaseLabel;

    public ACARS_Board(Window window) {
        super(window);

        dataLink = window.getServiceFactory().getBean(ACARS_Service.class);
        simTracker = window.getServiceFactory().getBean(SimTracker.class);
        serverLabel = addLabel("VA Server", OFFLINE);
        phaseLabel = addLabel("Phase of Flight", NOT_AVAIL);

        dataLink.addUpdateListener(this::onUpdate);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.white);
        setPreferredSize(new Dimension(250, 0));
        updateContentPane(true);
    }

    @Override
    protected String getTitle() {
        return "ACARS";
    }

    @Override
    protected String getOfflineText() {
        return "Datalink offline";
    }

    private void onUpdate() {
        setConnected(dataLink.isConnected());
        setValue(serverLabel, dataLink.getServer());
        setValue(phaseLabel, simTracker.getBridge().getFlightPhase());
    }
}
