package com.naver.idealproduction.song.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.annotation.Nonnull;

import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Aircraft {
    private String icaoCode = "";
    private String name = "";
    private String registration = "";
    private int capacity = 0;

    @JsonSetter("icao_code")
    public void setIcaoCode(String icaoCode) {
        this.icaoCode = icaoCode;
    }

    @JsonSetter("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonSetter("reg")
    public void setRegistration(String registration) {
        this.registration = registration;
    }

    @JsonSetter("max_passengers")
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    @Nonnull
    public String getIcaoCode() {
        return Optional.ofNullable(icaoCode).orElse("");
    }

    @Nonnull
    public String getName() {
        return Optional.ofNullable(name).orElse("");
    }

    @Nonnull
    public String getRegistration() {
        return Optional.ofNullable(registration).orElse("");
    }

    public int getCapacity() {
        return capacity;
    }
}
