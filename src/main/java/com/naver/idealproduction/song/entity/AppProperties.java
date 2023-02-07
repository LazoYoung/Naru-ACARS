package com.naver.idealproduction.song.entity;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naver.idealproduction.song.SimOverlayNG;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppProperties {
    private static final Logger logger = Logger.getLogger(SimOverlayNG.class.getName());
    private static final String fileName = "properties.json";
    private static final AppProperties defaultProps;
    private static File file = null;
    private int port;
    private String simbriefName;

    static {
        defaultProps = new AppProperties();
        defaultProps.setPort(8080);

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

    public static AppProperties getInstance() {
        try {
            var mapper = new ObjectMapper();
            return mapper.readValue(file, AppProperties.class);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to read " + fileName);
            return defaultProps;
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

    @JsonSetter("port")
    public void setPort(int port) {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("Invalid port range!");
        }
        this.port = port;
    }

    @JsonGetter("simbrief-name")
    public void setSimbriefName(String simbriefName) {
        this.simbriefName = simbriefName;
    }
}
