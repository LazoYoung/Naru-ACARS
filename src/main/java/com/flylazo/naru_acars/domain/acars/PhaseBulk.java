package com.flylazo.naru_acars.domain.acars;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flylazo.naru_acars.domain.Phase;
import com.flylazo.naru_acars.domain.acars.request.Bulk;

public class PhaseBulk extends Bulk {
    @JsonProperty
    public String event;
    @JsonProperty
    public int phase;

    public PhaseBulk(Phase phase) {
        this.event = "status";
        this.phase = phase.ordinal();
    }
}
