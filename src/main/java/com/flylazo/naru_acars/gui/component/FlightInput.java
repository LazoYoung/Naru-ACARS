package com.flylazo.naru_acars.gui.component;

import com.flylazo.naru_acars.gui.Window;

import javax.swing.*;
import java.awt.*;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.LayoutStyle.ComponentPlacement.RELATED;

public class FlightInput extends JPanel {
    private final JTextField csInput;
    private final JTextField acfInput;
    private final JTextField cruiseInput;
    private final JTextField fltTimeInput;

    public FlightInput(Window window, Font labelFont) {
        var lightFont = new Font("Ubuntu Regular", Font.PLAIN, 15);
        var csLabel = window.bakeLabel("Callsign", labelFont, Color.black);
        var acfLabel = window.bakeLabel("Aircraft", labelFont, Color.black);
        var cruiseLabel = window.bakeLabel("Cruise altitude", labelFont, Color.black);
        var feetLabel = window.bakeLabel("ft", lightFont, Color.black);
        var fltTimeLabel = window.bakeLabel("Flight time", labelFont, Color.black);
        csInput = new TextInput(7, true);
        acfInput = new TextInput(7, true);
        cruiseInput = new TextInput(6, false);
        fltTimeInput = new TextInput("hh:mm", 7, false);

        var layout = new GroupLayout(this);
        var hGroup = layout.createParallelGroup()
                .addComponent(csLabel)
                .addComponent(csInput, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                .addComponent(acfLabel)
                .addComponent(acfInput, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                .addComponent(cruiseLabel)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(cruiseInput, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                        .addComponent(feetLabel))
                .addComponent(fltTimeLabel)
                .addComponent(fltTimeInput, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
        var vGroup = layout.createSequentialGroup()
                .addComponent(csLabel)
                .addComponent(csInput, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                .addGap(10)
                .addComponent(acfLabel)
                .addComponent(acfInput, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                .addGap(10)
                .addComponent(cruiseLabel)
                .addGroup(layout.createParallelGroup()
                        .addComponent(cruiseInput, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                        .addComponent(feetLabel))
                .addGap(10)
                .addComponent(fltTimeLabel)
                .addComponent(fltTimeInput, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);

        layout.setHorizontalGroup(hGroup);
        layout.setVerticalGroup(vGroup);
        this.setLayout(layout);
    }

    public String getCallsign() {
        return csInput.getText();
    }

    public String getAircraft() {
        return acfInput.getText();
    }

    public String getCruiseAltitude() {
        return cruiseInput.getText();
    }

    public String getFlightTime() {
        return fltTimeInput.getText();
    }

    public void setCallsign(String text) {
        csInput.setText(text);
    }

    public void setAircraft(String text) {
        acfInput.setText(text);
    }

    public void setCruiseAltitude(String text) {
        cruiseInput.setText(text);
    }

    public void setFlightTime(String text) {
        fltTimeInput.setText(text);
    }

}
