package com.flylazo.naru_acars.gui;

import com.flylazo.naru_acars.NaruACARS;
import com.flylazo.naru_acars.gui.page.ConsolePage;
import com.flylazo.naru_acars.gui.page.DashboardPage;
import com.flylazo.naru_acars.gui.page.OverlaysPage;
import com.flylazo.naru_acars.servlet.service.SimTracker;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
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
    private ConfigurableApplicationContext context;

    public Window() {
        try {
            var font = registerFonts("ubuntu-regular.ttf", 15f);
            var iconURL = getClass().getResource("/icon.png");
            registerFonts("ubuntu-medium.ttf", 15f);
            setDefaultFont(new FontUIResource(font));
            setIconImage(new ImageIcon(iconURL).getImage());
        } catch (IOException | FontFormatException e) {
            Logger logger = Logger.getLogger(NaruACARS.class.getName());
            logger.warning("Failed to register custom fonts.");
        }
    }

    public void start(
            ConsolePage consolePage,
            SimTracker simTracker,
            ConfigurableApplicationContext context
    ) {
        this.simTracker = simTracker;
        this.contentPane = new JTabbedPane();
        this.context = context;
        var dashboardPage = new DashboardPage(this);
        var overlayPage = new OverlaysPage(this);
        contentPane.addTab("Dashboard", dashboardPage);
        contentPane.addTab("Overlays", overlayPage);
        contentPane.addTab("Console", consolePage);

        final var window = this;
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                String title = "Terminate program";
                String message = "Confirm you want to exit?";
                int answer = JOptionPane.showConfirmDialog(window, message, title, YES_NO_OPTION, QUESTION_MESSAGE);

                if (answer == YES_OPTION) {
                    NaruACARS.exit(1);
                }
            }
        });

        setContentPane(contentPane);
        setResizable(false);
        setPreferredSize(new Dimension(800, 500));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setTitle("Naru ACARS");
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

    public BeanFactory getServiceFactory() {
        if (context == null) {
            throw new IllegalStateException("Window has not started.");
        }

        return context.getBeanFactory();
    }

    public CompoundBorder getMargin(JComponent comp, int top, int left, int bottom, int right) {
        Border border = comp.getBorder();
        Border margin = new EmptyBorder(top, left, bottom, right);
        return new CompoundBorder(border, margin);
    }

    public JLabel bakeLabel(String text, Font font, Color color) {
        var label = new JLabel(text, JLabel.CENTER);

        if (font != null) {
            label.setFont(font);
        }
        label.setAlignmentX(CENTER_ALIGNMENT);
        label.setForeground(color);
        return label;
    }

    public JLabel bakeLabel(String text, Color color) {
        return this.bakeLabel(text, null, color);
    }

    public JTabbedPane getContentTab() {
        return contentPane;
    }

    @Override
    public void dispose() {
        super.dispose();
        simTracker.getBridge().release();
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
