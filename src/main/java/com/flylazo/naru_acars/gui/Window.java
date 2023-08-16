package com.flylazo.naru_acars.gui;

import com.flylazo.naru_acars.NaruACARS;
import com.flylazo.naru_acars.gui.page.*;
import com.flylazo.naru_acars.servlet.service.SimTracker;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static java.awt.Font.TRUETYPE_FONT;
import static javax.swing.JOptionPane.*;

public class Window extends JFrame {
    private SimTracker simTracker;
    private JTabbedPane contentPane;
    private ConfigurableApplicationContext context;
    private Map<Class<?>, JComponent> pages;

    public Window() {
        try {
            this.pages = new HashMap<>();
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
        var dispatchPage = new DispatchPage(this);
        var acarsPage = new ACARS_Page(this);
        var overlayPage = new OverlaysPage(this);
        var miscPage = new MiscPage(this);
        this.pages.clear();
        this.pages.put(DispatchPage.class, dispatchPage);
        this.pages.put(ACARS_Page.class, acarsPage);
        this.pages.put(OverlaysPage.class, overlayPage);
        this.pages.put(MiscPage.class, miscPage);
        this.contentPane.addTab("Dispatch", dispatchPage);
        this.contentPane.addTab("ACARS", acarsPage);
        this.contentPane.addTab("Overlays", overlayPage);
        this.contentPane.addTab("Console", consolePage);
        this.contentPane.addTab("Misc", miscPage);

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

    public void selectPage(Class<?> clazz) {
        JComponent comp = this.pages.get(clazz);

        if (comp != null) {
            this.contentPane.setSelectedComponent(comp);
        }
    }

    /**
     * @param type Magic value: JOptionPane.XXX_MESSAGE
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
            throw new IllegalStateException("Application has not started.");
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

    public Border getAmberBorder() {
        return BorderFactory.createLineBorder(Color.red, 1);
    }

    public Border getDefaultBorder(JComponent component) {
        String type = component.getClass().getSimpleName();

        if (type.equals("TextInput")) {
            type = "TextField";
        } else if (type.startsWith("J")) {
            type = type.substring(1);
        }

        return UIManager.getLookAndFeel().getDefaults().getBorder(type + ".border");
    }

    public void setDocumentFilter(Document doc, String regex, boolean uppercase) {
        var filter = new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (Pattern.compile(regex).matcher(text).find()) {
                    super.insertString(fb, offset, null, attrs);
                } else {
                    super.insertString(fb, offset, uppercase ? text.toUpperCase() : text, attrs);
                }
            }
        };
        var document = (AbstractDocument) doc;
        document.setDocumentFilter(filter);
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
