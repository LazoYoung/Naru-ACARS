package com.naver.idealproduction.song.gui;

import com.naver.idealproduction.song.SimTracker;
import com.naver.idealproduction.song.entity.repository.OverlayRepository;
import com.naver.idealproduction.song.gui.panel.Console;

import javax.swing.*;
import java.awt.*;

import static javax.swing.JOptionPane.*;

public class Window extends JFrame {

    private SimTracker simTracker;

    public void start(
            Console console,
            SimTracker simTracker,
            OverlayRepository overlayRepository
    ) {
        this.simTracker = simTracker;

        setResizable(false);
        setPreferredSize(new Dimension(800, 500));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("SimOverlayNG");

        var contentPane = new JTabbedPane();
        var dashboard = new Dashboard(console, simTracker);
        var overlayPanel = new Overlays(this, overlayRepository);
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

    @Override
    public void dispose() {
        super.dispose();
        simTracker.terminate();
    }
}
