package com.flylazo.naru_acars.gui.page;

import com.flylazo.naru_acars.gui.Window;
import com.flylazo.naru_acars.gui.panel.ACARS_Form;
import com.flylazo.naru_acars.gui.panel.Dashboard;

import javax.swing.*;

public class ACARS_Page extends JSplitPane {
    public ACARS_Page(Window window) {
        super(HORIZONTAL_SPLIT);

        var board = new Dashboard(window, 20);
        var form = new ACARS_Form(window);
        setLeftComponent(board);
        setRightComponent(form);
        setDividerSize(0);
        setResizeWeight(0);
    }
}
