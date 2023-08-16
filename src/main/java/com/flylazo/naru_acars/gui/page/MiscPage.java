package com.flylazo.naru_acars.gui.page;

import com.flylazo.naru_acars.gui.Window;
import com.flylazo.naru_acars.gui.panel.SimvarMonitor;

import javax.swing.*;

public class MiscPage extends JSplitPane {

    public MiscPage(Window window) {
        var settingsPanel = new JPanel();
        var simvarMonitor = new SimvarMonitor(window);
        settingsPanel.setBorder(BorderFactory.createTitledBorder("Settings"));
        simvarMonitor.setBorder(BorderFactory.createTitledBorder("Simulator variables"));

        this.setTopComponent(settingsPanel);
        this.setBottomComponent(simvarMonitor);
        this.setResizeWeight(0.0);
        this.setDividerSize(0);
        this.setOrientation(JSplitPane.VERTICAL_SPLIT);
    }

}
