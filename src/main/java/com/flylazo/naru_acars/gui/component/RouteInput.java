package com.flylazo.naru_acars.gui.component;

import com.flylazo.naru_acars.domain.Airport;
import com.flylazo.naru_acars.gui.Window;
import com.flylazo.naru_acars.servlet.bridge.SimBridge;
import com.flylazo.naru_acars.servlet.service.SimTracker;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER;
import static javax.swing.SwingConstants.VERTICAL;

public class RouteInput extends JPanel {
    private final String NOT_FOUND = "Not found";
    private final SimTracker simTracker;
    private final List<Consumer<Boolean>> listeners = new ArrayList<>();
    private final TextInput depInput;
    private final TextInput arrInput;
    private final TextInput altInput;
    private final JScrollPane rtePane;
    private final JScrollPane rmkPane;
    private final JTextArea rteInput;
    private final JTextArea rmkInput;
    private final JLabel depHint;
    private final JLabel arrHint;
    private final JLabel altHint;

    public RouteInput(Window window, Font labelFont) {
        this.simTracker = window.getServiceFactory().getBean(SimTracker.class);
        var depLabel = window.bakeLabel("Departure airport", labelFont, Color.black);
        var arrLabel = window.bakeLabel("Arrival airport", labelFont, Color.black);
        var altLabel = window.bakeLabel("Alternate airport*", labelFont, Color.black);
        var depArrow = window.bakeLabel(">", labelFont, Color.black);
        var arrArrow = window.bakeLabel(">", labelFont, Color.black);
        var altArrow = window.bakeLabel(">", labelFont, Color.black);
        var rteLabel = window.bakeLabel("Route", labelFont, Color.black);
        var rmkLabel = window.bakeLabel("Remarks*", labelFont, Color.black);
        var airportValidator = getAirportValidator();
        depInput = new TextInput("ICAO", 4, true);
        arrInput = new TextInput("ICAO", 4, true);
        altInput = new TextInput("ICAO", 4, true);
        rteInput = new JTextArea(3, 20);
        rmkInput = new JTextArea(3, 20);
        rtePane = new JScrollPane(rteInput);
        rmkPane = new JScrollPane(rmkInput);
        depHint = window.bakeLabel(NOT_FOUND, Color.yellow);
        arrHint = window.bakeLabel(NOT_FOUND, Color.yellow);
        altHint = window.bakeLabel(NOT_FOUND, Color.yellow);
        depInput.getDocument().addDocumentListener(airportValidator);
        arrInput.getDocument().addDocumentListener(airportValidator);
        altInput.getDocument().addDocumentListener(airportValidator);
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

    public void addValidationListener(Consumer<Boolean> callback) {
        listeners.add(callback);
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

    public void validateAirport() {
        var dep = Optional.ofNullable(depInput.getText()).orElse("");
        var arr = Optional.ofNullable(arrInput.getText()).orElse("");
        var alt = Optional.ofNullable(altInput.getText()).orElse("");
        SimBridge simBridge = simTracker.getBridge();
        Optional<Airport> departure = simBridge.getAirport(dep);
        Optional<Airport> arrival = simBridge.getAirport(arr);
        Optional<Airport> alternate = simBridge.getAirport(alt);
        boolean valid = departure.isPresent() && arrival.isPresent();

        depHint.setText(departure.map(Airport::getName).orElse(NOT_FOUND));
        arrHint.setText(arrival.map(Airport::getName).orElse(NOT_FOUND));
        altHint.setText(alternate.map(Airport::getName).orElse(NOT_FOUND));
        depHint.setPreferredSize(depHint.getSize());
        arrHint.setPreferredSize(arrHint.getSize());
        altHint.setPreferredSize(altHint.getSize());
        depHint.setForeground(departure.isEmpty() ? Color.yellow : Color.green);
        arrHint.setForeground(arrival.isEmpty() ? Color.yellow : Color.green);
        altHint.setForeground(alternate.isEmpty() ? Color.yellow : Color.green);
        listeners.forEach(c -> c.accept(valid));
    }

    private DocumentListener getAirportValidator() {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                validateAirport();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                validateAirport();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                validateAirport();
            }
        };
    }
}
