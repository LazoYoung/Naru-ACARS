package com.naver.idealproduction.song.view;

import com.naver.idealproduction.song.SimOverlayNG;
import com.naver.idealproduction.song.entity.Overlay;
import com.naver.idealproduction.song.repo.OverlayRepository;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OverlayPanel extends JPanel {

    private final Logger logger = Logger.getLogger(SimOverlayNG.class.getName());
    private final OverlayRepository repository;
    private final JEditorPane overlayView;
    private final JComboBox<String> selector;

    public OverlayPanel(OverlayRepository repository) {
        this.repository = repository;
        String[] items = repository.getAll()
                .stream()
                .map(Overlay::getName)
                .toArray(String[]::new);

        // todo implement useBtn
        selector = new JComboBox<>(items);
        overlayView = new JEditorPane();
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

        selector.setMaximumSize(new Dimension(80, 30));
        selector.addActionListener(this::onComboSelect);
        controlLayout.setHorizontalGroup(hGroup);
        controlLayout.setVerticalGroup(vGroup);
        controlPane.setLayout(controlLayout);
        controlPane.add(Box.createHorizontalStrut(20));
        controlPane.add(selector);
        controlPane.add(Box.createHorizontalGlue());
        controlPane.add(useBtn);
        controlPane.add(Box.createHorizontalStrut(20));

        var overlayPane = new JPanel(new GridLayout(1, 1));
        var item = (String) selector.getSelectedItem();
        Optional<Overlay> overlay = Optional.ofNullable(item).flatMap(repository::get);
        overlayView.setEditable(false);
        overlayView.setContentType("text/html");

        try {
            if (overlay.isEmpty()) {
                overlayView.setPage(SimOverlayNG.getWebURL("/404"));
            } else {
                var path = overlay.get().getPath();
                overlayView.setPage(SimOverlayNG.getWebURL(path));
                logger.info("New overlay selected: " + item);
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }

        overlayPane.setBorder(BorderFactory.createLineBorder(Color.black));
        overlayPane.add(overlayView);

        var borderLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
        setLayout(borderLayout);
        setBorder(BorderFactory.createTitledBorder("Overlay"));
        add(controlPane);
        add(overlayPane);

        repository.addUpdateListener(() -> SwingUtilities.invokeLater(this::updateSelector));
    }

    private void updateSelector() {
        selector.removeAllItems();

        for (var overlay : repository.getAll()) {
            selector.addItem(overlay.getName());
        }
    }

    @SuppressWarnings("unchecked")
    private void onComboSelect(ActionEvent event) {
        var comboBox = (JComboBox<String>) event.getSource();
        var overlayName = (String) comboBox.getSelectedItem();

        if (overlayName == null) {
            return;
        }

        var overlay = repository.get(overlayName);

        repository.select(overlayName);
        try {
            if (overlay.isEmpty()) {
                overlayView.setPage(SimOverlayNG.getWebURL("/404"));
            } else {
                String path = overlay.get().getPath();
                overlayView.setPage(SimOverlayNG.getWebURL(path));
                logger.info("New overlay selected: " + overlayName);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
