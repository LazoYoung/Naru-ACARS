package com.naver.idealproduction.song.view;

import com.naver.idealproduction.song.SimMonitor;
import com.naver.idealproduction.song.repo.OverlayRepository;

import javax.swing.*;
import java.awt.*;

import static javax.swing.JOptionPane.*;

public class Window extends JFrame {

    private final SimMonitor simMonitor;

    public Window(ConsoleHandlerNG consoleHandler, SimMonitor simMonitor, OverlayRepository overlayRepository) {
        this.simMonitor = simMonitor;

        setResizable(false);
        setPreferredSize(new Dimension(800, 500));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setTitle("SimOverlayNG");

        var contentPane = new JTabbedPane();
        var dashboard = new Dashboard(consoleHandler, simMonitor);
        var overlayPanel = new OverlayPanel(overlayRepository);
        contentPane.addTab("Dashboard", dashboard);
        contentPane.addTab("Overlay", overlayPanel);
        setContentPane(contentPane);
        pack();
    }

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
        simMonitor.terminate();
    }
}