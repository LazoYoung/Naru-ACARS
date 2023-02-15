package com.naver.idealproduction.song.gui.panel;

import com.naver.idealproduction.song.SimOverlayNG;
import com.naver.idealproduction.song.entity.unit.Simvar;
import com.naver.idealproduction.song.service.SimDataService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Short.MAX_VALUE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

public class SimvarMonitor extends SimplePanel {

    private final Font font =  new Font("Monospaced", Font.PLAIN, 17);
    private final JLabel label = bakeLabel("N/A", font, Color.yellow);
    private final SimDataService simDataService;

    public SimvarMonitor(SimDataService simDataService) {
        this.simDataService = simDataService;
        var items = Arrays.stream(Simvar.values()).map(Simvar::toString).toArray(String[]::new);
        var comboBox = new JComboBox<>(items);

        var layout = new GroupLayout(this);
        var hGroup = layout.createSequentialGroup()
                .addComponent(comboBox)
                .addComponent(label, PREFERRED_SIZE, PREFERRED_SIZE, MAX_VALUE);
        var vGroup = layout.createParallelGroup()
                .addComponent(comboBox)
                .addComponent(label);

        comboBox.setMaximumSize(new Dimension(150, 50));
        comboBox.addActionListener(this::onComboSelect);
        label.setOpaque(true);
        label.setBackground(Color.gray);
        label.setHorizontalTextPosition(SwingConstants.CENTER);
        updateLabel(comboBox);
        layout.setHorizontalGroup(hGroup);
        layout.setVerticalGroup(vGroup);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        this.setBorder(BorderFactory.createTitledBorder("Simulator Variables"));
        this.setLayout(layout);
    }

    @SuppressWarnings("unchecked")
    private void onComboSelect(ActionEvent e) {
        updateLabel((JComboBox<String>) e.getSource());
    }

    // todo Text won't refresh
    private void updateLabel(JComboBox<String> comboBox) {
        String item = (String) comboBox.getSelectedItem();
        Simvar simvar;

        try {
            simvar = Simvar.valueOf(item);
        } catch (IllegalArgumentException ex) {
            var logger = Logger.getLogger(SimOverlayNG.class.getName());
            logger.log(Level.SEVERE, "Unknown variable: " + item, ex);
            return;
        }

        Object o = simDataService.getVariable(simvar);
        label.setText((o == null) ? "N/A" : String.valueOf(o));
        label.setForeground((o == null) ? Color.yellow : Color.white);
    }

}
