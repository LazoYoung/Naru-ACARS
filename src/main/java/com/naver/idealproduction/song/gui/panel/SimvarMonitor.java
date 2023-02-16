package com.naver.idealproduction.song.gui.panel;

import com.naver.idealproduction.song.SimOverlayNG;
import com.naver.idealproduction.song.domain.unit.Simvar;
import com.naver.idealproduction.song.gui.Dashboard;
import com.naver.idealproduction.song.servlet.service.SimDataService;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Short.MAX_VALUE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

public class SimvarMonitor extends SimplePanel {

    private final JLabel label1 = bakeLabel("N/A", Color.black);
    private final JLabel label2 = bakeLabel("N/A", Color.black);
    private final SimDataService simDataService;

    public SimvarMonitor(Dashboard dashboard) {
        var context = dashboard.getSpringContext();
        this.simDataService = context.getBean(SimDataService.class);
        var items = Arrays.stream(Simvar.values()).map(Simvar::toString).toArray(String[]::new);
        final var comboBox1 = new JComboBox<>(items);
        final var comboBox2 = new JComboBox<>(items);
        var layout = new GroupLayout(this);
        var hGroup = layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                        .addComponent(comboBox1)
                        .addComponent(comboBox2))
                .addGroup(layout.createParallelGroup()
                                .addComponent(label1, PREFERRED_SIZE, PREFERRED_SIZE, MAX_VALUE)
                                .addComponent(label2, PREFERRED_SIZE, PREFERRED_SIZE, MAX_VALUE));
        var vGroup = layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                                .addComponent(comboBox1)
                                .addComponent(label1))
                .addGroup(layout.createParallelGroup()
                                .addComponent(comboBox2)
                                .addComponent(label2));
        var boldFont = new Font("Ubuntu Medium", Font.PLAIN, 15);

        comboBox1.setSelectedIndex(0);
        comboBox2.setSelectedIndex(1);
        comboBox1.setFont(boldFont);
        comboBox2.setFont(boldFont);
        comboBox1.setMaximumSize(new Dimension(150, 50));
        comboBox2.setMaximumSize(new Dimension(150, 50));
        comboBox1.addActionListener(e -> updateLabel(label1, comboBox1));
        comboBox2.addActionListener(e -> updateLabel(label2, comboBox2));
        label1.setBorder(BorderFactory.createLineBorder(Color.gray, 2));
        label2.setBorder(BorderFactory.createLineBorder(Color.gray, 2));
        updateLabel(label1, comboBox1);
        updateLabel(label2, comboBox2);
        layout.setHorizontalGroup(hGroup);
        layout.setVerticalGroup(vGroup);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        layout.linkSize(SwingConstants.VERTICAL, comboBox1, label1);
        layout.linkSize(SwingConstants.VERTICAL, comboBox2, label2);
        this.setBorder(BorderFactory.createTitledBorder("Simulator Variables"));
        this.setLayout(layout);

        simDataService.addUpdateListener(() -> {
            updateLabel(label1, comboBox1);
            updateLabel(label2, comboBox2);
        });
    }

    private void updateLabel(JLabel label, JComboBox<String> comboBox) {
        Simvar simvar;
        String item = (String) comboBox.getSelectedItem();

        try {
            simvar = Simvar.valueOf(item);
        } catch (IllegalArgumentException ex) {
            var logger = Logger.getLogger(SimOverlayNG.class.getName());
            logger.log(Level.SEVERE, "Unknown variable: " + item, ex);
            return;
        }

        Object o = simDataService.getVariable(simvar);
        label.setText((o == null) ? "N/A" : String.valueOf(o));
    }

}
