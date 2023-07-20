package com.flylazo.naru_acars.gui.page;

import com.flylazo.naru_acars.gui.Window;
import com.flylazo.naru_acars.gui.panel.Dispatcher;
import com.flylazo.naru_acars.gui.panel.SimulatorBoard;
import com.flylazo.naru_acars.gui.panel.SimvarMonitor;

import javax.swing.*;

public class DashboardPage extends JSplitPane {
    public DashboardPage(Window window) {
        super(HORIZONTAL_SPLIT);

        var simulatorBoard = new SimulatorBoard(window);
        var dispatcher = new Dispatcher(window);
        var simvarMonitor = new SimvarMonitor(window);
        var rightPanel = new JPanel();

        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.add(dispatcher);
        rightPanel.add(simvarMonitor);
        setLeftComponent(simulatorBoard);
        setRightComponent(rightPanel);
        setDividerSize(0);
        setResizeWeight(0);
    }
}
