package com.flylazo.naru_acars.gui.panel;

import com.flylazo.naru_acars.domain.acars.VirtualAirline;
import com.flylazo.naru_acars.gui.Window;
import com.flylazo.naru_acars.gui.component.TextInput;

import javax.swing.*;
import java.awt.*;

import static javax.swing.GroupLayout.Alignment.LEADING;
import static javax.swing.LayoutStyle.ComponentPlacement.UNRELATED;

public class ACARS_Form extends PanelBase {
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
        var connectButton = new JButton("Connect");
        var hGlue = Box.createHorizontalGlue();

        hGroup.addContainerGap(20, 20)
                .addGroup(formLayout.createParallelGroup(LEADING, false)
                        .addComponent(vaLabel)
                        .addComponent(vaCombo)
                        .addComponent(apiLabel)
                        .addComponent(apiInput)
                        .addGroup(formLayout.createSequentialGroup()
                                .addComponent(hGlue)
                                .addComponent(connectButton)
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
                                .addComponent(connectButton)
                        )
                )
                .addContainerGap(20, 20);
        formLayout.setHorizontalGroup(hGroup);
        formLayout.setVerticalGroup(vGroup);
        form.setLayout(formLayout);
        this.add(form);
        this.setBorder(BorderFactory.createTitledBorder("Datalink form"));
    }
}
