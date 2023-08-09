package com.flylazo.naru_acars.gui.panel;

import com.flylazo.naru_acars.gui.Window;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PanelBase extends JPanel {
    protected final Window window;

    public PanelBase(Window window) {
        this.window = window;
    }

    protected void setButtonAction(JButton button, Runnable callback) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getComponent().isEnabled()) {
                    callback.run();
                }
            }
        });
    }
}
