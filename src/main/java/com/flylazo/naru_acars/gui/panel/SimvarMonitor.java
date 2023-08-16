package com.flylazo.naru_acars.gui.panel;

import com.flylazo.naru_acars.NaruACARS;
import com.flylazo.naru_acars.domain.overlay.Simvar;
import com.flylazo.naru_acars.gui.Window;
import com.flylazo.naru_acars.servlet.service.SimDataService;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Short.MAX_VALUE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

public class SimvarMonitor extends PanelBase {

    private final SimDataService simDataService;

    public SimvarMonitor(Window window) {
        super(window);

        int count = 5;
        var labels = this.createLabels(count);
        var comboBoxes = this.createComboBoxes(count, labels);
        this.simDataService = window.getServiceFactory().getBean(SimDataService.class);
        var layout = new GroupLayout(this);

        var h1 = layout.createParallelGroup();
        var h2 = layout.createParallelGroup();
        var vGroup = layout.createSequentialGroup();

        for (int i = 0; i < count; i++) {
            var comboBox = comboBoxes.get(i);
            var label = labels.get(i);
            h1.addComponent(comboBox);
            h2.addComponent(label, PREFERRED_SIZE, PREFERRED_SIZE, MAX_VALUE);
            vGroup.addGroup(layout.createParallelGroup()
                    .addComponent(comboBox)
                    .addComponent(label));
        }

        var hGroup = layout.createSequentialGroup()
                .addGroup(h1)
                .addGroup(h2);

        for (int i = 0; i < count; i++) {
            var label = labels.get(i);
            var comboBox = comboBoxes.get(i);
            this.updateLabel(label, comboBox);
            this.simDataService.addUpdateListener(() -> {
                updateLabel(label, comboBox);
            });
            layout.linkSize(SwingConstants.VERTICAL, comboBox, label);
        }

        layout.setHorizontalGroup(hGroup);
        layout.setVerticalGroup(vGroup);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        this.setBorder(BorderFactory.createTitledBorder("Simulator Variables"));
        this.setLayout(layout);
    }

    private List<JLabel> createLabels(int count) {
        List<JLabel> list = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            var label = window.bakeLabel("N/A", Color.black);
            label.setBorder(BorderFactory.createLineBorder(Color.gray, 2));
            list.add(label);
        }
        return list;
    }

    private List<JComboBox<String>> createComboBoxes(int count, List<JLabel> labels) {
        List<JComboBox<String>> list = new ArrayList<>();
        var font = new Font("Ubuntu Medium", Font.PLAIN, 15);
        var items = Arrays.stream(Simvar.Type.values()).map(Simvar.Type::toString).toArray(String[]::new);

        for (int i = 0; i < count; i++) {
            var label = labels.get(i);
            var comboBox = new JComboBox<>(items);
            comboBox.setSelectedIndex(i);
            comboBox.setFont(font);
            comboBox.setMaximumSize(new Dimension(150, 50));
            comboBox.addActionListener(e -> updateLabel(label, comboBox));
            list.add(comboBox);
        }
        return list;
    }

    private void updateLabel(JLabel label, JComboBox<String> comboBox) {
        Simvar.Type varType;
        String item = (String) comboBox.getSelectedItem();

        try {
            varType = Simvar.Type.valueOf(item);
        } catch (IllegalArgumentException ex) {
            var logger = Logger.getLogger(NaruACARS.class.getName());
            logger.log(Level.SEVERE, "Unknown variable: " + item, ex);
            return;
        }

        Object o = simDataService.getVariable(varType);
        label.setText((o == null) ? "N/A" : String.valueOf(o));
    }

}
