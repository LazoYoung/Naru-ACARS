package com.naver.idealproduction.song.gui;

import com.naver.idealproduction.song.gui.subpanel.Dispatcher;
import com.naver.idealproduction.song.gui.subpanel.SimMonitor;
import com.naver.idealproduction.song.gui.subpanel.SimvarMonitor;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;

public class Dashboard extends JSplitPane {
    private final ConfigurableApplicationContext context;

    public Dashboard(ConfigurableApplicationContext context) {
        super(HORIZONTAL_SPLIT);
        this.context = context;
        var simMonitor = new SimMonitor(this);
        var dispatcher = new Dispatcher(this);
        var simvarMonitor = new SimvarMonitor(this);
        var rPanel = new JPanel();

        rPanel.setLayout(new BoxLayout(rPanel, BoxLayout.Y_AXIS));
        rPanel.add(dispatcher);
        rPanel.add(simvarMonitor);
        setLeftComponent(simMonitor);
        setRightComponent(rPanel);
        setDividerSize(0);
        setResizeWeight(0.0);
    }

    public ConfigurableApplicationContext getSpringContext() {
        return context;
    }
}
