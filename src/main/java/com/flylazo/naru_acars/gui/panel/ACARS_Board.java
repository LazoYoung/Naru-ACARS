package com.flylazo.naru_acars.gui.panel;

import com.flylazo.naru_acars.gui.Window;

import javax.swing.*;
import java.awt.*;

public class ACARS_Board extends Board {
    private final String ONLINE = "Connected";
    private final String OFFLINE = "Disconnected";
    private final String NOT_AVAIL = "N/A";
    private final int serverLabel;
    private final int phaseLabel;

    public ACARS_Board(Window window) {
        super(window);

        serverLabel = addLabel("VA Server", OFFLINE);
        phaseLabel = addLabel("Phase of Flight", NOT_AVAIL);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.white);
        setPreferredSize(new Dimension(250, 0));
        updateContentPane(true);
    }

    @Override
    protected String getTitle() {
        return "ACARS";
    }

    @Override
    protected String getOfflineText() {
        return "Disconnected";
    }
}
