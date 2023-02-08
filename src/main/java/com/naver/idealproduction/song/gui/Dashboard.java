package com.naver.idealproduction.song.gui;

import com.naver.idealproduction.song.SimTracker;
import com.naver.idealproduction.song.gui.panel.Console;
import com.naver.idealproduction.song.gui.panel.Dispatcher;
import com.naver.idealproduction.song.gui.panel.SimMonitor;

import javax.swing.*;

public class Dashboard extends JSplitPane {
    private final Console console;
    private final SimTracker simTracker;

    public Dashboard(Console console, SimTracker simTracker) {
        super(HORIZONTAL_SPLIT);

        this.console = console;
        this.simTracker = simTracker;
        var simMonitor = new SimMonitor(this);
        var dispatcher = new Dispatcher(this);

        setLeftComponent(simMonitor);
        setRightComponent(dispatcher);
        setDividerSize(0);
        setResizeWeight(0.0);
    }

    public Console getConsole() {
        return console;
    }

    public SimTracker getSimTracker() {
        return simTracker;
    }
}
