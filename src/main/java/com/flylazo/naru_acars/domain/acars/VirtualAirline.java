package com.flylazo.naru_acars.domain.acars;

import java.net.URI;

public enum VirtualAirline {
    NARU_AIRLINE("Naru Airline", URI.create("")),
    LOCAL("Localhost", URI.create("ws://127.0.0.1:6001"));

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
