package com.naver.idealproduction.song.gui.panel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SimplePanel extends JPanel {
    public CompoundBorder getMargin(JComponent comp, int top, int left, int bottom, int right) {
        Border border = comp.getBorder();
        Border margin = new EmptyBorder(top, left, bottom, right);
        return new CompoundBorder(border, margin);
    }

    public JLabel bakeLabel(String text, Font font, Color color) {
        var label = new JLabel(text, JLabel.CENTER);
        label.setFont(font);
        label.setAlignmentX(CENTER_ALIGNMENT);
        label.setForeground(color);
        return label;
    }
}
