package com.naver.idealproduction.song.view;

import com.naver.idealproduction.song.SimOverlayNG;
import com.naver.idealproduction.song.entity.Overlay;
import com.naver.idealproduction.song.repo.OverlayRepository;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.logging.Logger;

public class OverlayPanel extends JPanel {

    public OverlayPanel(OverlayRepository repository) {
        var borderLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
        String[] items = repository.getAll()
                .stream()
                .map(Overlay::getName)
                .toArray(String[]::new);

        var selector = new JComboBox<>(items);
        var useBtn = new JButton("Use overlay");
        var controlPane = new JPanel();
        var controlLayout = new GroupLayout(controlPane);
        var hGroup = controlLayout.createSequentialGroup()
                .addContainerGap(20, 20)
                .addComponent(selector)
                .addGap(20, Short.MAX_VALUE, Short.MAX_VALUE)
                .addComponent(useBtn)
                .addContainerGap(20, 20);
        var vGroup = controlLayout.createSequentialGroup()
                .addGap(5)
                .addGroup(controlLayout.createParallelGroup()
                        .addComponent(selector)
                        .addComponent(useBtn))
                .addGap(10);
        selector.setMaximumSize(new Dimension(60, 30));
        controlLayout.setHorizontalGroup(hGroup);
        controlLayout.setVerticalGroup(vGroup);
        controlPane.setLayout(controlLayout);
        controlPane.add(Box.createHorizontalStrut(20));
        controlPane.add(selector);
        controlPane.add(Box.createHorizontalGlue());
        controlPane.add(useBtn);
        controlPane.add(Box.createHorizontalStrut(20));

        var overlayPane = new JPanel(new GridLayout(1, 1));
        var overlayView = new JEditorPane();
        overlayView.setEditable(false);
        overlayView.setContentType("text/html");
        try {
            var host = SimOverlayNG.getHost();
            var port = SimOverlayNG.getSystemPort();
            overlayView.setPage(String.format("http://%s:%s", host, port));
        } catch (IOException e) {
            Logger logger = Logger.getLogger(SimOverlayNG.class.getName());
            logger.warning("Failed to access url: " + overlayView.getPage().toString());
        }
        overlayPane.setBorder(BorderFactory.createLineBorder(Color.black));
        overlayPane.add(overlayView);

        setLayout(borderLayout);
        setBorder(BorderFactory.createTitledBorder("Overlay"));
        add(controlPane);
        add(overlayPane);
    }

}
