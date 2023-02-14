package com.naver.idealproduction.song.entity.overlay;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class SimData {
    private final Map<String, Object> map = new HashMap<>();

    @JsonProperty("map")
    public Map<String, Object> getMap() {
        return map;
    }

    public void put(SimVar simVar, Object value) {
        var key = simVar.toString().toLowerCase();
        map.put(key, value);
    }
}
