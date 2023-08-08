package com.flylazo.naru_acars.gui.panel;

import com.flylazo.naru_acars.domain.acars.VirtualAirline;
import com.flylazo.naru_acars.gui.Window;
import com.flylazo.naru_acars.gui.component.TextInput;
import com.flylazo.naru_acars.servlet.service.ACARS_Service;
import com.flylazo.naru_acars.servlet.service.socket.SocketError;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import static javax.swing.GroupLayout.Alignment.LEADING;
import static javax.swing.JOptionPane.*;

public class ACARS_Form extends PanelBase {
    private final ACARS_Service service;
    private final JComboBox<VirtualAirline> serverCombo;
    private final TextInput apiInput;

    public ACARS_Form(Window window) {
        super(window);

        this.apiInput = new TextInput("Paste the key of your VA account", 30, false);
        this.serverCombo = new JComboBox<>(VirtualAirline.values());
        this.service = window.getServiceFactory().getBean(ACARS_Service.class);
        var form = new JPanel();
        var formLayout = new GroupLayout(form);
        var hGroup = formLayout.createSequentialGroup();
        var vGroup = formLayout.createSequentialGroup();
        var boldFont = new Font("Ubuntu Medium", Font.PLAIN, 15);
        var vaLabel = window.bakeLabel("Virtual airline", boldFont, Color.black);
        var apiLabel = window.bakeLabel("API key", boldFont, Color.black);
        var connectBtn = new JButton("Connect");
        connectBtn.addActionListener(this::onClickConnect);

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

    private void onClickConnect(ActionEvent event) {
        var airline = (VirtualAirline) this.serverCombo.getSelectedItem();

        if (airline == null) {
            this.window.showDialog(WARNING_MESSAGE, "Select a virtual airline first.");
            return;
        }

        this.service.getConnector(airline)
                .withAPIKey(this.apiInput.getText())
                .whenError(this::alertError)
                .connect();
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
