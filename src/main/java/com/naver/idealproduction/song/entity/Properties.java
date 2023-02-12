package com.naver.idealproduction.song.entity;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naver.idealproduction.song.SimOverlayNG;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Properties {
    private static final Logger logger = Logger.getLogger(SimOverlayNG.class.getName());
    private static final String fileName = "properties.json";
    private static final Properties defaultProps = new Properties();
    private static File file = null;
    private int port = 8080;
    private String simbriefName = "";
    private String overlay = null;

    static {
        try {
            var path = SimOverlayNG.getDirectory().resolve(fileName);
            file = path.toFile();

            if (!file.isFile()) {
                var mapper = new ObjectMapper();
                mapper.writeValue(file, defaultProps);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to write " + fileName, e);
        }
    }

    public static Properties read() {
        try {
            var mapper = new ObjectMapper();
            return mapper.readValue(file, Properties.class);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to read " + fileName, e);
            return defaultProps;
        }
    }

    public boolean save() {
        try {
            var mapper = new ObjectMapper();
            mapper.writeValue(file, this);
            return true;
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to read " + fileName, e);
            return false;
        }
    }

    @JsonGetter("port")
    public int getPort() {
        return port;
    }

    @JsonGetter("simbrief-name")
    public String getSimbriefName() {
        return simbriefName;
    }

    @JsonGetter("overlay")
    public String getOverlay() {
        return overlay;
    }

    @JsonSetter("port")
    public void setPort(int port) {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("Invalid port range!");
        }
        this.port = port;
    }

    @JsonSetter("simbrief-name")
    public void setSimbriefName(String simbriefName) {
        this.simbriefName = simbriefName;
    }

    @JsonSetter("overlay")
    public void setOverlay(String overlay) {
        this.overlay = overlay;
    }
}
