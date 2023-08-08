package com.flylazo.naru_acars.gui.panel;

import com.flylazo.naru_acars.domain.acars.VirtualAirline;
import com.flylazo.naru_acars.gui.Window;
import com.flylazo.naru_acars.gui.component.TextInput;
import com.flylazo.naru_acars.servlet.service.ACARS_Service;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import static javax.swing.GroupLayout.Alignment.LEADING;

public class ACARS_Form extends PanelBase {
    private final ACARS_Service service;
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
        var vaCombo = new JComboBox<>(VirtualAirline.values());
        var apiInput = new TextInput("Paste the key of your VA account", 30, false);
        this.service = window.getServiceFactory().getBean(ACARS_Service.class);
        this.connectBtn = new JButton("Connect");
        this.connectBtn.addActionListener(this::onClickConnect);

        var hGlue = Box.createHorizontalGlue();
        hGroup.addContainerGap(20, 20)
                .addGroup(formLayout.createParallelGroup(LEADING, false)
                        .addComponent(vaLabel)
                        .addComponent(vaCombo)
                        .addComponent(apiLabel)
                        .addComponent(apiInput)
                        .addGroup(formLayout.createSequentialGroup()
                                .addComponent(hGlue)
                                .addComponent(this.connectBtn)
                        )
                )
                .addContainerGap(20, 20);
        vGroup.addContainerGap(20, 20)
                .addGroup(formLayout.createSequentialGroup()
                        .addComponent(vaLabel)
                        .addComponent(vaCombo)
                        .addGap(20)
                        .addComponent(apiLabel)
                        .addComponent(apiInput)
                        .addGap(40)
                        .addGroup(formLayout.createParallelGroup(LEADING, false)
                                .addComponent(hGlue)
                                .addComponent(this.connectBtn)
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
        // todo method stub
    }
}
