package com.flylazo.naru_acars.domain.acars.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FetchBulk extends Bulk {
    @JsonProperty
    public String type;
}
