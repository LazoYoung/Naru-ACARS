package com.flylazo.naru_acars.domain.acars;

public enum ServiceType {
    SCHEDULE("Scheduled flight"),
    CHARTER("Charter flight"),
    CARGO("Cargo flight");

    public final String text;

    ServiceType(String text) {
        this.text = text;
    }
}
