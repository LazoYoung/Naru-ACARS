package com.naver.idealproduction.song.entity;

import com.fasterxml.jackson.annotation.JsonSetter;

public class Aircraft {
    private String icaoCode;
    private String name;
    private String registration;
    private int capacity;

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

    public String getIcaoCode() {
        return icaoCode;
    }

    public String getName() {
        return name;
    }

    public String getRegistration() {
        return registration;
    }

    public int getCapacity() {
        return capacity;
    }
}
