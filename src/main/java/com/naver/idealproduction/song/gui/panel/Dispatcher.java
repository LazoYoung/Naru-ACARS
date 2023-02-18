package com.naver.idealproduction.song.gui.panel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naver.idealproduction.song.SimOverlayNG;
import com.naver.idealproduction.song.domain.Airport;
import com.naver.idealproduction.song.domain.FlightPlan;
import com.naver.idealproduction.song.domain.Properties;
import com.naver.idealproduction.song.gui.Dashboard;
import com.naver.idealproduction.song.gui.component.TextInput;
import com.naver.idealproduction.song.servlet.repository.AircraftRepository;
import com.naver.idealproduction.song.servlet.service.SimBridge;
import com.naver.idealproduction.song.servlet.service.SimDataService;
import com.naver.idealproduction.song.servlet.service.SimTracker;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.swing.GroupLayout.Alignment.BASELINE;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.LayoutStyle.ComponentPlacement.RELATED;
import static javax.swing.LayoutStyle.ComponentPlacement.UNRELATED;

public class Dispatcher extends SimplePanel {
    private final Logger logger = Logger.getLogger(SimOverlayNG.class.getName());
    private final String NOT_FOUND = "Not found";
    private final String FORM_EMPTY = "Please fill out the form";
    private final SimTracker simTracker;
    private final SimDataService simDataService;
    private final AircraftRepository aircraftRepo;
    private final JTextField csInput;
    private final JTextField acfInput;
    private final TextInput depInput;
    private final TextInput arrInput;
    private final JLabel depHint;
    private final JLabel arrHint;
    private final JLabel actionLabel;
    private final JButton simbriefBtn;
    private final JButton submitBtn;
    private FlightPlan plan = null;

    public Dispatcher(Dashboard dashboard) {
        this.simTracker = dashboard.getSpringContext().getBean(SimTracker.class);
        this.aircraftRepo = dashboard.getSpringContext().getBean(AircraftRepository.class);
        this.simDataService = dashboard.getSpringContext().getBean(SimDataService.class);
        var formPane = new JPanel();
        var formLayout = new GroupLayout(formPane);
        var boldFont = new Font("Ubuntu Medium", Font.PLAIN, 15);
        var csLabel = bakeLabel("Callsign", boldFont, Color.black);
        var acfLabel = bakeLabel("Aircraft", boldFont, Color.black);
        var depLabel = bakeLabel("Departure", boldFont, Color.black);
        var arrLabel = bakeLabel("Arrival", boldFont, Color.black);
        csInput = new TextInput(6, true);
        acfInput = new TextInput(6, true);
        depInput = new TextInput("ICAO", 6, true);
        arrInput = new TextInput("ICAO", 6, true);
        depHint = bakeLabel(NOT_FOUND, Color.yellow);
        arrHint = bakeLabel(NOT_FOUND, Color.yellow);
        DocumentListener docListener = new DocumentListener() {
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
        var hGroup = formLayout.createSequentialGroup()
                .addContainerGap(20, 20)
                .addGroup(formLayout.createParallelGroup(LEADING, false)
                        .addComponent(csLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                        .addComponent(csInput, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                        .addComponent(acfLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                        .addComponent(acfInput, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE))
                .addPreferredGap(UNRELATED)
                .addGroup(formLayout.createParallelGroup(LEADING, false)
                        .addComponent(depLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                        .addComponent(depInput, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                        .addComponent(arrLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                        .addComponent(arrInput, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE))
                .addPreferredGap(RELATED)
                .addGroup(formLayout.createParallelGroup(LEADING, true)
                        .addComponent(depHint, PREFERRED_SIZE, PREFERRED_SIZE, Short.MAX_VALUE)
                        .addComponent(arrHint, PREFERRED_SIZE, PREFERRED_SIZE, Short.MAX_VALUE))
                .addContainerGap(20, 20);
        var vGroup = formLayout.createSequentialGroup()
                .addContainerGap(20, 20)
                .addGroup(formLayout.createParallelGroup(BASELINE)
                        .addComponent(csLabel)
                        .addComponent(depLabel))
                .addGroup(formLayout.createParallelGroup(BASELINE, false)
                        .addComponent(csInput)
                        .addComponent(depInput)
                        .addComponent(depHint))
                .addGap(10)
                .addGroup(formLayout.createParallelGroup(BASELINE, false)
                        .addComponent(acfLabel)
                        .addComponent(arrLabel))
                .addGroup(formLayout.createParallelGroup(BASELINE, false)
                        .addComponent(acfInput)
                        .addComponent(arrInput)
                        .addComponent(arrHint))
                .addContainerGap(20, 20);
        var actionPane = new JPanel();
        actionLabel = new JLabel();
        simbriefBtn = new JButton("Simbrief");
        submitBtn = new JButton("SUBMIT");

        // Flight Dispatcher
        csInput.setForeground(Color.black);
        acfInput.setForeground(Color.black);
        depInput.getDocument().addDocumentListener(docListener);
        arrInput.getDocument().addDocumentListener(docListener);
        depHint.setOpaque(true);
        arrHint.setOpaque(true);
        depHint.setBorder(getMargin(depHint, 0, 10, 0, 10));
        arrHint.setBorder(getMargin(arrHint, 0, 10, 0, 10));
        depHint.setBackground(Color.darkGray);
        arrHint.setBackground(Color.darkGray);
        simbriefBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                importSimbrief();
            }
        });
        simbriefBtn.setToolTipText("Import your Simbrief flight plan.");
        simbriefBtn.setFont(boldFont);
        submitBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getComponent().isEnabled()) {
                    submitFlightPlan();
                }
            }
        });
        submitBtn.setToolTipText(FORM_EMPTY);
        submitBtn.setEnabled(false);
        submitBtn.setFont(boldFont);
        actionPane.setLayout(new BoxLayout(actionPane, BoxLayout.X_AXIS));
        actionPane.add(Box.createHorizontalGlue());
        actionPane.add(actionLabel);
        actionPane.add(Box.createHorizontalStrut(20));
        actionPane.add(simbriefBtn);
        actionPane.add(Box.createHorizontalStrut(10));
        actionPane.add(submitBtn);
        actionPane.add(Box.createHorizontalStrut(20));
        formLayout.setHorizontalGroup(hGroup);
        formLayout.setVerticalGroup(vGroup);
        formLayout.linkSize(SwingConstants.VERTICAL, depInput, depHint);
        formLayout.linkSize(SwingConstants.VERTICAL, arrInput, arrHint);
        formPane.setLayout(formLayout);
        this.setBorder(BorderFactory.createTitledBorder("Flight Dispatcher"));
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(formPane);
        this.add(Box.createVerticalStrut(10));
        this.add(actionPane);
        this.add(Box.createVerticalStrut(20));
    }

    private void validateInput() {
        var dep = Optional.ofNullable(depInput.getText()).orElse("");
        var arr = Optional.ofNullable(arrInput.getText()).orElse("");
        var depHintSize = depHint.getSize();
        var arrHintSize = arrHint.getSize();
        SimBridge simBridge = simTracker.getBridge();
        Optional<Airport> departure = simBridge.getAirport(dep);
        Optional<Airport> arrival = simBridge.getAirport(arr);
        boolean valid = departure.isPresent() && arrival.isPresent();

        depHint.setText(departure.map(Airport::getName).orElse(NOT_FOUND));
        arrHint.setText(arrival.map(Airport::getName).orElse(NOT_FOUND));
        depHint.setPreferredSize(depHintSize);
        arrHint.setPreferredSize(arrHintSize);
        depHint.setForeground(departure.isEmpty() ? Color.yellow : Color.green);
        arrHint.setForeground(arrival.isEmpty() ? Color.yellow : Color.green);
        submitBtn.setEnabled(valid);
        submitBtn.setToolTipText(valid ? "Submit your flight plan" : FORM_EMPTY);
    }

    private void importSimbrief() {
        final var props = Properties.read();
        var name = props.getSimbriefName();

        if (name == null || name.isBlank()) {
            SwingUtilities.invokeLater(() -> {
                String input = JOptionPane.showInputDialog("Please specify your Simbrief name.");
                props.setSimbriefName(input);
                props.save();
            });
            return;
        }

        simbriefBtn.setEnabled(false);
        actionLabel.setForeground(Color.black);
        actionLabel.setText("Loading...");

        var endpoint = "https://www.simbrief.com/api/xml.fetcher.php?username=%s&json=1";
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder()
                .uri(URI.create(String.format(endpoint, name)))
                .timeout(Duration.ofSeconds(7))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .exceptionally(t -> {
                    if (ExceptionUtils.indexOfType(t, HttpTimeoutException.class) > -1) {
                        actionLabel.setText("Connection timeout.");
                    } else if (ExceptionUtils.indexOfType(t, ConnectException.class) > -1) {
                        actionLabel.setText("Connection refused.");
                    } else {
                        actionLabel.setText("Failed to fetch.");
                        logger.log(Level.SEVERE, "Failed to fetch simbrief OFP.", t);
                    }
                    actionLabel.setForeground(Color.red);
                    simbriefBtn.setEnabled(true);
                    return null;
                })
                .thenApply(response -> {
                    if (response == null) {
                        return null;
                    }

                    try {
                        return new ObjectMapper().readValue(response.body(), FlightPlan.class);
                    } catch (JsonProcessingException e) {
                        logger.log(Level.SEVERE, "Failed to parse json.", e);
                        actionLabel.setText(null);
                        simbriefBtn.setEnabled(true);
                        return null;
                    }
                })
                .thenAccept(plan -> SwingUtilities.invokeLater(() -> {
                    if (plan == null) {
                        return;
                    }

                    this.plan = plan;
                    var acf = plan.getAircraft();
                    csInput.setText(plan.getCallsign());
                    acfInput.setText((acf != null) ? acf.getIcaoCode() : "");
                    depInput.setText(plan.getDepartureCode());
                    arrInput.setText(plan.getArrivalCode());
                    actionLabel.setForeground(Color.blue);
                    actionLabel.setText("Fetch complete!");
                    simbriefBtn.setEnabled(true);
                    validateInput();
                }));
    }

    private void submitFlightPlan() {
        if (plan == null) {
            plan = new FlightPlan();
        }

        var acf = acfInput.getText();
        plan.setCallsign(csInput.getText());
        plan.setAircraft((acf != null) ? aircraftRepo.get(acf) : null);
        plan.setDepartureCode(depInput.getText());
        plan.setArrivalCode(arrInput.getText());
        FlightPlan.submit(plan);
        simDataService.requestUpdate();
        actionLabel.setText("Plan sent!");
    }
}
