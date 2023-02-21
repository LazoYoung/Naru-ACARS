package com.naver.idealproduction.song.gui;

import com.naver.idealproduction.song.SimOverlayNG;
import com.naver.idealproduction.song.servlet.service.OverlayService;
import com.naver.idealproduction.song.servlet.service.SimTracker;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.logging.Logger;

import static java.awt.Font.TRUETYPE_FONT;
import static javax.swing.JOptionPane.*;

public class Window extends JFrame {
    private SimTracker simTracker;
    private JTabbedPane contentPane;

    public Window() {
        try {
            var font = registerFonts("ubuntu-regular.ttf", 15f);
            registerFonts("ubuntu-medium.ttf", 15f);
            setDefaultFont(new FontUIResource(font));
        } catch (IOException | FontFormatException e) {
            Logger logger = Logger.getLogger(SimOverlayNG.class.getName());
            logger.warning("Failed to register custom fonts.");
        }
    }

    public void start(
            Console console,
            SimTracker simTracker,
            ConfigurableApplicationContext context
    ) {
        this.simTracker = simTracker;
        contentPane = new JTabbedPane();
        var dashboard = new Dashboard(context);
        var overlayPanel = new Overlays(this, context.getBean(OverlayService.class));
        contentPane.addTab("Dashboard", dashboard);
        contentPane.addTab("Overlays", overlayPanel);
        contentPane.addTab("Console", console);

        final var window = this;
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                String title = "Terminate program";
                String message = "Confirm you want to exit?";
                int answer = JOptionPane.showConfirmDialog(window, message, title, YES_NO_OPTION, QUESTION_MESSAGE);

                if (answer == YES_OPTION) {
                    SimOverlayNG.exit(1);
                }
            }
        });

        setContentPane(contentPane);
        setResizable(false);
        setPreferredSize(new Dimension(800, 500));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setTitle("SimOverlayNG");
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

    private Font registerFonts(String fileName, float size) throws IOException, FontFormatException {
        var resource = new ClassPathResource("static/font/" + fileName);
        var font = Font.createFont(TRUETYPE_FONT, resource.getInputStream())
                .deriveFont(size);
        var env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        env.registerFont(font);
        return font;
    }

    private void setDefaultFont(FontUIResource font) {
        var defaults = UIManager.getDefaults();

        for (var keys = defaults.keys(); keys.hasMoreElements();) {
            Object key = keys.nextElement();
            Object value = defaults.get(key);

            if (value instanceof FontUIResource) {
                defaults.put(key, font);
            }
        }
    }
}
