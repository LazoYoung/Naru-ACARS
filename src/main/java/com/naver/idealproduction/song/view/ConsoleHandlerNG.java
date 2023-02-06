package com.naver.idealproduction.song.view;

import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;

public class ConsoleHandlerNG extends Handler {

    private final JTextArea textArea;

    public ConsoleHandlerNG(Logger logger) {
        this.textArea = new JTextArea();
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
                Throwable thrown = record.getThrown();
                var builder = new StringBuilder(String.format("[%s] %s %s - %s", level, date, time, record.getMessage()));

                if (thrown != null) {
                    builder.append('\n').append(ExceptionUtils.getStackTrace(thrown));
                }
                return builder.toString();
            }
        });
        setLevel(Level.INFO);
        logger.addHandler(this);
    }

    public JTextArea getTextArea() {
        return textArea;
    }

    @Override
    public void publish(LogRecord record) {
        if (record == null) {
            return;
        }

        try {
            String msg = getFormatter().format(record) + '\n';
            textArea.insert(msg, textArea.getCaretPosition());
        } catch (Exception ignored) {}
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
