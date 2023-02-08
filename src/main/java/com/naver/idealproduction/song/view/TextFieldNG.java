package com.naver.idealproduction.song.view;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class TextFieldNG extends JTextField implements FocusListener {

    private boolean isHint = true;
    private final String hint;

    public TextFieldNG(String hint, int column, boolean uppercase) {
        super(hint);
        this.hint = hint;
        setColumns(column);
        setForeground(Color.gray);
        addFocusListener(this);

        if (uppercase) {
            var doc = (AbstractDocument) getDocument();
            doc.setDocumentFilter(getFilter());
        }
    }

    public TextFieldNG(int column, boolean uppercase) {
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
        isHint = false;
        setForeground(Color.black);
        super.setText(t);
    }

    @Override
    public void focusGained(FocusEvent e) {
        if (isHint) {
            isHint = false;
            setText("");
            setForeground(Color.black);
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        String text = super.getText();

        if (text == null || text.isEmpty()) {
            isHint = true;
            setText(hint);
            setForeground(Color.gray);
        }
    }
}
