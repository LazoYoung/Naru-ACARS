package com.flylazo.naru_acars.gui.panel;

import com.flylazo.naru_acars.NaruACARS;
import com.flylazo.naru_acars.domain.FlightPlan;
import com.flylazo.naru_acars.domain.Properties;
import com.flylazo.naru_acars.domain.acars.ServiceType;
import com.flylazo.naru_acars.domain.acars.VirtualAirline;
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
import java.time.Instant;
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
        var apiKey = Properties.read().getAcarsAPI();
        this.logger = NaruACARS.logger;
        this.apiInput = new TextInput("Paste the key of your VA account", 20, false);
        this.apiInput.setText(apiKey);
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
        final var service = this.charterCheckbox.isSelected() ? ServiceType.CHARTER : ServiceType.SCHEDULE;
        final var props = Properties.read();
        final var server = (VirtualAirline) this.serverCombo.getSelectedItem();

        this.service.startFlight(service, this::getStartResponse, this::handleStartError);
        this.connectBtn.setText("Disconnect");
        super.setButtonListener(this.connectBtn, this::disconnectServer);
        props.setAcarsAPI(this.apiInput.getText());
        props.setVirtualAirline(server != null ? server.getId() : 0);
        props.save();
    }

    private void getStartResponse(Response response) {
        this.window.showDialog(INFORMATION_MESSAGE, "Welcome back, captain!");
    }

    private void handleStartError(ErrorResponse response) {
        final var status = response.getStatus();

        if (status == Status.BEFORE_FLIGHT) {
            int epochStart = Integer.parseInt(response.getResponse());
            long min = (epochStart - Instant.now().getEpochSecond()) / 60;
            var message = String.format("The flight starts in %d minutes.", min);
            this.window.showDialog(WARNING_MESSAGE, message);
            this.service.disconnect();
        } else if (status == Status.BAD_STATE) {
            final boolean isCharter = this.service.getServiceType() == ServiceType.CHARTER;
            final var message = isCharter ? "Would you like to restart flight?" : "Would you like to resume flight?";
            final var title = "ACARS message";
            final int choice = JOptionPane.showConfirmDialog(this.window, message, title, YES_NO_OPTION);

            if (choice == NO_OPTION) {
                this.service.disconnect();
            } else if (isCharter) {
                this.restartFlight();
            } else {
                this.service.startTracking();
            }
        } else {
            this.logger.log(Level.SEVERE, response.getResponse());
            this.service.disconnect();
        }
    }

    private void restartFlight() {
        final var service = this.service.getServiceType();

        this.service.cancelFlight(response -> {
            this.service.startFlight(service, this::getStartResponse, this::handleStartError);
        }, error -> {
            this.window.showDialog(ERROR_MESSAGE, error.getResponse());
        });
    }

    private void onClose() {
        this.connectBtn.setText(CONNECT);
        super.setButtonListener(this.connectBtn, this::connectServer);
    }

    private void alertError(SocketError error) {
        this.window.showDialog(ERROR_MESSAGE, error.message);
    }

}
