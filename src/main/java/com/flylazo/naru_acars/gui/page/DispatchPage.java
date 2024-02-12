package com.flylazo.naru_acars.gui.page;

import com.flylazo.naru_acars.gui.Window;
import com.flylazo.naru_acars.gui.panel.Dispatcher;
import com.flylazo.naru_acars.gui.panel.Dashboard;

import javax.swing.*;

public class DispatchPage extends JSplitPane {
    public DispatchPage(Window window) {
        super(HORIZONTAL_SPLIT);

        var simulatorBoard = new Dashboard(window, 20);
        var dispatcher = new Dispatcher(window, 20);

        setLeftComponent(simulatorBoard);
        setRightComponent(dispatcher);
        setDividerSize(0);
        setResizeWeight(0);
    }
}
