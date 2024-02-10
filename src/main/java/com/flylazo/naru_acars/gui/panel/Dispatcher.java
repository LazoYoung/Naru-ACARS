package com.flylazo.naru_acars.gui.panel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flylazo.naru_acars.NaruACARS;
import com.flylazo.naru_acars.domain.FlightPlan;
import com.flylazo.naru_acars.domain.Properties;
import com.flylazo.naru_acars.domain.acars.VirtualAirline;
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
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.swing.JOptionPane.*;
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
    private final JButton importBtn;
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
        this.importBtn = new JButton("Import");
        this.actionBtn = new JButton(ACTION_SUBMIT);
        var layout = new GroupLayout(this);
        var noteFont = new Font("Ubuntu Regular", Font.PLAIN, 13);
        var btnFont = new Font("Ubuntu Medium", Font.PLAIN, 15);
        var noteLabel = window.bakeLabel("* Optional fields", noteFont, Color.black);
        var actionPane = new JPanel();

        // Flight Dispatcher
        super.setButtonListener(this.importBtn, this::openImportPrompt);
        super.setButtonListener(this.actionBtn, this::submitFlightPlan);
        this.actionBtn.setToolTipText("Submit your flight plan");
        this.importBtn.setFont(btnFont);
        this.actionBtn.setFont(btnFont);
        actionPane.setLayout(new BoxLayout(actionPane, BoxLayout.X_AXIS));
        actionPane.add(Box.createHorizontalGlue());
        actionPane.add(this.actionLabel);
        actionPane.add(Box.createHorizontalStrut(20));
        actionPane.add(this.importBtn);
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
        this.importBtn.setEnabled(edit);
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
                String input = showInputDialog("Your Simbrief name or id...");
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

        this.importBtn.setEnabled(false);
        this.actionBtn.setEnabled(false);
        this.actionLabel.setForeground(Color.black);
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
                        this.logger.log(Level.SEVERE, "Failed to fetch simbrief OFP.", t);
                    }
                    this.importBtn.setEnabled(true);
                    this.actionBtn.setEnabled(true);
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
                        this.sendActionMessage("Fetch complete.", Color.blue);
                    }
                    this.importBtn.setEnabled(true);
                    this.actionBtn.setEnabled(true);
                }));
    }

    private void importBooking() {
        this.importBtn.setEnabled(false);
        this.actionBtn.setEnabled(false);
        this.sendActionMessage("Loading...", Color.black);

        if (this.acarsService.isConnected()) {
            var context = this.acarsService.getContext();
            this.acarsService.fetchBooking(context, this::getBookingResponse, r -> this.handleBookingError(r.getResponse()));
        } else {
            final var props = Properties.read();
            final var server = VirtualAirline.getById(props.getVirtualAirline());
            var key = props.getAcarsAPI();

            if (key == null) {
                key = JOptionPane.showInputDialog("Please specify your ACARS API.");
            }

            this.acarsService.getConnector(server)
                    .withAPIKey(key)
                    .whenError(error -> this.handleBookingError(error.message))
                    .whenSuccess(context -> {
                        this.importBooking();
                        context.terminate();
                    })
                    .connect();
        }

    }

    private void openImportPrompt() {
        var panel = new JPanel();
        var font = new Font("Ubuntu Medium", Font.PLAIN, 16);
        var text = new JLabel("Select the source to import from.");
        var group = new ButtonGroup();
        var simbriefBtn = new JRadioButton("Simbrief");
        var bookingBtn = new JRadioButton("ACARS booking");
        var layout = new GroupLayout(panel);
        var hGroup = layout.createParallelGroup()
                .addComponent(text)
                .addComponent(simbriefBtn)
                .addComponent(bookingBtn);
        var vGroup = layout.createSequentialGroup()
                .addComponent(text)
                .addComponent(simbriefBtn)
                .addComponent(bookingBtn);
        layout.setHorizontalGroup(hGroup);
        layout.setVerticalGroup(vGroup);
        text.setFont(font);
        group.add(simbriefBtn);
        group.add(bookingBtn);
        panel.add(text);
        panel.add(simbriefBtn);
        panel.add(bookingBtn);
        panel.setLayout(layout);
        simbriefBtn.setSelected(true);
        int option = showConfirmDialog(this.window, panel, "Import flightplan", DEFAULT_OPTION, PLAIN_MESSAGE);

        if (option != OK_OPTION) return;

        if (simbriefBtn.isSelected()) {
            this.importSimbrief();
        } else if (bookingBtn.isSelected()) {
            this.importBooking();
        }
    }

    private void getBookingResponse(BookingResponse response) {
        SwingUtilities.invokeLater(() -> {
            var flightPlan = response.getFlightPlan();
            flightPlan.markAsBooked();
            this.fillForm(flightPlan);
            this.importBtn.setEnabled(true);
            this.actionBtn.setEnabled(true);
            this.sendActionMessage("Fetch complete.", Color.blue);
        });
    }

    private void handleBookingError(String message) {
        this.window.showDialog(ERROR_MESSAGE, message);
        this.importBtn.setEnabled(true);
        this.actionBtn.setEnabled(true);
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
        var offBlock = flightPlan.getBlockOff();
        var flightTime = flightInput.getFlightTime();
        assert flightTime != null;

        if (offBlock != null) {
            flightPlan.setBlockOn(offBlock.plus(flightTime));
        } else {
            flightPlan.setBlockOff(Instant.now());
            flightPlan.setBlockOn(Instant.now().plus(flightTime));
        }

        flightPlan.setCallsign(flightInput.getCallsign());
        flightPlan.setAircraft(flightInput.getAircraft());
        flightPlan.setBlockTime(flightTime);
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
