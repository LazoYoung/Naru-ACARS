package com.flylazo.naru_acars.gui.panel;

import com.flylazo.naru_acars.gui.Window;

import javax.swing.*;

public class PanelBase extends JPanel {
    protected final Window window;

    public PanelBase(Window window) {
        this.window = window;
    }

    protected void setButtonListener(JButton button, Runnable callback) {
        for (var listener : button.getActionListeners()) {
            button.removeActionListener(listener);
        }

        button.addActionListener(e -> callback.run());
    }
}
