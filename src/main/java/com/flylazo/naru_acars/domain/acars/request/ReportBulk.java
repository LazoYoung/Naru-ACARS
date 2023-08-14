package com.flylazo.naru_acars.domain.acars.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReportBulk extends Bulk {
    @JsonProperty
    public double latitude;
    @JsonProperty
    public double longitude;
    @JsonProperty
    public int altitude;
    @JsonProperty
    public int ias;
    @JsonProperty
    public int heading;
}
