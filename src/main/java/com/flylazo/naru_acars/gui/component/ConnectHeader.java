package com.flylazo.naru_acars.gui.component;

import java.awt.*;

public class ConnectHeader extends Header {
    private final Font font;
    private ConnectStatus status = ConnectStatus.OFFLINE;

    public ConnectHeader(Font titleFont, Font badgeFont, String title) {
        super(titleFont, title);
        this.font = badgeFont;
    }

    public void setStatus(ConnectStatus status) {
        this.status = status;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        var g2d = (Graphics2D) g;
        var statusText = this.status.getText();
        var statusFontMetrics = g2d.getFontMetrics(this.font);
        var statusLineMetrics = super.getLineMetrics(g2d, this.font, statusText);
        var titleFontMetrics = g2d.getFontMetrics(super.titleFont);
        float statusAscent = statusLineMetrics.getAscent();
        float statusDescent = statusLineMetrics.getDescent();

        int xPad = 5;
        int yPad = 2;
        int statusWidth = statusFontMetrics.stringWidth(statusText);
        int titleWidth = titleFontMetrics.stringWidth(super.title);
        int cellWidth = statusWidth + 2 * xPad;
        int cellHeight = Math.round(statusAscent + statusDescent) + 2 * yPad;
        int xMax = getWidth();
        super.width = titleWidth + statusWidth + cellWidth + xPad;

        g2d.setColor(this.status.getBackgroundColor());
        g2d.fillRoundRect(xMax - cellWidth, 0, cellWidth, cellHeight, cellHeight, cellHeight);
        g2d.setColor(this.status.getForegroundColor());
        g2d.setFont(this.font);
        g2d.drawString(statusText, xMax - xPad - statusWidth, statusAscent + yPad);
        super.updateSize();
    }
}
