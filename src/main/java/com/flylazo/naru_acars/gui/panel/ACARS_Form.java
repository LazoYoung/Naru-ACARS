package com.flylazo.naru_acars.gui.panel;

import com.flylazo.naru_acars.NaruACARS;
import com.flylazo.naru_acars.domain.FlightPlan;
import com.flylazo.naru_acars.domain.acars.ServiceType;
import com.flylazo.naru_acars.domain.acars.VirtualAirline;
import com.flylazo.naru_acars.domain.acars.response.BookingResponse;
import com.flylazo.naru_acars.domain.acars.response.ErrorResponse;
import com.flylazo.naru_acars.domain.acars.response.Response;
import com.flylazo.naru_acars.domain.acars.response.Status;
import com.flylazo.naru_acars.gui.Window;
import com.flylazo.naru_acars.gui.component.Header;
import com.flylazo.naru_acars.gui.component.TextInput;
import com.flylazo.naru_acars.gui.page.DispatchPage;
import com.flylazo.naru_acars.servlet.service.ACARS_Service;
import com.flylazo.naru_acars.servlet.socket.SocketContext;
import com.flylazo.naru_acars.servlet.socket.SocketError;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.swing.GroupLayout.Alignment.LEADING;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.JOptionPane.*;

public class ACARS_Form extends PanelBase {
    private static final String CONNECT = "Connect";
    private final Logger logger;
    private final ACARS_Service service;
    private final JComboBox<VirtualAirline> serverCombo;
    private final TextInput apiInput;
    private final JButton connectBtn;
    private final JCheckBox charterCheckbox;

    public ACARS_Form(Window window, int margin) {
        super(window);

        var form = new JPanel();
        var headerFont = new Font("Ubuntu Medium", Font.PLAIN, 16);
        var boldFont = new Font("Ubuntu Medium", Font.PLAIN, 15);
        var header = new Header(headerFont, "ACARS settings");
        var vaLabel = window.bakeLabel("Virtual airline", boldFont, Color.black);
        var apiLabel = window.bakeLabel("API key", boldFont, Color.black);
        var btnFont = new Font("Ubuntu Medium", Font.PLAIN, 15);
        this.logger = NaruACARS.logger;
        this.apiInput = new TextInput("Paste the key of your VA account", 20, false);
        this.serverCombo = new JComboBox<>(VirtualAirline.values());
        this.service = window.getServiceFactory().getBean(ACARS_Service.class);
        this.charterCheckbox = new JCheckBox("Charter flight", false);
        this.connectBtn = new JButton(CONNECT);
        this.connectBtn.setFont(btnFont);
        this.service.getListener().observeClose(this::onClose);
        super.setButtonListener(this.connectBtn, this::connectServer);

        var inputPanel = new JPanel();
        var inputLayout = new GroupLayout(inputPanel);
        var ihGroup = inputLayout.createSequentialGroup()
                .addGroup(inputLayout.createParallelGroup()
                        .addComponent(vaLabel)
                        .addComponent(this.serverCombo))
                .addGap(margin * 2) // glue won't stretch
                .addGroup(inputLayout.createParallelGroup()
                        .addComponent(apiLabel)
                        .addComponent(this.apiInput));
        var ivGroup = inputLayout.createParallelGroup()
                .addGroup(inputLayout.createSequentialGroup()
                        .addComponent(vaLabel)
                        .addComponent(this.serverCombo))
                .addGroup(inputLayout.createSequentialGroup()
                        .addComponent(apiLabel)
                        .addComponent(this.apiInput));
        inputLayout.setHorizontalGroup(ihGroup);
        inputLayout.setVerticalGroup(ivGroup);
        inputPanel.setLayout(inputLayout);

        var layout = new GroupLayout(form);
        var hGlue = Box.createHorizontalGlue();
        var hGroup = layout.createSequentialGroup()
                .addContainerGap(margin, margin)
                .addGroup(layout.createParallelGroup()
                        .addComponent(header)
                        .addComponent(inputPanel, PREFERRED_SIZE, PREFERRED_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(this.charterCheckbox)
                                .addComponent(hGlue)
                                .addComponent(this.connectBtn)))
                .addContainerGap(margin, margin);
        var vGroup = layout.createSequentialGroup()
                .addContainerGap(margin, margin)
                .addComponent(header)
                .addComponent(inputPanel)
                .addGap(margin)
                .addGroup(layout.createParallelGroup(LEADING, false)
                        .addComponent(this.charterCheckbox)
                        .addComponent(hGlue)
                        .addComponent(this.connectBtn))
                .addContainerGap(margin, margin);
        layout.setHorizontalGroup(hGroup);
        layout.setVerticalGroup(vGroup);
        form.setLayout(layout);
        this.add(form);
    }

    private void connectServer() {
        var airline = (VirtualAirline) this.serverCombo.getSelectedItem();

        if (airline == null) {
            this.window.showDialog(WARNING_MESSAGE, "Select a virtual airline first.");
            return;
        }

        if (!FlightPlan.isDispatched()) {
            this.window.showDialog(WARNING_MESSAGE, "Submit a flightplan first!");
            this.window.selectPage(DispatchPage.class);
            return;
        }

        if (this.service.isConnected()) {
            this.window.showDialog(WARNING_MESSAGE, "Already connected!");
            return;
        }

        try {
            this.service.getConnector(airline)
                    .withAPIKey(this.apiInput.getText())
                    .whenSuccess(this::onConnected)
                    .whenError(this::alertError)
                    .connect();
        } catch (Throwable t) {
            this.logger.log(Level.SEVERE, "Failed to connect!", t);
        }
    }

    private void disconnectServer() {
        this.service.disconnect();
        this.window.showDialog(INFORMATION_MESSAGE, "Disconnected from server.");
    }

    private void onConnected(SocketContext context) {
        this.connectBtn.setText("Disconnect");
        super.setButtonListener(this.connectBtn, this::disconnectServer);

        if (this.charterCheckbox.isSelected()) {
            this.service.startFlight(ServiceType.CHARTER, this::getStartResponse, this::handleStartError);
        } else {
            this.service.fetchBooking(this::getBookingResponse, this::handleBookingError);
        }
    }

    private void getBookingResponse(BookingResponse response) {
        FlightPlan plan = response.getFlightPlan();
        plan.markAsBooked();

        if (FlightPlan.getDispatched().isBooked()) {
            this.service.startFlight(ServiceType.SCHEDULE, this::getStartResponse, this::handleStartError);
        } else {
            SwingUtilities.invokeLater(() -> {
                String title = "Flightplan mismatch";
                String message = "Would you like to import the flightplan?";
                int option = JOptionPane.showConfirmDialog(this.window, message, title, YES_NO_OPTION);

                if (option == YES_OPTION) {
                    FlightPlan.submit(plan);
                    this.service.startFlight(ServiceType.SCHEDULE, this::getStartResponse, this::handleStartError);
                } else {
                    this.service.disconnect();
                }
            });
        }
    }

    private void handleBookingError(ErrorResponse response) {
        if (response.getStatus() == Status.NOT_FOUND) {
            this.service.disconnect();
            this.window.showDialog(WARNING_MESSAGE, "Booking schedule not found!");
        } else {
            this.logger.log(Level.SEVERE, response.getResponse());
        }
    }

    private void getStartResponse(Response response) {
        this.window.showDialog(INFORMATION_MESSAGE, "Welcome back, captain!");
    }

    private void handleStartError(ErrorResponse response) {
        this.service.disconnect();
        this.logger.log(Level.SEVERE, response.getResponse());
    }

    private void onClose() {
        this.connectBtn.setText(CONNECT);
        super.setButtonListener(this.connectBtn, this::connectServer);
    }

    private void alertError(SocketError error) {
        String message = switch (error) {
            case OFFLINE -> "Server is offline!";
            case API_KEY_IN_USE -> "API key is in use!";
            case API_KEY_INVALID -> "API key is invalid!";
            case FATAL_ERROR -> "Fatal error!";
        };
        this.window.showDialog(ERROR_MESSAGE, message);
    }

}
