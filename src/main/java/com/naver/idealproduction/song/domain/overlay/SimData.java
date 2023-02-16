package com.naver.idealproduction.song.domain.overlay;

import com.fasterxml.jackson.annotation.JsonView;
import com.naver.idealproduction.song.domain.unit.Simvar;

import java.util.HashMap;
import java.util.Map;

public class SimData {
    public interface WebView {}

    private final Map<String, Object> map = new HashMap<>();

    @JsonView(WebView.class)
    public Map<String, Object> getMap() {
        return map;
    }

    public Object get(Simvar simvar) {
        var key = simvar.toString().toLowerCase();
        return map.get(key);
    }

    public void put(Simvar simVar, Object value) {
        var key = simVar.toString().toLowerCase();
        map.put(key, value);
    }
}
