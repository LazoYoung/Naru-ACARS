package com.naver.idealproduction.song.view;

import com.naver.idealproduction.song.SimOverlayNG;
import com.naver.idealproduction.song.entity.Overlay;
import com.naver.idealproduction.song.repo.OverlayRepository;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.awt.Dialog.ModalityType.APPLICATION_MODAL;
import static java.lang.Short.MAX_VALUE;
import static javax.swing.BoxLayout.Y_AXIS;

public class OverlayPanel extends JPanel {

    private final Logger logger = Logger.getLogger(SimOverlayNG.class.getName());
    private final OverlayRepository repository;
    private final JEditorPane overlayView;
    private final JComboBox<String> selector;

    public OverlayPanel(Window window, OverlayRepository repository) {
        this.repository = repository;
        String[] items = repository.getAll()
                .stream()
                .map(Overlay::getName)
                .toArray(String[]::new);

        selector = new JComboBox<>(items);
        overlayView = new JEditorPane();
        var useBtn = new JButton("Use overlay");
        var controlPane = new JPanel();
        var controlLayout = new GroupLayout(controlPane);
        var hGroup = controlLayout.createSequentialGroup()
                .addContainerGap(20, 20)
                .addComponent(selector)
                .addGap(20, MAX_VALUE, MAX_VALUE)
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
        useBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Optional<Overlay> selected = repository.getSelected();

                if (selected.isEmpty()) {
                    window.showDialog(JOptionPane.WARNING_MESSAGE, "Please select an overlay.");
                } else {
                    showURLDialog(window, selected.get());
                }
            }
        });
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
                logger.info("Selected overlay: " + item);
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }

        overlayPane.setBorder(BorderFactory.createLineBorder(Color.black));
        overlayPane.add(overlayView);

        var borderLayout = new BoxLayout(this, Y_AXIS);
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
                logger.info("Selected overlay: " + overlayName);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void showURLDialog(Window window, Overlay overlay) {
        final var url = SimOverlayNG.getWebURL(overlay.getPath()).toString();
        final var dialog = new JDialog(window, "Overlay URL", APPLICATION_MODAL);
        var panel = new JPanel();
        var layout = new GroupLayout(panel);
        var message = new JLabel("Copy this URL into your OBS browser source.");
        var urlField = new JTextField(url);
        var button = new JButton("Copy");

        var hGroup = layout.createParallelGroup()
                .addGroup(
                        layout.createSequentialGroup()
                                .addComponent(message)
                )
                .addComponent(urlField)
                .addGroup(
                        layout.createSequentialGroup()
                                .addContainerGap(0, MAX_VALUE)
                                .addComponent(button)
                                .addContainerGap(0, MAX_VALUE)
                );

        var vGroup = layout.createSequentialGroup()
                .addComponent(message)
                .addComponent(urlField)
                .addComponent(button);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                try {
                    var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    var selection = new StringSelection(url);
                    clipboard.setContents(selection, selection);
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "Failed to edit system clipboard.", ex);
                }
                dialog.dispose();
            }
        });
        urlField.setEditable(false);
        layout.setHorizontalGroup(hGroup);
        layout.setVerticalGroup(vGroup);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        panel.setLayout(layout);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(window);
        dialog.setVisible(true);
    }
}
