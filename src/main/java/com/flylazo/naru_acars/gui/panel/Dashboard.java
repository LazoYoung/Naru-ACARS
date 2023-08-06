package com.flylazo.naru_acars.gui.panel;

import com.flylazo.naru_acars.gui.Window;
import com.flylazo.naru_acars.gui.component.ConnectHeader;
import com.flylazo.naru_acars.gui.component.ConnectStatus;
import com.flylazo.naru_acars.servlet.bridge.SimBridge;
import com.flylazo.naru_acars.servlet.service.SimTracker;

import javax.swing.*;
import java.awt.*;

public class Dashboard extends PanelBase {
    private record SimulatorDTO(String simulator, String bridge, int framerate, int refreshRate) {}
    private final SimTracker simTracker;
    private final ConnectHeader header;
    private final String NOT_AVAIL = "N/A";
    private final String TITLE_OFFLINE = "Simulator";
    private final JLabel simValue;
    private final JLabel frameValue;
    private final JLabel refreshValue;

    public Dashboard(Window window, int margin) {
        super(window);

        simTracker = window.getServiceFactory().getBean(SimTracker.class);
        var headerFont = new Font("Ubuntu Medium", Font.PLAIN, 16);
        var simLabel = new JLabel("Simulator: ");
        var frameLabel = new JLabel("Framerate: ");
        var refreshLabel = new JLabel("Refresh: ");
        this.header = new ConnectHeader(headerFont, TITLE_OFFLINE);
        this.simValue = new JLabel(NOT_AVAIL);
        this.frameValue = new JLabel(NOT_AVAIL);
        this.refreshValue = new JLabel(NOT_AVAIL);

        var layout = new GroupLayout(this);
        var simGlue = Box.createVerticalGlue();
        var simLabelGlue = Box.createHorizontalGlue();
        var frameLabelGlue = Box.createHorizontalGlue();
        var refreshLabelGlue = Box.createHorizontalGlue();
        var hGroup = layout.createSequentialGroup()
                .addContainerGap(margin, margin)
                .addGroup(layout.createParallelGroup()
                        .addComponent(header)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(simLabel)
                                .addComponent(simLabelGlue)
                                .addComponent(simValue))
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(frameLabel)
                                .addComponent(frameLabelGlue)
                                .addComponent(frameValue))
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(refreshLabel)
                                .addComponent(refreshLabelGlue)
                                .addComponent(refreshValue))
                        .addComponent(simGlue))
                .addContainerGap(margin, margin);
        var vGroup = layout.createSequentialGroup()
                .addContainerGap(margin, margin)
                .addComponent(header)
                .addGroup(layout.createParallelGroup()
                        .addComponent(simLabel)
                        .addComponent(simLabelGlue)
                        .addComponent(simValue))
                .addGroup(layout.createParallelGroup()
                        .addComponent(frameLabel)
                        .addComponent(frameLabelGlue)
                        .addComponent(frameValue))
                .addGroup(layout.createParallelGroup()
                        .addComponent(refreshLabel)
                        .addComponent(refreshLabelGlue)
                        .addComponent(refreshValue))
                .addComponent(simGlue)
                .addContainerGap(margin, margin);
        layout.setHorizontalGroup(hGroup);
        layout.setVerticalGroup(vGroup);
        this.simTracker.addUpdateListener(this::onUpdate);
        this.setLayout(layout);
        this.setBackground(Color.white);
        this.setPreferredSize(new Dimension(250, 0));
    }

    private void onUpdate(SimBridge data) {
        final boolean connected = data.isConnected();
        final var object = new SimulatorDTO(
                data.getSimulator(),
                this.simTracker.getBridge().getBridgeName(),
                data.getFPS(),
                this.simTracker.getRefreshRate()
        );

        SwingUtilities.invokeLater(() -> {
            if (connected) {
                onConnected(object);
            } else {
                onDisconnected();
            }
        });
    }

    private void onConnected(SimulatorDTO object) {
        header.setTitle(object.bridge);
        header.setStatus(ConnectStatus.ONLINE);
        simValue.setText(object.simulator);
        frameValue.setText(object.framerate + " fps");
        refreshValue.setText(object.refreshRate + " ms");
    }

    private void onDisconnected() {
        header.setTitle(TITLE_OFFLINE);
        header.setStatus(ConnectStatus.OFFLINE);
        simValue.setText(NOT_AVAIL);
        frameValue.setText(NOT_AVAIL);
        refreshValue.setText(NOT_AVAIL);
    }

}
