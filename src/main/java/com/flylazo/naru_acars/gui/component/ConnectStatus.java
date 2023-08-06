package com.flylazo.naru_acars.gui.component;

import java.awt.*;

public enum ConnectStatus {
    ONLINE("ONLINE", Color.BLACK, Color.GREEN),
    OFFLINE("OFFLINE", Color.WHITE, Color.RED);

    private final String text;
    private final Color foreground;
    private final Color background;

    ConnectStatus(String text, Color foreground, Color background) {
        this.text = text;
        this.foreground = foreground;
        this.background = background;
    }

    public String getText() {
        return text;
    }

    public Color getForegroundColor() {
        return foreground;
    }

    public Color getBackgroundColor() {
        return background;
    }
}
