package com.flylazo.naru_acars.domain.acars.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Request {
    private String intent;
    private String ident;
    private Bulk bulk;

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public String getIdent() {
        return ident;
    }

    public void setIdent(String ident) {
        this.ident = ident;
    }

    public Bulk getBulk() {
        return bulk;
    }

    public void setBulk(Bulk bulk) {
        this.bulk = bulk;
    }

    public String serialize() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }
}
