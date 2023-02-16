package com.naver.idealproduction.song.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.annotation.Nonnull;

import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Aircraft {
    private String icaoCode = "";
    private String name = "";

    @JsonSetter("icao_code")
    public void setIcaoCode(String icaoCode) {
        this.icaoCode = icaoCode;
    }

    @JsonSetter("name")
    public void setName(String name) {
        this.name = name;
    }

    @Nonnull
    public String getIcaoCode() {
        return Optional.ofNullable(icaoCode).orElse("");
    }

    @Nonnull
    public String getName() {
        return Optional.ofNullable(name).orElse("");
    }
}
