package com.naver.idealproduction.song;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class ConsoleHandlerNG extends Handler {

    private final JTextArea textArea;

    public ConsoleHandlerNG(JTextArea textArea) {
        this.textArea = textArea;
        DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        caret.setBlinkRate(200);
        setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                String level = record.getLevel().getName();
                LocalDateTime now = LocalDateTime.now();
                String date = now.format(DateTimeFormatter.ISO_DATE);
                String time = now.format(DateTimeFormatter.ofPattern("kk:mm:ss"));
                return String.format("[%s] %s %s - %s", level, date, time, record.getMessage());
            }
        });
        setLevel(Level.INFO);
    }

    @Override
    public void publish(LogRecord record) {
        if (record == null) {
            return;
        }

        try {
            String msg = getFormatter().format(record) + '\n';
            textArea.insert(msg, textArea.getCaretPosition());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void flush() {
        textArea.replaceRange(null, 0, textArea.getCaretPosition());
    }

    @Override
    public void close() throws SecurityException {
        flush();
    }
}
