package com.flylazo.naru_acars.gui.component;

import com.flylazo.naru_acars.domain.Airport;
import com.flylazo.naru_acars.gui.Window;
import com.flylazo.naru_acars.servlet.bridge.SimBridge;
import com.flylazo.naru_acars.servlet.service.SimTracker;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.Optional;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER;
import static javax.swing.SwingConstants.VERTICAL;

public class RouteInput extends JPanel {
    private final String NOT_FOUND = "Not found";
    private final Window window;
    private final SimTracker simTracker;
    private final TextInput depInput;
    private final TextInput arrInput;
    private final TextInput altInput;
    private final JTextArea rteInput;
    private final JTextArea rmkInput;
    private final JLabel depHint;
    private final JLabel arrHint;
    private final JLabel altHint;

    public RouteInput(Window window, Font labelFont) {
        var depLabel = window.bakeLabel("Departure airport", labelFont, Color.black);
        var arrLabel = window.bakeLabel("Arrival airport", labelFont, Color.black);
        var altLabel = window.bakeLabel("Alternate airport*", labelFont, Color.black);
        var depArrow = window.bakeLabel(">", labelFont, Color.black);
        var arrArrow = window.bakeLabel(">", labelFont, Color.black);
        var altArrow = window.bakeLabel(">", labelFont, Color.black);
        var rteLabel = window.bakeLabel("Route", labelFont, Color.black);
        var rmkLabel = window.bakeLabel("Remarks*", labelFont, Color.black);
        var validator = getValidator();
        this.window = window;
        this.simTracker = window.getServiceFactory().getBean(SimTracker.class);
        depInput = new TextInput("ICAO", 4, true);
        arrInput = new TextInput("ICAO", 4, true);
        altInput = new TextInput("ICAO", 4, true);
        rteInput = new JTextArea(3, 20);
        rmkInput = new JTextArea(3, 20);
        var rtePane = new JScrollPane(rteInput);
        var rmkPane = new JScrollPane(rmkInput);
        depHint = window.bakeLabel(NOT_FOUND, Color.yellow);
        arrHint = window.bakeLabel(NOT_FOUND, Color.yellow);
        altHint = window.bakeLabel(NOT_FOUND, Color.yellow);
        depInput.getDocument().addDocumentListener(validator);
        arrInput.getDocument().addDocumentListener(validator);
        altInput.getDocument().addDocumentListener(validator);
        rteInput.getDocument().addDocumentListener(validator);
        depHint.setOpaque(true);
        arrHint.setOpaque(true);
        altHint.setOpaque(true);
        depHint.setBorder(window.getMargin(depHint, 0, 10, 0, 10));
        arrHint.setBorder(window.getMargin(arrHint, 0, 10, 0, 10));
        altHint.setBorder(window.getMargin(arrHint, 0, 10, 0, 10));
        depHint.setBackground(Color.darkGray);
        arrHint.setBackground(Color.darkGray);
        altHint.setBackground(Color.darkGray);
        rteInput.setLineWrap(true);
        rmkInput.setLineWrap(true);
        rteInput.setWrapStyleWord(true);
        rmkInput.setWrapStyleWord(true);
        rtePane.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        rmkPane.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        rtePane.setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_NEVER);
        rmkPane.setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_NEVER);
        window.setDocumentFilter(rteInput.getDocument(), "[^A-Za-z0-9/ ]+", true);

        var layout = new GroupLayout(this);
        var hGroup = layout.createParallelGroup()
                .addComponent(depLabel)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(depInput, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                        .addGap(5)
                        .addComponent(depArrow)
                        .addGap(5)
                        .addComponent(depHint, PREFERRED_SIZE, PREFERRED_SIZE, Short.MAX_VALUE))
                .addComponent(arrLabel)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(arrInput, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                        .addGap(5)
                        .addComponent(arrArrow)
                        .addGap(5)
                        .addComponent(arrHint, PREFERRED_SIZE, PREFERRED_SIZE, Short.MAX_VALUE))
                .addComponent(altLabel)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(altInput, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                        .addGap(5)
                        .addComponent(altArrow)
                        .addGap(5)
                        .addComponent(altHint, PREFERRED_SIZE, PREFERRED_SIZE, Short.MAX_VALUE))
                .addComponent(rteLabel)
                .addComponent(rtePane, PREFERRED_SIZE, PREFERRED_SIZE, Short.MAX_VALUE)
                .addComponent(rmkLabel)
                .addComponent(rmkPane, PREFERRED_SIZE, PREFERRED_SIZE, Short.MAX_VALUE);
        var vGroup = layout.createSequentialGroup()
                .addComponent(depLabel)
                .addGroup(layout.createParallelGroup()
                        .addComponent(depInput, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                        .addComponent(depArrow)
                        .addComponent(depHint))
                .addGap(10)
                .addComponent(arrLabel)
                .addGroup(layout.createParallelGroup()
                        .addComponent(arrInput, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                        .addComponent(arrArrow)
                        .addComponent(arrHint))
                .addGap(10)
                .addComponent(altLabel)
                .addGroup(layout.createParallelGroup()
                        .addComponent(altInput, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                        .addComponent(altArrow)
                        .addComponent(altHint))
                .addGap(10)
                .addComponent(rteLabel)
                .addComponent(rtePane, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                .addGap(10)
                .addComponent(rmkLabel)
                .addComponent(rmkPane, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
        layout.setHorizontalGroup(hGroup);
        layout.setVerticalGroup(vGroup);
        layout.linkSize(VERTICAL, depInput, depHint);
        layout.linkSize(VERTICAL, arrInput, arrHint);
        layout.linkSize(VERTICAL, altInput, altHint);
        this.setLayout(layout);
    }

    public String getDeparture() {
        return depInput.getText();
    }

    public String getArrival() {
        return arrInput.getText();
    }

    public String getAlternate() {
        return altInput.getText();
    }

    public String getRoute() {
        return rteInput.getText();
    }

    public String getRemarks() {
        return rmkInput.getText();
    }

    public void setDeparture(String icao) {
        depInput.setText(icao);
    }

    public void setArrival(String icao) {
        arrInput.setText(icao);
    }

    public void setAlternate(String icao) {
        altInput.setText(icao);
    }

    public void setRoute(String route) {
        rteInput.setText(route);
    }

    public void setRemarks(String remarks) {
        rmkInput.setText(remarks);
    }

    public boolean validateForm() {
        boolean valid = true;
        var dep = Optional.ofNullable(depInput.getText()).orElse("");
        var arr = Optional.ofNullable(arrInput.getText()).orElse("");
        var alt = Optional.ofNullable(altInput.getText()).orElse("");
        SimBridge simBridge = simTracker.getBridge();
        Optional<String> departure = simBridge.getAirport(dep).map(Airport::getName);
        Optional<String> arrival = simBridge.getAirport(arr).map(Airport::getName);
        Optional<String> alternate = simBridge.getAirport(alt).map(Airport::getName);
        depHint.setText(departure.orElse(NOT_FOUND));
        arrHint.setText(arrival.orElse(NOT_FOUND));
        altHint.setText(alternate.orElse(NOT_FOUND));
        depHint.setPreferredSize(depHint.getSize());
        arrHint.setPreferredSize(arrHint.getSize());
        altHint.setPreferredSize(altHint.getSize());
        depHint.setForeground(departure.isEmpty() ? Color.yellow : Color.green);
        arrHint.setForeground(arrival.isEmpty() ? Color.yellow : Color.green);
        altHint.setForeground(alternate.isEmpty() ? Color.yellow : Color.green);

        if (departure.isEmpty()) {
            depInput.setBorder(window.getAmberBorder());
            valid = false;
        } else {
            depInput.setBorder(window.getDefaultBorder(depInput));
        }

        if (arrival.isEmpty()) {
            arrInput.setBorder(window.getAmberBorder());
            valid = false;
        } else {
            arrInput.setBorder(window.getDefaultBorder(arrInput));
        }

        if (getRoute().isBlank()) {
            rteInput.setBorder(window.getAmberBorder());
            valid = false;
        } else {
            rteInput.setBorder(window.getDefaultBorder(rteInput));
        }

        return valid;
    }

    private DocumentListener getValidator() {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                validateForm();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                validateForm();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                validateForm();
            }
        };
    }

}
