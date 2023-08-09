package com.flylazo.naru_acars.gui.page;

import com.flylazo.naru_acars.gui.Window;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.logging.*;

import static java.util.logging.Level.SEVERE;
import static javax.swing.JOptionPane.ERROR_MESSAGE;

public class ConsolePage extends JPanel {

    private static class LogHandler extends Handler {
        private final Window window;
        private final JTextArea textArea;
        private final ZoneId zoneId;

        private LogHandler(Window window, JTextArea textArea) {
            this.window = window;
            this.textArea = textArea;
            this.zoneId = ZoneId.systemDefault();
        }

        @Override
        public void publish(LogRecord record) {
            if (record == null) {
                return;
            }

            var time = record.getInstant()
                    .atZone(zoneId)
                    .truncatedTo(ChronoUnit.MILLIS)
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            var level = record.getLevel();
            var source = record.getSourceClassName();
            var t = record.getThrown();

            System.out.printf("%s  %s - %s : %s%n", time, level.getName(), source, record.getMessage());

            if (t != null) {
                t.printStackTrace();
            }

            try {
                String msg = getFormatter().format(record) + '\n';
                textArea.insert(msg, textArea.getCaretPosition());

                if (level.equals(SEVERE)) {
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

    public ConsolePage(Logger logger, Window window) {
        var layout = new GridLayout(1, 1);
        var textArea = new JTextArea();
        var handler = new LogHandler(window, textArea);
        var caret = (DefaultCaret) textArea.getCaret();

        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        caret.setBlinkRate(200);
        handler.setFormatter(new LogFormatter());
        textArea.setEditable(false);
        this.replaceLogHandler(logger, handler);
        this.setLayout(layout);
        this.setBorder(BorderFactory.createTitledBorder("Console"));
        this.add(new JScrollPane(textArea));
    }

    private void replaceLogHandler(Logger logger, Handler newHandler) {
        logger.setLevel(Level.ALL);

        for (var handler : logger.getHandlers()) {
            logger.removeHandler(handler);
        }

        logger.setUseParentHandlers(false);
        logger.addHandler(newHandler);
        newHandler.setLevel(Level.ALL);
    }
}
