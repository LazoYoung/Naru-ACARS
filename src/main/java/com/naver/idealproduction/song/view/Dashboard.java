package com.naver.idealproduction.song.view;

import com.naver.idealproduction.song.SimData;
import com.naver.idealproduction.song.SimUpdateListener;
import com.naver.idealproduction.song.SimMonitor;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class Dashboard extends JSplitPane implements SimUpdateListener {

    private final SimMonitor simMonitor;
    private JPanel pane;
    private JLabel stateLabel;
    private JLabel simLabel;
    private JLabel simValue;
    private JLabel fpsLabel;
    private JLabel refreshLabel;
    private JLabel refreshValue;
    private JLabel fpsValue;
    private JLabel offlineLabel;
    private boolean online = false;
    private String simText;
    private String fpsText;
    private String refreshText;

    public Dashboard(SimMonitor simMonitor) {
        super(HORIZONTAL_SPLIT);
        this.simMonitor = simMonitor;
        var fsuipcPane = drawLeftPane();
        var dispatchPane = drawRightPane();

        setLeftComponent(fsuipcPane);
        setRightComponent(dispatchPane);
        setDividerSize(0);
        setResizeWeight(0.0);
        simMonitor.addUpdateListener(this);
    }

    @Override
    public void onUpdate(SimData data) {
        boolean _online = online;
        online = data.isConnected();

        if (online) {
            var fps = data.getFramerate();
            simText = data.getSimulator();
            fpsText = (fps < 0 || fps > 500) ? "N/A" : String.valueOf(fps);
            refreshText = simMonitor.getRefreshRate() + "ms";
        }
        SwingUtilities.invokeLater(() -> {
            boolean draw = (online != _online);
            updateContentPane(draw);
        });
    }

    private JPanel drawLeftPane() {
        stateLabel = new JLabel("FSUIPC", JLabel.CENTER);
        stateLabel.setOpaque(true);
        stateLabel.setForeground(Color.white);
        stateLabel.setBackground(Color.red);
        stateLabel.setFont(new Font("Monospaced", Font.BOLD, 30));
        Border border = stateLabel.getBorder();
        Border margin = new EmptyBorder(10, 10, 10, 10);
        stateLabel.setBorder(new CompoundBorder(border, margin));
        stateLabel.setAlignmentX(CENTER_ALIGNMENT);

        var labelFont = new Font("Monospaced", Font.BOLD, 18);
        var valueFont = new Font("Serif", Font.PLAIN, 16);
        simLabel = new JLabel("Simulator", JLabel.CENTER);
        simLabel.setForeground(Color.gray);
        simLabel.setFont(labelFont);
        simLabel.setAlignmentX(CENTER_ALIGNMENT);

        simValue = new JLabel("N/A", JLabel.CENTER);
        simValue.setFont(valueFont);
        simValue.setAlignmentX(CENTER_ALIGNMENT);

        fpsLabel = new JLabel("FPS", JLabel.CENTER);
        fpsLabel.setForeground(Color.gray);
        fpsLabel.setFont(labelFont);
        fpsLabel.setAlignmentX(CENTER_ALIGNMENT);

        fpsValue = new JLabel("N/A", JLabel.CENTER);
        fpsValue.setFont(valueFont);
        fpsValue.setAlignmentX(CENTER_ALIGNMENT);

        refreshLabel = new JLabel("Refresh rate", JLabel.CENTER);
        refreshLabel.setForeground(Color.gray);
        refreshLabel.setFont(labelFont);
        refreshLabel.setAlignmentX(CENTER_ALIGNMENT);

        refreshValue = new JLabel("N/A", JLabel.CENTER);
        refreshValue.setFont(valueFont);
        refreshValue.setAlignmentX(CENTER_ALIGNMENT);

        offlineLabel = new JLabel("Offline", JLabel.CENTER);
        offlineLabel.setFont(labelFont);
        offlineLabel.setForeground(Color.red);
        offlineLabel.setAlignmentX(CENTER_ALIGNMENT);

        pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        pane.setBackground(Color.white);
        pane.setMinimumSize(new Dimension(250, getHeight()));
        updateContentPane(true);
        return pane;
    }

    private JPanel drawRightPane() {
        var pane = new JPanel();
        pane.setBackground(Color.lightGray);
        return pane;
    }

    private void updateContentPane(boolean draw) {
        if (online) {
            simValue.setText(simText);
            fpsValue.setText(fpsText);
            refreshValue.setText(refreshText);
            stateLabel.setBackground(Color.green);
        } else {
            stateLabel.setBackground(Color.red);
        }

        if (draw) {
            pane.removeAll();
            pane.add(Box.createRigidArea(new Dimension(0, 20)));
            pane.add(stateLabel);

            if (online) {
                pane.add(Box.createVerticalGlue());
                pane.add(simLabel);
                pane.add(simValue);
                pane.add(Box.createVerticalGlue());
                pane.add(fpsLabel);
                pane.add(fpsValue);
                pane.add(Box.createVerticalGlue());
                pane.add(refreshLabel);
                pane.add(refreshValue);
                pane.add(Box.createVerticalGlue());
            } else {
                pane.add(Box.createRigidArea(new Dimension(0, 120)));
                pane.add(offlineLabel);
            }

            pane.revalidate();
            pane.repaint();
        }
    }
}
