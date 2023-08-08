package com.flylazo.naru_acars.domain.acars.request;

import com.fasterxml.jackson.annotation.JsonGetter;

public class AuthBulk extends Bulk {
    private final String key;

    public AuthBulk(String key) {
        this.key = key;
    }

    @JsonGetter("key")
    public String getKey() {
        return key;
    }
}
