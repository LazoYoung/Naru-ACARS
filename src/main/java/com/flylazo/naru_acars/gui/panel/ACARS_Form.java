package com.flylazo.naru_acars.gui.panel;

import com.flylazo.naru_acars.NaruACARS;
import com.flylazo.naru_acars.domain.FlightPlan;
import com.flylazo.naru_acars.domain.acars.VirtualAirline;
import com.flylazo.naru_acars.domain.acars.response.BookingResponse;
import com.flylazo.naru_acars.gui.Window;
import com.flylazo.naru_acars.gui.component.TextInput;
import com.flylazo.naru_acars.servlet.service.ACARS_Service;
import com.flylazo.naru_acars.servlet.socket.SocketContext;
import com.flylazo.naru_acars.servlet.socket.SocketError;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.swing.GroupLayout.Alignment.LEADING;
import static javax.swing.JOptionPane.*;

public class ACARS_Form extends PanelBase {
    private static final String CONNECT = "Connect";
    private final Logger logger;
    private final ACARS_Service service;
    private final JComboBox<VirtualAirline> serverCombo;
    private final TextInput apiInput;
    private final JButton connectBtn;

    public ACARS_Form(Window window) {
        super(window);

        var form = new JPanel();
        var formLayout = new GroupLayout(form);
        var hGroup = formLayout.createSequentialGroup();
        var vGroup = formLayout.createSequentialGroup();
        var boldFont = new Font("Ubuntu Medium", Font.PLAIN, 15);
        var vaLabel = window.bakeLabel("Virtual airline", boldFont, Color.black);
        var apiLabel = window.bakeLabel("API key", boldFont, Color.black);
        var btnFont = new Font("Ubuntu Medium", Font.PLAIN, 15);
        this.logger = NaruACARS.logger;
        this.apiInput = new TextInput("Paste the key of your VA account", 30, false);
        this.serverCombo = new JComboBox<>(VirtualAirline.values());
        this.service = window.getServiceFactory().getBean(ACARS_Service.class);
        this.connectBtn = new JButton(CONNECT);
        this.connectBtn.setFont(btnFont);
        this.service.getListener().observeClose(this::onClose);
        super.setButtonListener(this.connectBtn, this::connectServer);

        var hGlue = Box.createHorizontalGlue();
        hGroup.addContainerGap(20, 20)
                .addGroup(formLayout.createParallelGroup(LEADING, false)
                        .addComponent(vaLabel)
                        .addComponent(serverCombo)
                        .addComponent(apiLabel)
                        .addComponent(apiInput)
                        .addGroup(formLayout.createSequentialGroup()
                                .addComponent(hGlue)
                                .addComponent(connectBtn)
                        )
                )
                .addContainerGap(20, 20);
        vGroup.addContainerGap(20, 20)
                .addGroup(formLayout.createSequentialGroup()
                        .addComponent(vaLabel)
                        .addComponent(serverCombo)
                        .addGap(20)
                        .addComponent(apiLabel)
                        .addComponent(apiInput)
                        .addGap(40)
                        .addGroup(formLayout.createParallelGroup(LEADING, false)
                                .addComponent(hGlue)
                                .addComponent(connectBtn)
                        )
                )
                .addContainerGap(20, 20);
        formLayout.setHorizontalGroup(hGroup);
        formLayout.setVerticalGroup(vGroup);
        form.setLayout(formLayout);
        this.add(form);
        this.setBorder(BorderFactory.createTitledBorder("Datalink form"));
    }

    private void connectServer() {
        var airline = (VirtualAirline) this.serverCombo.getSelectedItem();

        if (airline == null) {
            this.window.showDialog(WARNING_MESSAGE, "Select a virtual airline first.");
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
        this.service.getContext().terminate();
        this.window.showDialog(INFORMATION_MESSAGE, "Disconnected from server.");
    }

    private void onConnected(SocketContext context) {
        this.connectBtn.setText("Disconnect");
        super.setButtonListener(this.connectBtn, this::disconnectServer);
        this.service.fetchBooking(this::getBookingResponse, null);
    }

    private void getBookingResponse(BookingResponse response) {
        FlightPlan plan = response.getFlightPlan();
        plan.markAsBooked();

        if (!FlightPlan.getDispatched().isBooked()) {
            SwingUtilities.invokeLater(() -> {
                String title = "Flight booking found";
                String message = "Would you like to import your flightplan?";
                int option = JOptionPane.showConfirmDialog(this.window, message, title, YES_NO_OPTION);

                if (option == YES_OPTION) {
                    FlightPlan.submit(plan);
                }
            });
        }
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
