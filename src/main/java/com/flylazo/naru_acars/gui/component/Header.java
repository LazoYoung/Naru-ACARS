package com.flylazo.naru_acars.gui.component;

import javax.swing.*;
import java.awt.*;
import java.awt.font.LineMetrics;

import static java.awt.RenderingHints.*;

public class Header extends JComponent {

    protected final Font titleFont;
    protected String title;
    protected int width;
    protected int height;

    public Header(Font font, String title) {
        this.title = title;
        this.titleFont = font;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        var g2d = (Graphics2D) g;
        var fontMetrics = g.getFontMetrics();
        var lineMetrics = getLineMetrics(g2d, this.titleFont, this.title);
        float asc = lineMetrics.getAscent();
        float des = lineMetrics.getDescent();
        int yPad = 10;
        int yLine = Math.round(asc + des + yPad);
        this.width = fontMetrics.stringWidth(this.title);
        this.height = yLine + yPad;

        g2d.setColor(Color.black);
        g2d.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON);
        g2d.setFont(this.titleFont);
        g2d.drawString(this.title, 0, Math.round(asc));
        g2d.drawLine(0, yLine, getWidth(), yLine);
        this.updateSize();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(this.width, this.height);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    protected void updateSize() {
        var dimension = this.getPreferredSize();
        this.setMinimumSize(dimension);
        this.setMaximumSize(new Dimension(Short.MAX_VALUE, dimension.height));
        this.revalidate(); // inform layout of the size change
    }

    protected LineMetrics getLineMetrics(Graphics2D g, Font font, String string) {
        g.setFont(font);
        var context = g.getFontRenderContext();
        return font.getLineMetrics(string, context);
    }
}
