package com.flylazo.naru_acars.domain;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flylazo.naru_acars.NaruACARS;
import com.flylazo.naru_acars.domain.acars.VirtualAirline;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Properties {
    private static final Logger logger = Logger.getLogger(NaruACARS.class.getName());
    private static final String fileName = "properties.json";
    private static final Properties defaultProps = new Properties();
    private static File file = null;
    private int port = 8080;
    private String simbriefId = "";
    private String overlay = null;
    private String acarsAPI = null;
    private int virtualAirline = VirtualAirline.LOCAL.getId();

    static {
        try {
            var path = NaruACARS.getDirectory().resolve(fileName);
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

    public void save() {
        try {
            var mapper = new ObjectMapper();
            mapper.writeValue(file, this);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to read " + fileName, e);
        }
    }

    @JsonGetter("port")
    public int getPort() {
        return port;
    }

    @JsonGetter("simbrief-id")
    public String getSimbriefId() {
        return simbriefId;
    }

    @JsonGetter("overlay")
    public String getOverlay() {
        return overlay;
    }

    @JsonGetter("acars-api")
    public String getAcarsAPI() {
        return acarsAPI;
    }

    @JsonGetter("virtual-airline")
    public int getVirtualAirline() {
        return virtualAirline;
    }

    @JsonSetter("port")
    public void setPort(int port) {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("Invalid port range!");
        }
        this.port = port;
    }

    @JsonSetter("simbrief-id")
    public void setSimbriefId(String simbriefId) {
        this.simbriefId = simbriefId;
    }

    @JsonSetter("overlay")
    public void setOverlay(String overlay) {
        this.overlay = overlay;
    }

    @JsonSetter("acars-api")
    public void setAcarsAPI(String key) {
        this.acarsAPI = key;
    }

    @JsonSetter("virtual-airline")
    public void setVirtualAirline(int id) {
        this.virtualAirline = id;
    }
}
