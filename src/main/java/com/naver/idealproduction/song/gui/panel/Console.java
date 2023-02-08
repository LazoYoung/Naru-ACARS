package com.naver.idealproduction.song.gui.panel;

import com.naver.idealproduction.song.gui.Window;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;

import static java.util.logging.Level.SEVERE;
import static javax.swing.JOptionPane.ERROR_MESSAGE;

public class Console extends JPanel {

    private static class LogHandler extends Handler {
        private final Window window;
        private final JTextArea textArea;

        private LogHandler(Window window, JTextArea textArea) {
            this.window = window;
            this.textArea = textArea;
        }

        @Override
        public void publish(LogRecord record) {
            if (record == null) {
                return;
            }

            try {
                String msg = getFormatter().format(record) + '\n';
                Level level = record.getLevel();
                textArea.insert(msg, textArea.getCaretPosition());

                if (level.equals(SEVERE)) {
                    Throwable t = record.getThrown();
                    String rawMsg = record.getMessage();
                    if (t != null) {
                        window.showDialog(ERROR_MESSAGE, rawMsg + '\n' + t.getClass().getName());
                    } else {
                        window.showDialog(ERROR_MESSAGE, rawMsg);
                    }
                }
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

    private static class LogFormatter extends Formatter {
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
    }

    private final JTextArea textArea;

    public Console(Logger logger, Window window) {
        this.textArea = new JTextArea();
        LogHandler handler = new LogHandler(window, textArea);
        DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        caret.setBlinkRate(200);
        handler.setFormatter(new LogFormatter());
        handler.setLevel(Level.INFO);
        logger.addHandler(handler);
    }

    public JTextArea getTextArea() {
        return textArea;
    }
}
