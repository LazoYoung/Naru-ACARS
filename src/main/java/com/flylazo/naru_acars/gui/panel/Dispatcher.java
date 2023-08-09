package com.flylazo.naru_acars.gui.panel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flylazo.naru_acars.NaruACARS;
import com.flylazo.naru_acars.domain.FlightPlan;
import com.flylazo.naru_acars.domain.Properties;
import com.flylazo.naru_acars.domain.acars.request.FetchBulk;
import com.flylazo.naru_acars.domain.acars.request.Request;
import com.flylazo.naru_acars.domain.acars.response.BookingResponse;
import com.flylazo.naru_acars.domain.acars.response.ErrorResponse;
import com.flylazo.naru_acars.gui.Window;
import com.flylazo.naru_acars.gui.component.FlightInput;
import com.flylazo.naru_acars.gui.component.Header;
import com.flylazo.naru_acars.gui.component.RouteInput;
import com.flylazo.naru_acars.servlet.service.ACARS_Service;
import com.flylazo.naru_acars.servlet.service.SimDataService;
import com.flylazo.naru_acars.servlet.socket.SocketMessage;
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
    private final Logger logger = Logger.getLogger(NaruACARS.class.getName());
    private final ACARS_Service acarsService;
    private final SimDataService simDataService;
    private final FlightInput flightInput;
    private final RouteInput routeInput;
    private final JLabel actionLabel;
    private final JButton simbriefBtn;
    private final JButton bookingBtn;
    private ScheduledFuture<?> actionTask;
    private FlightPlan plan = null;

    public Dispatcher(Window window, int margin) {
        super(window);

        var labelFont = new Font("Ubuntu Regular", Font.BOLD, 15);
        this.acarsService = window.getServiceFactory().getBean(ACARS_Service.class);
        this.simDataService = window.getServiceFactory().getBean(SimDataService.class);
        this.flightInput = new FlightInput(window, labelFont);
        this.routeInput = new RouteInput(window, labelFont);
        this.actionLabel = new JLabel();
        this.simbriefBtn = new JButton("Simbrief");
        this.bookingBtn = new JButton("Booking");
        var layout = new GroupLayout(this);
        var noteFont = new Font("Ubuntu Regular", Font.PLAIN, 13);
        var btnFont = new Font("Ubuntu Medium", Font.PLAIN, 15);
        var noteLabel = window.bakeLabel("* Optional fields", noteFont, Color.black);
        var actionPane = new JPanel();
        var submitBtn = new JButton("SUBMIT");

        // Flight Dispatcher
        super.setButtonListener(this.simbriefBtn, this::importSimbrief);
        super.setButtonListener(this.bookingBtn, this::importBooking);
        super.setButtonListener(submitBtn, this::submitFlightPlan);
        this.simbriefBtn.setToolTipText("Import from Simbrief.");
        this.bookingBtn.setToolTipText("Import from ACARS booking.");
        submitBtn.setToolTipText("Submit your flight plan");
        this.simbriefBtn.setFont(btnFont);
        this.bookingBtn.setFont(btnFont);
        submitBtn.setFont(btnFont);
        actionPane.setLayout(new BoxLayout(actionPane, BoxLayout.X_AXIS));
        actionPane.add(Box.createHorizontalGlue());
        actionPane.add(actionLabel);
        actionPane.add(Box.createHorizontalStrut(20));
        actionPane.add(this.bookingBtn);
        actionPane.add(Box.createHorizontalStrut(10));
        actionPane.add(this.simbriefBtn);
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
        this.sendActionMessage("Loading...", Color.black);

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
                    } catch (JsonProcessingException ex) {
                        this.logger.log(Level.SEVERE, "Failed to parse json.", ex);
                        return null;
                    }
                })
                .thenAccept(plan -> SwingUtilities.invokeLater(() -> {
                    if (plan != null) {
                        fillForm(plan);
                        this.simbriefBtn.setEnabled(true);
                    }
                }));
    }

    private void importBooking() {
        if (!this.acarsService.isConnected()) {
            this.window.showDialog(ERROR_MESSAGE, "ACARS is offline.");
            return;
        }

        var context = this.acarsService.getContext();
        var message = new SocketMessage<BookingResponse>(context);
        var request = new Request()
                .withIntent("fetch")
                .withBulk(new FetchBulk("booking"));
        this.bookingBtn.setEnabled(false);
        this.sendActionMessage("Loading...", Color.black);

        try {
            message.fetchResponse(this::getBookingResponse)
                    .whenError(this::handleBookingError)
                    .send(request);
        } catch (JsonProcessingException e) {
            this.logger.log(Level.SEVERE, "Socket error!", e);
        }
    }

    private void getBookingResponse(BookingResponse response) {
        SwingUtilities.invokeLater(() -> {
            fillForm(response.getFlightPlan());
            this.bookingBtn.setEnabled(true);
            this.sendActionMessage("Fetch complete.", Color.blue);
        });
    }

    private void handleBookingError(ErrorResponse response) {
        this.window.showDialog(ERROR_MESSAGE, response.toString());
        this.bookingBtn.setEnabled(true);
        this.clearActionMessage();
    }

    private void fillForm(FlightPlan plan) {
        this.plan = plan;
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
        this.sendActionMessage("Fetch complete.", Color.blue);
    }

    private void submitFlightPlan() {
        if (!flightInput.validateForm() || !routeInput.validateForm()) {
            this.sendActionMessage("Please fill out the form", Color.red);
            return;
        }

        if (this.plan == null) {
            this.plan = new FlightPlan();
        }

        this.plan.setCallsign(flightInput.getCallsign());
        this.plan.setAircraft(flightInput.getAircraft());
        this.plan.setBlockTime(flightInput.getFlightTime());
        this.plan.setDepartureCode(routeInput.getDeparture());
        this.plan.setArrivalCode(routeInput.getArrival());
        this.plan.setAlternateCode(routeInput.getAlternate());
        this.plan.setRoute(routeInput.getRoute());
        this.plan.setRemarks(routeInput.getRemarks());
        FlightPlan.submit(this.plan);
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
