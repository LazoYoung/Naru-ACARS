package com.flylazo.naru_acars.gui.component;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class TextInput extends JTextField implements FocusListener {

    private boolean isHint;
    private final String hint;

    public TextInput(String hint, int column, boolean uppercase) {
        super();
        super.setColumns(column);
        super.setText(hint);
        this.isHint = (hint != null);
        this.hint = hint;
        setForeground(Color.gray);
        addFocusListener(this);

        if (uppercase) {
            var doc = (AbstractDocument) getDocument();
            doc.setDocumentFilter(getFilter());
        }
    }

    public TextInput(int column, boolean uppercase) {
        this(null, column, uppercase);
    }

    private DocumentFilter getFilter() {
        return new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                super.insertString(fb, offset, string.toUpperCase(), attr);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                super.replace(fb, offset, length, text.toUpperCase(), attrs);
            }
        };
    }

    @Override
    public String getText() {
        return isHint ? "" : super.getText();
    }

    @Override
    public void setText(String t) {
        if (hint != null) {
            isHint = false;
            setForeground(Color.black);
        }
        super.setText(t);
    }

    @Override
    public void focusGained(FocusEvent e) {
        if (isHint) {
            isHint = false;
            super.setText("");
            setForeground(Color.black);
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        if (hint != null) {
            String text = super.getText();

            if (text == null || text.isEmpty()) {
                isHint = true;
                super.setText(hint);
                setForeground(Color.gray);
            }
        }
    }
}
