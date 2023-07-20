package com.flylazo.naru_acars.gui.panel;

import com.flylazo.naru_acars.gui.Window;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public abstract class Board extends PanelBase {
    private final List<JLabel> labelList = new ArrayList<>();
    private final List<JLabel> valueList = new ArrayList<>();
    private final List<String> labelTextList = new ArrayList<>();
    private final List<String> valueTextList = new ArrayList<>();
    private final JLabel titleLabel;
    private final JLabel offlineLabel;
    private final Font labelFont = new Font("Ubuntu Medium", Font.PLAIN, 18);
    private boolean isConnected = false;

    protected Board(Window window) {
        super(window);

        var stateFont = new Font("Ubuntu Medium", Font.PLAIN, 24);
        titleLabel = window.bakeLabel("No title", stateFont, Color.white);
        titleLabel.setBackground(Color.red);
        titleLabel.setOpaque(true);
        titleLabel.setBorder(window.getMargin(titleLabel, 10, 10, 10, 10));
        offlineLabel = window.bakeLabel("Offline", labelFont, Color.black);
    }

    protected abstract String getTitle();

    protected abstract String getOfflineText();

    protected boolean isConnected() {
        return isConnected;
    }

    protected void setConnected(boolean value) {
        isConnected = value;
    }

    /**
     * @return index of the new label
     */
    protected int addLabel(String defLabel, String defValue) {
        var valueFont = new Font("Ubuntu Regular", Font.PLAIN, 16);
        var label = window.bakeLabel(defLabel, labelFont, Color.gray);
        var value = window.bakeLabel(defValue, valueFont, Color.black);
        labelList.add(label);
        valueList.add(value);
        labelTextList.add(label.getText());
        valueTextList.add(value.getText());
        return labelList.size() - 1;
    }

    protected void setValue(int index, String text) {
        valueTextList.set(index, text);
    }

    protected void updateContentPane(boolean draw) {
        if (isConnected) {
            for (int i = 0; i < labelList.size(); i++) {
                var label = labelTextList.get(i);
                var value = valueTextList.get(i);
                labelList.get(i).setText(label);
                valueList.get(i).setText(value);
            }
            titleLabel.setBackground(Color.green);
        } else {
            titleLabel.setBackground(Color.red);
        }

        if (draw) {
            removeAll();
            titleLabel.setText(getTitle());
            add(Box.createRigidArea(new Dimension(0, 20)));
            add(titleLabel);

            if (isConnected) {
                for (int i = 0; i < labelList.size(); i++) {
                    add(Box.createVerticalGlue());
                    add(labelList.get(i));
                    add(valueList.get(i));
                }
                add(Box.createVerticalGlue());
            } else {
                offlineLabel.setText(getOfflineText());
                add(Box.createVerticalStrut(120));
                add(offlineLabel);
            }

            revalidate();
            repaint();
        }
    }
}
