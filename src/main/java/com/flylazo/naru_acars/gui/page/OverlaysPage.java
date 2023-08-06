package com.flylazo.naru_acars.gui.page;

import com.flylazo.naru_acars.domain.overlay.Overlay;
import com.flylazo.naru_acars.NaruACARS;
import com.flylazo.naru_acars.gui.Window;
import com.flylazo.naru_acars.servlet.service.OverlayService;
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
import java.util.logging.Logger;

import static java.awt.Dialog.ModalityType.APPLICATION_MODAL;
import static java.lang.Short.MAX_VALUE;
import static java.util.logging.Level.*;
import static javax.swing.BoxLayout.Y_AXIS;
import static org.burningwave.core.assembler.StaticComponentContainer.Modules;

public class OverlaysPage extends JPanel {
    private final Logger logger = Logger.getLogger(NaruACARS.class.getName());
    private final String validURL = NaruACARS.getWebURL("/overlay").toString();
    private final String invalidURL = NaruACARS.getWebURL("/404").toString();
    private final Window window;
    private final OverlayService service;
    private final JComboBox<String> selector;
    private final JPanel overlayPane;
    private CefApp cefApp;
    private CefBrowser browser = null;

    public OverlaysPage(Window window) {
        this.window = window;
        this.service = window.getServiceFactory().getBean(OverlayService.class);
        var items = service.getOverlays()
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

        service.get(true).ifPresent(overlay -> selector.setSelectedItem(overlay.getName()));
        selector.setMinimumSize(new Dimension(150, 25));
        selector.setMaximumSize(new Dimension(150, 25));
        selector.addActionListener(this::onComboSelect);
        useBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                try {
                    showURLDialog(window);
                } catch (Exception ex) {
                    logger.log(SEVERE, ex.getMessage(), ex);
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
        invokeLater(this::createBrowser);
        tab.addChangeListener(e -> {
            if (browser != null && tab.getSelectedComponent().equals(this)) {
                updateBrowser(browser.getURL());
            }
        });
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                invokeLater(() -> disposeBrowser());
            }
        });
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

    private void createBrowser() {
        Modules.exportPackageToAllUnnamed("java.base", "java.lang");
        Modules.exportPackageToAllUnnamed("java.desktop", "sun.awt", "sun.java2d");

        try { // Mac OSX specific packages
            Modules.exportPackageToAllUnnamed("java.desktop", "sun.lwawt", "sun.lwawt.macosx");
        } catch (Exception ignored) {}

        try {
            var builder = new CefAppBuilder();
            var dir = NaruACARS.getDirectory().resolve("jcef-bundle").toFile();
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
            browser = cefClient.createBrowser(validURL, true, false);
            loadBrowser(browser.getUIComponent());
        } catch (Exception e) {
            logger.log(SEVERE, "Failed to load overlay viewer!", e);
            loadBrowser(null);
        }
    }

    private void loadBrowser(@Nullable Component comp) {
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

    @SuppressWarnings("unchecked")
    private void onComboSelect(ActionEvent event) {
        var comboBox = (JComboBox<String>) event.getSource();
        var overlayName = (String) comboBox.getSelectedItem();
        var overlay = service.getOverlays().stream()
                .filter(e -> e.getName().equals(overlayName))
                .findAny();

        if (browser == null) {
            window.showDialog(JOptionPane.INFORMATION_MESSAGE, "Viewer is still loading...");
        } else if (overlay.isPresent()) {
            service.select(overlay.get().getId());
            updateBrowser(validURL);
        } else {
            updateBrowser(invalidURL);
        }
    }

    private void updateBrowser(String url) {
        try {
            browser.loadURL(url);
            browser.getUIComponent().repaint();
        } catch (Exception e) {
            logger.log(SEVERE, "Failed to load: " + url, e);
        }
    }

    private void showURLDialog(Window window) {
        final var dialog = new JDialog(window, "Overlay URL", APPLICATION_MODAL);
        var panel = new JPanel();
        var layout = new GroupLayout(panel);
        var message = new JLabel("Copy it into your OBS browser source.");
        var urlField = new JTextField(validURL);
        var button = new JButton("Copy");
        var hGroup = layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup()
                                .addComponent(message))
                .addComponent(urlField)
                .addGroup(layout.createSequentialGroup()
                                .addContainerGap(0, MAX_VALUE)
                                .addComponent(button)
                                .addContainerGap(0, MAX_VALUE));
        var vGroup = layout.createSequentialGroup()
                .addComponent(message)
                .addComponent(urlField)
                .addComponent(button);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                try {
                    var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    var selection = new StringSelection(validURL);
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
