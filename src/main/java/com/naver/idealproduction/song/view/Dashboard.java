package com.naver.idealproduction.song.view;

import com.naver.idealproduction.song.SimData;
import com.naver.idealproduction.song.SimMonitor;
import com.naver.idealproduction.song.entity.Airport;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.Optional;

import static javax.swing.GroupLayout.*;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static javax.swing.LayoutStyle.ComponentPlacement.UNRELATED;

public class Dashboard extends JSplitPane {

    private final SimMonitor simMonitor;
    private final JPanel leftPane = new JPanel();
    private final JPanel rightPane = new JPanel();
    private final DocumentListener docListener;
    private final String notAvail = "N/A";
    private final String notFound = "Not found";
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
    private JTextField csInput;
    private JTextField acfInput;
    private TextFieldNG depInput;
    private TextFieldNG arrInput;
    private JLabel depHint;
    private JLabel arrHint;
    private JButton saveBtn;

    public Dashboard(SimMonitor simMonitor) {
        super(HORIZONTAL_SPLIT);
        this.simMonitor = simMonitor;
        docListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                validateInput();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                validateInput();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                validateInput();
            }
        };
        bakeLeftPane();
        bakeRightPane();
        setLeftComponent(leftPane);
        setRightComponent(rightPane);
        setDividerSize(0);
        setResizeWeight(0.0);
        simMonitor.addUpdateListener(this::onUpdate);
    }

    private void validateInput() {
        var dep = Optional.ofNullable(depInput.getText()).orElse("");
        var arr = Optional.ofNullable(arrInput.getText()).orElse("");
        SimData data = simMonitor.getData();
        Optional<Airport> departure = data.getAirport(dep);
        Optional<Airport> arrival = data.getAirport(arr);

        depHint.setText(departure.isEmpty() ? notFound : departure.get().getName());
        arrHint.setText(arrival.isEmpty() ? notFound : arrival.get().getName());
        depHint.setForeground(departure.isEmpty() ? Color.red : Color.white);
        arrHint.setForeground(arrival.isEmpty() ? Color.red : Color.white);
        saveBtn.setEnabled(departure.isPresent() && arrival.isPresent());
    }

    private void onUpdate(SimData data) {
        boolean _online = online;
        online = data.isConnected();

        if (online) {
            var fps = data.getFramerate();
            simText = data.getSimulator();
            fpsText = (fps < 0 || fps > 500) ? notAvail : String.valueOf(fps);
            refreshText = simMonitor.getRefreshRate() + "ms";
        }
        SwingUtilities.invokeLater(() -> {
            boolean draw = (online != _online);
            updateContentPane(draw);
        });
    }

    private void bakeLeftPane() {
        var labelFont = new Font("Monospaced", Font.BOLD, 18);
        var valueFont = new Font("Serif", Font.PLAIN, 16);
        var stateFont = new Font("Monospaced", Font.BOLD, 30);
        stateLabel = bakeLabel("FSUIPC", stateFont, Color.white);
        stateLabel.setBackground(Color.red);
        stateLabel.setOpaque(true);
        stateLabel.setBorder(getMargin(stateLabel, 10, 10, 10, 10));
        simLabel = bakeLabel("Simulator", labelFont, Color.gray);
        simValue = bakeLabel(notAvail, valueFont, Color.black);
        fpsLabel = bakeLabel("FPS", labelFont, Color.gray);
        fpsValue = bakeLabel(notAvail, valueFont, Color.black);
        refreshLabel = bakeLabel("Refresh rate", labelFont, Color.gray);
        refreshValue = bakeLabel(notAvail, valueFont, Color.black);
        offlineLabel = bakeLabel("Offline", labelFont, Color.red);
        leftPane.setLayout(new BoxLayout(leftPane, BoxLayout.Y_AXIS));
        leftPane.setBackground(Color.white);
        leftPane.setPreferredSize(new Dimension(250, 0));
        updateContentPane(true);
    }

    private CompoundBorder getMargin(JComponent comp, int top, int left, int bottom, int right) {
        Border border = comp.getBorder();
        Border margin = new EmptyBorder(top, left, bottom, right);
        return new CompoundBorder(border, margin);
    }

    private void bakeRightPane() {
        var formPane = new JPanel();
        var formLayout = new GroupLayout(formPane);
        var labelFont = new Font("Monospaced", Font.BOLD, 16);
        var hintFont = new Font("Monospaced", Font.BOLD, 14);
        var csLabel = bakeLabel("Callsign", labelFont, Color.black);
        var acfLabel = bakeLabel("Aircraft", labelFont, Color.black);
        var depLabel = bakeLabel("Departure", labelFont, Color.black);
        var arrLabel = bakeLabel("Arrival", labelFont, Color.black);
        csInput = new TextFieldNG(8, true);
        acfInput = new TextFieldNG(8, true);
        depInput = new TextFieldNG("ICAO", 8, true);
        arrInput = new TextFieldNG("ICAO", 8, true);
        depHint = bakeLabel(notFound, hintFont, Color.red);
        arrHint = bakeLabel(notFound, hintFont, Color.red);
        var hGroup = formLayout.createSequentialGroup()
                .addContainerGap(50, 50)
                .addGroup(formLayout.createParallelGroup(LEADING, false)
                        .addComponent(csLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                        .addComponent(csInput, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                        .addComponent(depLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                        .addComponent(depInput, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                        .addComponent(arrLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                        .addComponent(arrInput, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE))
                .addGap(40)
                .addGroup(formLayout.createParallelGroup(LEADING, false)
                        .addComponent(acfLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                        .addComponent(acfInput, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                        .addComponent(depHint, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(arrHint, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(UNRELATED, PREFERRED_SIZE, Short.MAX_VALUE);
        var vGroup = formLayout.createSequentialGroup()
                .addContainerGap(40, 40)
                .addGroup(formLayout.createParallelGroup(LEADING)
                        .addComponent(csLabel)
                        .addComponent(acfLabel))
                .addGroup(formLayout.createParallelGroup(LEADING, false)
                        .addComponent(csInput)
                        .addComponent(acfInput))
                .addGap(10)
                .addComponent(depLabel)
                .addGroup(formLayout.createParallelGroup(LEADING, false)
                        .addComponent(depInput)
                        .addComponent(depHint))
                .addGap(10)
                .addComponent(arrLabel)
                .addGroup(formLayout.createParallelGroup(LEADING, false)
                        .addComponent(arrInput)
                        .addComponent(arrHint));

        var actionPane = new JPanel();
        var simbriefBtn = new JButton("SIMBRIEF");
        saveBtn = new JButton("SAVE");

        depInput.getDocument().addDocumentListener(docListener);
        arrInput.getDocument().addDocumentListener(docListener);
        depHint.setOpaque(true);
        arrHint.setOpaque(true);
        depHint.setBorder(getMargin(depHint, 0, 10, 0, 10));
        arrHint.setBorder(getMargin(arrHint, 0, 10, 0, 10));
        depHint.setBackground(Color.lightGray);
        arrHint.setBackground(Color.lightGray);
        saveBtn.setEnabled(false);
        actionPane.setLayout(new BoxLayout(actionPane, BoxLayout.X_AXIS));
        actionPane.add(Box.createHorizontalGlue());
        actionPane.add(simbriefBtn);
        actionPane.add(getRigidGap(10, 0));
        actionPane.add(saveBtn);
        actionPane.add(getRigidGap(50, 0));
        formLayout.setHorizontalGroup(hGroup);
        formLayout.setVerticalGroup(vGroup);
        formPane.setLayout(formLayout);
        rightPane.setLayout(new BoxLayout(rightPane, BoxLayout.Y_AXIS));
        rightPane.add(formPane);
        rightPane.add(Box.createVerticalGlue());
        rightPane.add(actionPane);
        rightPane.add(getRigidGap(0, 50));
    }

    private JLabel bakeLabel(String text, Font font, Color color) {
        var label = new JLabel(text, JLabel.CENTER);
        label.setFont(font);
        label.setAlignmentX(CENTER_ALIGNMENT);
        label.setForeground(color);
        return label;
    }

    private Component getRigidGap(int width, int height) {
        return Box.createRigidArea(new Dimension(width, height));
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
            leftPane.removeAll();
            leftPane.add(Box.createRigidArea(new Dimension(0, 20)));
            leftPane.add(stateLabel);

            if (online) {
                leftPane.add(Box.createVerticalGlue());
                leftPane.add(simLabel);
                leftPane.add(simValue);
                leftPane.add(Box.createVerticalGlue());
                leftPane.add(fpsLabel);
                leftPane.add(fpsValue);
                leftPane.add(Box.createVerticalGlue());
                leftPane.add(refreshLabel);
                leftPane.add(refreshValue);
                leftPane.add(Box.createVerticalGlue());
            } else {
                leftPane.add(getRigidGap(0, 120));
                leftPane.add(offlineLabel);
            }

            leftPane.revalidate();
            leftPane.repaint();
        }
    }
}
