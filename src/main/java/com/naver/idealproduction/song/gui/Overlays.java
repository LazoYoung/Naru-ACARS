package com.naver.idealproduction.song.gui;

import com.naver.idealproduction.song.SimOverlayNG;
import com.naver.idealproduction.song.entity.Overlay;
import com.naver.idealproduction.song.entity.repository.OverlayRepository;
import jakarta.annotation.Nullable;
import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.EnumProgress;
import me.friwi.jcefmaven.MavenCefAppHandlerAdapter;
import me.friwi.jcefmaven.impl.progress.ConsoleProgressHandler;
import org.cef.CefApp;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefMessageRouter;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

import static java.awt.Dialog.ModalityType.APPLICATION_MODAL;
import static java.lang.Short.MAX_VALUE;
import static java.util.logging.Level.SEVERE;
import static javax.swing.BoxLayout.Y_AXIS;

public class Overlays extends JPanel {
    private final Logger logger = Logger.getLogger(SimOverlayNG.class.getName());
    private final OverlayRepository repository;
    private final JComboBox<String> selector;
    private final JPanel overlayPane;
    private CefApp cefApp;
    private CefBrowser browser = null;

    public Overlays(Window window, OverlayRepository repository) {
        this.repository = repository;
        String[] items = repository.getAll()
                .stream()
                .map(Overlay::getName)
                .toArray(String[]::new);
        selector = new JComboBox<>(items);
        overlayPane = new JPanel(new GridLayout(1, 1));
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

        selector.setSelectedItem(items[0]);
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

        var borderLayout = new BoxLayout(this, Y_AXIS);
        setLayout(borderLayout);
        setBorder(BorderFactory.createTitledBorder("Overlay"));
        add(controlPane);
        add(overlayPane);

        final var tab = window.getContentTab();
        var item = (String) selector.getSelectedItem();
        Optional<Overlay> overlay = Optional.ofNullable(item).flatMap(repository::get);
        String url;

        if (overlay.isEmpty()) {
            url = SimOverlayNG.getWebURL("/404").toString();
        } else {
            var path = overlay.get().getPath();
            url = SimOverlayNG.getWebURL(path).toString();
        }
        invokeLater(() -> createBrowser(url));
        tab.addChangeListener(e -> {
            if (tab.getSelectedComponent().equals(this)) {
                updateBrowser(null);
            }
        });
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                invokeLater(() -> disposeBrowser());
            }
        });
        repository.addUpdateListener(() -> SwingUtilities.invokeLater(this::updateSelector));
    }

    private void invokeLater(Runnable run) {
        var thread = new Thread(() -> {
            try {
                run.run();
            } catch (Exception e) {
                logger.log(SEVERE, e.getMessage(), e);
            }
        });
        thread.start();
    }

    private void createBrowser(String url) {
        try {
            var builder = new CefAppBuilder();
            var dir = SimOverlayNG.getDirectory().resolve("jcef-bundle").toFile();
            builder.setInstallDir(dir);
            builder.setAppHandler(new MavenCefAppHandlerAdapter() {
                @Override
                public boolean onBeforeTerminate() {
                    return true;
                }
            });
            builder.setProgressHandler(new ConsoleProgressHandler() {
                @Override
                public void handleProgress(EnumProgress state, float percent) {
                    Objects.requireNonNull(state, "state cannot be null");
                    if (percent != -1f && (percent < 0f || percent > 100f)) {
                        throw new RuntimeException("percent has to be -1f or between 0f and 100f. Got " + percent + " instead");
                    }
                    String s = state.toString();
                    String state_ = Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
                    String percent_ = (percent == -1f) ? "" : (percent + "%");
                    logger.info(state_ + " cef browser... " + percent_);
                }
            });
            builder.getCefSettings().windowless_rendering_enabled = true;
            cefApp = builder.build();
            var cefClient = cefApp.createClient();
            var router = CefMessageRouter.create();
            cefClient.addMessageRouter(router);
            browser = cefClient.createBrowser(url, true, false);
            loadBrowser(browser.getUIComponent());
        } catch (Exception e) {
            logger.log(SEVERE, "Failed to load overlay viewer!", e);
            loadBrowser(null);
        }
    }

    private void loadBrowser(Component comp) {
        SwingUtilities.invokeLater(() -> {
            overlayPane.setBorder(BorderFactory.createLineBorder(Color.black));

            if (comp != null) {
                overlayPane.add(comp);
                comp.repaint();
            } else {
                var blank = new JPanel();
                blank.setBackground(Color.black);
                overlayPane.add(blank);
            }
        });
    }

    private void disposeBrowser() {
        CefApp.getInstance().dispose();
        cefApp.dispose();
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

        String url;
        var overlay = repository.get(overlayName);
        repository.select(overlayName);

        if (overlay.isEmpty()) {
            url = SimOverlayNG.getWebURL("/404").toString();
        } else {
            String path = overlay.get().getPath();
            url = SimOverlayNG.getWebURL(path).toString();
        }

        updateBrowser(url);
    }

    private void updateBrowser(@Nullable String url) {
        if (browser == null) {
            return;
        }
        if (url == null) {
            url = browser.getURL();
        }
        try {
            browser.loadURL(url);
            browser.getUIComponent().repaint();
        } catch (Exception e) {
            logger.log(SEVERE, "Failed to load: " + url, e);
        }
    }

    private void showURLDialog(Window window, Overlay overlay) {
        final var url = SimOverlayNG.getWebURL(overlay.getPath()).toString();
        final var dialog = new JDialog(window, "Overlay URL", APPLICATION_MODAL);
        var panel = new JPanel();
        var layout = new GroupLayout(panel);
        var message = new JLabel("Copy it into your OBS browser source.");
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
                    logger.log(SEVERE, "Failed to edit system clipboard.", ex);
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
        dialog.setResizable(false);
        dialog.setVisible(true);
    }
}
