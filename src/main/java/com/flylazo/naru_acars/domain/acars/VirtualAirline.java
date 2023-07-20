package com.flylazo.naru_acars.domain.acars;

public enum VirtualAirline {
    NARU_AIRLINE("Naru Airline");

    private final String text;

    VirtualAirline(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
