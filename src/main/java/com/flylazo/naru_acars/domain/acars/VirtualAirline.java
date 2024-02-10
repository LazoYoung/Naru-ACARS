package com.flylazo.naru_acars.domain.acars;

import java.net.URI;

public enum VirtualAirline {
    // todo assign proper uri when ready
    LOCAL(0, "Localhost", URI.create("ws://127.0.0.1:6001/socket")),
    NARU_AIRLINE(1, "Naru Airline", URI.create("")),
    ;

    private final String text;
    private final URI uri;
    private final int id;

    VirtualAirline(int id, String text, URI uri) {
        this.id = id;
        this.text = text;
        this.uri = uri;
    }

    public static VirtualAirline getById(int id) {
        for (var va : values()) {
            if (va.id == id) {
                return va;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return text;
    }

    public URI getUri() {
        return uri;
    }

    public int getId() {
        return id;
    }
}
