package com.naver.idealproduction.song.gui;

import com.naver.idealproduction.song.service.SimTracker;
import com.naver.idealproduction.song.gui.panel.Console;
import com.naver.idealproduction.song.service.OverlayService;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;
import java.awt.*;

import static javax.swing.JOptionPane.*;

public class Window extends JFrame {
    private SimTracker simTracker;
    private JTabbedPane contentPane;

    public void start(
            Console console,
            SimTracker simTracker,
            ConfigurableApplicationContext context
    ) {
        this.simTracker = simTracker;

        setResizable(false);
        setPreferredSize(new Dimension(800, 500));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("SimOverlayNG");

        contentPane = new JTabbedPane();
        var dashboard = new Dashboard(console, simTracker, context);
        var overlayService = context.getBean(OverlayService.class);
        var overlayPanel = new Overlays(this, overlayService);
        contentPane.addTab("Dashboard", dashboard);
        contentPane.addTab("Overlays", overlayPanel);
        setContentPane(contentPane);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * @param type Magic value: JOptionPane.XXX
     * @param message Message to display
     */
    public void showDialog(int type, String message) {
        String title = "Message";
        switch (type) {
            case PLAIN_MESSAGE,
                    INFORMATION_MESSAGE,
                    QUESTION_MESSAGE -> title = "Message";
            case WARNING_MESSAGE -> title = "Warning";
            case ERROR_MESSAGE -> title = "Error";
        }
        JOptionPane.showMessageDialog(this, message, title, type);
    }

    public JTabbedPane getContentTab() {
        return contentPane;
    }

    @Override
    public void dispose() {
        super.dispose();
        simTracker.terminate();
    }
}
