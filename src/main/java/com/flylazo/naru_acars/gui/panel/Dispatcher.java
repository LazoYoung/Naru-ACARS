package com.flylazo.naru_acars.gui.panel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flylazo.naru_acars.NaruACARS;
import com.flylazo.naru_acars.domain.FlightPlan;
import com.flylazo.naru_acars.domain.Properties;
import com.flylazo.naru_acars.gui.Window;
import com.flylazo.naru_acars.gui.component.FlightInput;
import com.flylazo.naru_acars.gui.component.Header;
import com.flylazo.naru_acars.gui.component.RouteInput;
import com.flylazo.naru_acars.servlet.service.SimDataService;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.swing.*;
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
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.swing.LayoutStyle.ComponentPlacement.UNRELATED;

public class Dispatcher extends PanelBase {
    private final Logger logger = Logger.getLogger(NaruACARS.class.getName());
    private final SimDataService simDataService;
    private final FlightInput flightInput;
    private final RouteInput routeInput;
    private final JLabel actionLabel;
    private final JButton simbriefBtn;
    private FlightPlan plan = null;

    public Dispatcher(Window window, int margin) {
        super(window);

        this.simDataService = window.getServiceFactory().getBean(SimDataService.class);
        var labelFont = new Font("Ubuntu Regular", Font.BOLD, 15);
        this.flightInput = new FlightInput(window, labelFont);
        this.routeInput = new RouteInput(window, labelFont);
        var layout = new GroupLayout(this);
        var noteFont = new Font("Ubuntu Regular", Font.PLAIN, 13);
        var btnFont = new Font("Ubuntu Medium", Font.PLAIN, 15);
        var noteLabel = window.bakeLabel("* Optional fields", noteFont, Color.black);
        var actionPane = new JPanel();
        var submitBtn = new JButton("SUBMIT");
        actionLabel = new JLabel();
        simbriefBtn = new JButton("Simbrief");

        // Flight Dispatcher
        simbriefBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                importSimbrief();
            }
        });
        simbriefBtn.setToolTipText("Import your Simbrief flight plan.");
        simbriefBtn.setFont(btnFont);
        submitBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getComponent().isEnabled()) {
                    submitFlightPlan();
                }
            }
        });
        submitBtn.setFont(btnFont);
        submitBtn.setToolTipText("Submit your flight plan");
        actionPane.setLayout(new BoxLayout(actionPane, BoxLayout.X_AXIS));
        actionPane.add(Box.createHorizontalGlue());
        actionPane.add(actionLabel);
        actionPane.add(Box.createHorizontalStrut(20));
        actionPane.add(simbriefBtn);
        actionPane.add(Box.createHorizontalStrut(10));
        actionPane.add(submitBtn);

        var titleFont = new Font("Ubuntu Medium", Font.PLAIN, 16);
        var header = new Header(titleFont, "Flight Dispatcher");
        var glue = Box.createVerticalGlue();
        var hGroup = layout.createSequentialGroup()
                .addContainerGap(margin, margin)
                .addGroup(layout.createParallelGroup()
                        .addComponent(header)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup()
                                        .addComponent(flightInput)
                                        .addComponent(glue)
                                        .addComponent(noteLabel))
                                .addPreferredGap(UNRELATED)
                                .addComponent(routeInput))
                        .addComponent(actionPane))
                .addContainerGap(margin, margin);
        var vGroup = layout.createSequentialGroup()
                .addContainerGap(margin, margin)
                .addComponent(header)
                .addGroup(layout.createParallelGroup()
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(flightInput)
                                .addComponent(glue)
                                .addComponent(noteLabel))
                        .addComponent(routeInput))
                .addPreferredGap(UNRELATED)
                .addComponent(actionPane)
                .addContainerGap(margin, margin);
        layout.setHorizontalGroup(hGroup);
        layout.setVerticalGroup(vGroup);
        this.setLayout(layout);
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
                        this.sendActionMessage("Connection timeout.", Color.red);
                    } else if (ExceptionUtils.indexOfType(t, ConnectException.class) > -1) {
                        this.sendActionMessage("Connection refused.", Color.red);
                    } else {
                        this.sendActionMessage("Process fail.", Color.red);
                        logger.log(Level.SEVERE, "Failed to fetch simbrief OFP.", t);
                    }
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

                        return null;
                    }
                })
                .thenAccept(plan -> SwingUtilities.invokeLater(() -> {
                    if (plan == null) {
                        return;
                    }

                    this.plan = plan;
                    var acf = plan.getAircraft();
                    var t = plan.getBlockTime().toMinutes();
                    flightInput.setCallsign(plan.getCallsign());
                    flightInput.setAircraft((acf != null) ? acf.getIcaoCode() : "");
                    flightInput.setFlightTime(String.format("%d:%02d", t / 60, t % 60));
                    routeInput.setDeparture(plan.getDepartureCode());
                    routeInput.setArrival(plan.getArrivalCode());
                    routeInput.setAlternate(plan.getAlternateCode());
                    routeInput.setRoute(plan.getRoute());
                    routeInput.setRemarks(plan.getRemarks());
                    this.sendActionMessage("Fetch complete.", Color.blue);
                    simbriefBtn.setEnabled(true);
                    routeInput.validateForm();
                }));
    }

    private void submitFlightPlan() {
        if (!flightInput.validateForm() || !routeInput.validateForm()) {
            this.sendActionMessage("Please fill out the form", Color.red);
            return;
        }

        if (plan == null) {
            plan = new FlightPlan();
        }

        plan.setCallsign(flightInput.getCallsign());
        plan.setAircraft(flightInput.getAircraft());
        plan.setBlockTime(flightInput.getFlightTime());
        plan.setDepartureCode(routeInput.getDeparture());
        plan.setArrivalCode(routeInput.getArrival());
        plan.setAlternateCode(routeInput.getAlternate());
        plan.setRoute(routeInput.getRoute());
        plan.setRemarks(routeInput.getRemarks());
        FlightPlan.submit(plan);
        simDataService.requestUpdate();
        this.sendActionMessage("Plan sent!", Color.blue);
    }

    private void sendActionMessage(String text, Color color) {
        actionLabel.setForeground(color);
        actionLabel.setText(text);

        var service = Executors.newSingleThreadScheduledExecutor();
        service.schedule(() -> {
            SwingUtilities.invokeLater(() -> actionLabel.setText(null));
        }, 3, TimeUnit.SECONDS);
    }
}
