package com.naver.idealproduction.song.gui.subpanel;

import com.naver.idealproduction.song.gui.Dashboard;
import com.naver.idealproduction.song.servlet.bridge.SimBridge;
import com.naver.idealproduction.song.servlet.service.SimTracker;

import javax.swing.*;
import java.awt.*;

public class SimMonitor extends SimplePanel {
    private final SimTracker simTracker;
    private final JLabel fsuipcLabel;
    private final JLabel simLabel;
    private final JLabel simValue;
    private final JLabel fpsLabel;
    private final JLabel refreshLabel;
    private final JLabel refreshValue;
    private final JLabel fpsValue;
    private final JLabel offlineLabel;
    private final String NOT_AVAIL = "N/A";
    private String simText;
    private String fpsText;
    private String refreshText;
    private boolean isConnected = false;

    public SimMonitor(Dashboard dashboard) {
        simTracker = dashboard.getSpringContext().getBean(SimTracker.class);
        simTracker.addProcessListener(this::onUpdate);

        var labelFont = new Font("Ubuntu Medium", Font.PLAIN, 18);
        var valueFont = new Font("Ubuntu Regular", Font.PLAIN, 16);
        var stateFont = new Font("Ubuntu Medium", Font.PLAIN, 30);
        fsuipcLabel = bakeLabel("FSUIPC", stateFont, Color.white);
        simLabel = bakeLabel("Simulator", labelFont, Color.gray);
        simValue = bakeLabel(NOT_AVAIL, valueFont, Color.black);
        fpsLabel = bakeLabel("FPS", labelFont, Color.gray);
        fpsValue = bakeLabel(NOT_AVAIL, valueFont, Color.black);
        refreshLabel = bakeLabel("Refresh rate", labelFont, Color.gray);
        refreshValue = bakeLabel(NOT_AVAIL, valueFont, Color.black);
        offlineLabel = bakeLabel("Offline", labelFont, Color.red);

        fsuipcLabel.setBackground(Color.red);
        fsuipcLabel.setOpaque(true);
        fsuipcLabel.setBorder(getMargin(fsuipcLabel, 10, 10, 10, 10));
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBackground(Color.white);
        this.setPreferredSize(new Dimension(250, 0));
        updateContentPane(true);
    }

    private void onUpdate(SimBridge data) {
        boolean _online = isConnected;
        isConnected = data.isConnected();

        if (isConnected) {
            var fps = data.getFPS();
            simText = data.getSimulator();
            fpsText = (fps < 0 || fps > 500) ? NOT_AVAIL : String.valueOf(fps);
            refreshText = simTracker.getRefreshRate() + "ms";
        }
        SwingUtilities.invokeLater(() -> {
            boolean draw = (isConnected != _online);
            updateContentPane(draw);
        });
    }

    private void updateContentPane(boolean draw) {
        if (isConnected) {
            simValue.setText(simText);
            fpsValue.setText(fpsText);
            refreshValue.setText(refreshText);
            fsuipcLabel.setBackground(Color.green);
        } else {
            fsuipcLabel.setBackground(Color.red);
        }

        if (draw) {
            removeAll();
            add(Box.createRigidArea(new Dimension(0, 20)));
            add(fsuipcLabel);

            if (isConnected) {
                add(Box.createVerticalGlue());
                add(simLabel);
                add(simValue);
                add(Box.createVerticalGlue());
                add(fpsLabel);
                add(fpsValue);
                add(Box.createVerticalGlue());
                add(refreshLabel);
                add(refreshValue);
                add(Box.createVerticalGlue());
            } else {
                add(Box.createVerticalStrut(120));
                add(offlineLabel);
            }

            revalidate();
            repaint();
        }
    }

}
