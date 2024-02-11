package com.flylazo.naru_acars.gui.panel;

import com.flylazo.naru_acars.domain.Phase;
import com.flylazo.naru_acars.gui.Window;
import com.flylazo.naru_acars.gui.component.ConnectHeader;
import com.flylazo.naru_acars.gui.component.ConnectStatus;
import com.flylazo.naru_acars.gui.component.Header;
import com.flylazo.naru_acars.servlet.bridge.SimBridge;
import com.flylazo.naru_acars.servlet.service.ACARS_Service;
import com.flylazo.naru_acars.servlet.service.SimTracker;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;

public class Dashboard extends PanelBase {
    
    private record SimulatorDTO(String simulator, String bridge, int framerate, int refreshRate) {}
    private final SimTracker simTracker;
    private final ACARS_Service acarsService;
    private final ConnectHeader simHeader;
    private final ConnectHeader acarsHeader;
    private final String NOT_AVAIL = "N/A";
    private final String SIM_TITLE = "Simulator";
    private final JLabel simValue;
    private final JLabel frameValue;
    private final JLabel refreshValue;
    private final JLabel serverValue;
    private final JLabel serviceValue;
    private final JLabel phaseValue;

    public Dashboard(Window window, int margin) {
        super(window);

        var headerFont = new Font("Ubuntu Medium", Font.PLAIN, 16);
        var badgeFont = new Font("Ubuntu Medium", Font.PLAIN, 15);
        this.simTracker = window.getServiceFactory().getBean(SimTracker.class);
        this.acarsService = window.getServiceFactory().getBean(ACARS_Service.class);
        this.simHeader = new ConnectHeader(headerFont, badgeFont, SIM_TITLE);
        this.acarsHeader = new ConnectHeader(headerFont, badgeFont, "ACARS");
        this.simValue = new JLabel(NOT_AVAIL);
        this.frameValue = new JLabel(NOT_AVAIL);
        this.refreshValue = new JLabel(NOT_AVAIL);
        this.serverValue = new JLabel(NOT_AVAIL);
        this.serviceValue = new JLabel(NOT_AVAIL);
        this.phaseValue = new JLabel(NOT_AVAIL);

        var layout = new GroupLayout(this);
        var simLabelMap = new LinkedHashMap<String, JLabel>();
        var acarsLabelMap = new LinkedHashMap<String, JLabel>();
        simLabelMap.put("Simulator", this.simValue);
        simLabelMap.put("Frame-rate", this.frameValue);
        simLabelMap.put("Refresh-rate", this.refreshValue);
        acarsLabelMap.put("Airline", this.serverValue);
        acarsLabelMap.put("Service", this.serviceValue);
        acarsLabelMap.put("Phase", this.phaseValue);
        var pGroup = layout.createParallelGroup();
        var hGroup = layout.createSequentialGroup()
                .addContainerGap(margin, margin)
                .addGroup(pGroup);
        var vGroup = layout.createSequentialGroup()
                .addContainerGap(margin, margin);
        this.attachContents(layout, pGroup, vGroup, this.simHeader, simLabelMap);
        this.attachContents(layout, pGroup, vGroup, this.acarsHeader, acarsLabelMap);
        hGroup.addContainerGap(margin, margin);
        vGroup.addContainerGap(margin, margin);
        layout.setHorizontalGroup(hGroup);
        layout.setVerticalGroup(vGroup);

        this.acarsService.getListener().observeEstablish(this::onSocketEstablish);
        this.acarsService.getListener().observeClose(this::onSocketClose);
        this.simTracker.addPhaseChangeListener(this::onPhaseChange);
        this.simTracker.addUpdateListener(this::onSimulatorUpdate);
        this.setLayout(layout);
        this.setBackground(Color.white);
        this.setPreferredSize(new Dimension(250, 0));
    }

    private void onSimulatorUpdate(SimBridge data) {
        final boolean connected = data.isConnected();
        final var object = new SimulatorDTO(
                data.getSimulator(),
                this.simTracker.getBridge().getBridgeName(),
                data.getFPS(),
                this.simTracker.getRefreshRate()
        );

        SwingUtilities.invokeLater(() -> {
            if (connected) {
                this.simHeader.setTitle(object.bridge);
                this.simHeader.setStatus(ConnectStatus.ONLINE);
                this.simHeader.repaint();
                this.simValue.setText(object.simulator);
                this.frameValue.setText(object.framerate + " fps");
                this.refreshValue.setText(object.refreshRate + " ms");
            } else {
                this.simHeader.setTitle(SIM_TITLE);
                this.simHeader.setStatus(ConnectStatus.OFFLINE);
                this.simHeader.repaint();
                this.simValue.setText(NOT_AVAIL);
                this.frameValue.setText(NOT_AVAIL);
                this.refreshValue.setText(NOT_AVAIL);
            }
        });
    }

    private void onSocketEstablish() {
        final var server = this.acarsService.getServerName();
        final var service = this.acarsService.getServiceType();
        final var phase = this.simTracker.getBridge().getFlightPhase();

        SwingUtilities.invokeLater(() -> {
            this.acarsHeader.setStatus(ConnectStatus.ONLINE);
            this.acarsHeader.repaint();
            this.serverValue.setText(server);
            this.serviceValue.setText(service != null ? service.text : NOT_AVAIL);
            this.phaseValue.setText(phase.name());
        });
    }

    private void onSocketClose() {
        SwingUtilities.invokeLater(() -> {
            this.acarsHeader.setStatus(ConnectStatus.OFFLINE);
            this.acarsHeader.repaint();
            this.serverValue.setText(NOT_AVAIL);
            this.serviceValue.setText(NOT_AVAIL);
            this.phaseValue.setText(NOT_AVAIL);
        });
    }

    private void onPhaseChange(Phase phase) {
        this.phaseValue.setText(phase.name());
    }

    /**
     * Attach contents to horizontal and vertical {@link GroupLayout.Group}
     * @param layout the parent layout
     * @param pGroup parallel group where this content is attached
     * @param sGroup sequential group where this content is attached
     * @param header header of the contents
     * @param labelMap map of labels to display. String key designates the name of label
     */
    private void attachContents(
            GroupLayout layout,
            GroupLayout.ParallelGroup pGroup,
            GroupLayout.SequentialGroup sGroup,
            Header header,
            LinkedHashMap<String, JLabel> labelMap
    ) {
        pGroup.addComponent(header);
        sGroup.addComponent(header);
        var bottomGlue = Box.createVerticalGlue();

        labelMap.forEach((key, value) -> {
            var glue = Box.createHorizontalGlue();
            var label = new JLabel(key + ": ");
            pGroup.addGroup(layout.createSequentialGroup()
                    .addComponent(label)
                    .addComponent(glue)
                    .addComponent(value));
            sGroup.addGroup(layout.createParallelGroup()
                    .addComponent(label)
                    .addComponent(glue)
                    .addComponent(value));
        });
        pGroup.addComponent(bottomGlue);
        sGroup.addComponent(bottomGlue);
    }

}
