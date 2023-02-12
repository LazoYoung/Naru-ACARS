package com.naver.idealproduction.song.entity.overlay;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import java.util.List;

@XmlType
public class Animate {
    private List<String> simVars;

    @XmlElement(name = "simvar")
    public void setSimVars(List<String> simVars) {
        this.simVars = simVars;
    }

    public List<String> getSimVars() {
        return simVars;
    }
}
