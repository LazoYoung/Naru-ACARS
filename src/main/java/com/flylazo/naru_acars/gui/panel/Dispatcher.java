package com.flylazo.naru_acars.gui.panel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flylazo.naru_acars.NaruACARS;
import com.flylazo.naru_acars.domain.FlightPlan;
import com.flylazo.naru_acars.domain.Properties;
import com.flylazo.naru_acars.domain.acars.response.BookingResponse;
import com.flylazo.naru_acars.domain.acars.response.ErrorResponse;
import com.flylazo.naru_acars.gui.Window;
import com.flylazo.naru_acars.gui.component.FlightInput;
import com.flylazo.naru_acars.gui.component.Header;
import com.flylazo.naru_acars.gui.component.RouteInput;
import com.flylazo.naru_acars.servlet.service.ACARS_Service;
import com.flylazo.naru_acars.servlet.service.SimDataService;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.swing.*;
import java.awt.*;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.LayoutStyle.ComponentPlacement.UNRELATED;

public class Dispatcher extends PanelBase {
    private static final String ACTION_SUBMIT = "Submit";
    private static final String ACTION_REVISE = "Revise";
    private final Logger logger;
    private final ACARS_Service acarsService;
    private final SimDataService simDataService;
    private final FlightInput flightInput;
    private final RouteInput routeInput;
    private final JLabel actionLabel;
    private final JButton simbriefBtn;
    private final JButton bookingBtn;
    private final JButton actionBtn;
    private ScheduledFuture<?> actionTask;

    public Dispatcher(Window window, int margin) {
        super(window);

        var labelFont = new Font("Ubuntu Regular", Font.BOLD, 15);
        this.logger = Logger.getLogger(NaruACARS.class.getName());
        this.acarsService = window.getServiceFactory().getBean(ACARS_Service.class);
        this.simDataService = window.getServiceFactory().getBean(SimDataService.class);
        this.flightInput = new FlightInput(window, labelFont);
        this.routeInput = new RouteInput(window, labelFont);
        this.actionLabel = new JLabel();
        this.simbriefBtn = new JButton("Simbrief");
        this.bookingBtn = new JButton("Booking");
        this.actionBtn = new JButton(ACTION_SUBMIT);
        var layout = new GroupLayout(this);
        var noteFont = new Font("Ubuntu Regular", Font.PLAIN, 13);
        var btnFont = new Font("Ubuntu Medium", Font.PLAIN, 15);
        var noteLabel = window.bakeLabel("* Optional fields", noteFont, Color.black);
        var actionPane = new JPanel();

        // Flight Dispatcher
        super.setButtonListener(this.simbriefBtn, this::importSimbrief);
        super.setButtonListener(this.bookingBtn, this::importBooking);
        super.setButtonListener(this.actionBtn, this::submitFlightPlan);
        this.simbriefBtn.setToolTipText("Import from Simbrief.");
        this.bookingBtn.setToolTipText("Import from ACARS booking.");
        this.actionBtn.setToolTipText("Submit your flight plan");
        this.simbriefBtn.setFont(btnFont);
        this.bookingBtn.setFont(btnFont);
        this.actionBtn.setFont(btnFont);
        actionPane.setLayout(new BoxLayout(actionPane, BoxLayout.X_AXIS));
        actionPane.add(Box.createHorizontalGlue());
        actionPane.add(this.actionLabel);
        actionPane.add(Box.createHorizontalStrut(20));
        actionPane.add(this.bookingBtn);
        actionPane.add(Box.createHorizontalStrut(10));
        actionPane.add(this.simbriefBtn);
        actionPane.add(Box.createHorizontalStrut(10));
        actionPane.add(this.actionBtn);
        FlightPlan.observeDispatch(this::onNewDispatch);
        this.setEditMode(true);

        var titleFont = new Font("Ubuntu Medium", Font.PLAIN, 16);
        var header = new Header(titleFont, "Flight Dispatcher");
        var glue = Box.createVerticalGlue();
        var hGroup = layout.createSequentialGroup()
                .addContainerGap(margin, margin)
                .addGroup(layout.createParallelGroup()
                        .addComponent(header)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup()
                                        .addComponent(this.flightInput)
                                        .addComponent(glue)
                                        .addComponent(noteLabel))
                                .addPreferredGap(UNRELATED)
                                .addComponent(this.routeInput))
                        .addComponent(actionPane))
                .addContainerGap(margin, margin);
        var vGroup = layout.createSequentialGroup()
                .addContainerGap(margin, margin)
                .addComponent(header)
                .addGroup(layout.createParallelGroup()
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(this.flightInput)
                                .addComponent(glue)
                                .addComponent(noteLabel))
                        .addComponent(this.routeInput))
                .addPreferredGap(UNRELATED)
                .addComponent(actionPane)
                .addContainerGap(margin, margin);
        layout.setHorizontalGroup(hGroup);
        layout.setVerticalGroup(vGroup);
        this.setLayout(layout);
    }

    private void setEditMode(boolean edit) {
        this.bookingBtn.setEnabled(edit);
        this.simbriefBtn.setEnabled(edit);
        this.routeInput.setEnabled(edit);
        this.flightInput.setEnabled(edit);
        this.actionBtn.setText(edit ? ACTION_SUBMIT : ACTION_REVISE);
        this.setButtonListener(this.actionBtn, () -> {
            if (edit) {
                submitFlightPlan();
            } else {
                setEditMode(true);
            }
        });
    }

    private void importSimbrief() {
        final var props = Properties.read();
        var username = props.getSimbriefId();
        String uri;

        if (username == null || username.isBlank()) {
            SwingUtilities.invokeLater(() -> {
                String input = JOptionPane.showInputDialog("Your Simbrief name or id...");
                props.setSimbriefId(input);
                props.save();
            });
            return;
        }

        try {
            int id = Integer.parseInt(username);
            var endpoint = "https://www.simbrief.com/api/xml.fetcher.php?userid=%d&json=1";
            uri = String.format(endpoint, id);
        } catch (NumberFormatException e) {
            var endpoint = "https://www.simbrief.com/api/xml.fetcher.php?username=%s&json=1";
            uri = String.format(endpoint, username);
        }

        simbriefBtn.setEnabled(false);
        actionLabel.setForeground(Color.black);
        this.sendActionMessage("Loading...", Color.black);

        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
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
                    } catch (JsonProcessingException ex) {
                        this.logger.log(Level.SEVERE, "Failed to parse json.", ex);
                        return null;
                    }
                })
                .thenAccept(flightPlan -> SwingUtilities.invokeLater(() -> {
                    if (flightPlan != null) {
                        fillForm(flightPlan);
                        this.simbriefBtn.setEnabled(true);
                        this.sendActionMessage("Fetch complete.", Color.blue);
                    }
                }));
    }

    private void importBooking() {
        this.bookingBtn.setEnabled(false);
        this.sendActionMessage("Loading...", Color.black);

        try {
            this.acarsService.fetchBooking(this::getBookingResponse, this::handleBookingError);
        } catch (IllegalStateException e) {
            this.bookingBtn.setEnabled(true);
            this.window.showDialog(ERROR_MESSAGE, "ACARS is offline.");
        }
    }

    private void getBookingResponse(BookingResponse response) {
        SwingUtilities.invokeLater(() -> {
            var flightPlan = response.getFlightPlan();
            flightPlan.markAsBooked();
            this.fillForm(flightPlan);
            this.bookingBtn.setEnabled(true);
            this.sendActionMessage("Fetch complete.", Color.blue);
        });
    }

    private void handleBookingError(ErrorResponse response) {
        this.window.showDialog(ERROR_MESSAGE, response.toString());
        this.bookingBtn.setEnabled(true);
        this.clearActionMessage();
    }

    private void onNewDispatch(FlightPlan plan) {
        this.fillForm(plan);
        this.setEditMode(false);
    }

    private void fillForm(FlightPlan plan) {
        var acf = plan.getAircraft();
        var bt = plan.getBlockTime();
        var t = (bt != null) ? bt.toMinutes() : 0;
        var flightTime = (t > 0) ? String.format("%d:%02d", t / 60, t % 60) : null;
        var aircraft = (acf != null) ? acf.getIcaoCode() : null;
        flightInput.setCallsign(plan.getCallsign());
        flightInput.setAircraft(aircraft);
        flightInput.setFlightTime(flightTime);
        flightInput.validateForm();
        routeInput.setDeparture(plan.getDepartureCode());
        routeInput.setArrival(plan.getArrivalCode());
        routeInput.setAlternate(plan.getAlternateCode());
        routeInput.setRoute(plan.getRoute());
        routeInput.setRemarks(plan.getRemarks());
        routeInput.validateForm();
    }

    private void submitFlightPlan() {
        if (!flightInput.validateForm() || !routeInput.validateForm()) {
            this.sendActionMessage("Please fill out the form", Color.red);
            return;
        }

        var flightPlan = new FlightPlan();
        flightPlan.setCallsign(flightInput.getCallsign());
        flightPlan.setAircraft(flightInput.getAircraft());
        flightPlan.setBlockTime(flightInput.getFlightTime());
        flightPlan.setDepartureCode(routeInput.getDeparture());
        flightPlan.setArrivalCode(routeInput.getArrival());
        flightPlan.setAlternateCode(routeInput.getAlternate());
        flightPlan.setRoute(routeInput.getRoute());
        flightPlan.setRemarks(routeInput.getRemarks());
        FlightPlan.submit(flightPlan);
        this.simDataService.requestUpdate();
        this.sendActionMessage("Plan sent!", Color.blue);
    }

    private void sendActionMessage(String text, Color color) {
        if (this.actionTask != null) {
            this.actionTask.cancel(true);
        }

        this.actionLabel.setForeground(color);
        this.actionLabel.setText(text);
        this.actionTask = Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            SwingUtilities.invokeLater(() -> actionLabel.setText(null));
            this.actionTask = null;
        }, 3, TimeUnit.SECONDS);
    }

    private void clearActionMessage() {
        if (this.actionTask != null) {
            this.actionTask.cancel(false);
        }
        this.actionLabel.setText(null);
    }
}
