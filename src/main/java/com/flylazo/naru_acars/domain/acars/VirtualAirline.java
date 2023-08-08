package com.flylazo.naru_acars.domain.acars;

import java.net.URI;

public enum VirtualAirline {
    // todo assign proper uri when ready
    LOCAL("Localhost", URI.create("ws://127.0.0.1:6001/socket")),
    NARU_AIRLINE("Naru Airline", URI.create("")),
    ;

    private final String text;
    private final URI uri;

    VirtualAirline(String text, URI uri) {
        this.text = text;
        this.uri = uri;
    }

    @Override
    public String toString() {
        return text;
    }

    public URI getUri() {
        return uri;
    }
}
