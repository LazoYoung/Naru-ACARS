package com.flylazo.naru_acars.domain.overlay;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import java.util.List;

@XmlType
public class Animate {
    private String type;
    private int interval;
    private List<Simvar> simVars;

    @XmlAttribute(name = "type")
    public void setType(String type) {
        this.type = type;
    }

    @XmlAttribute(name = "interval")
    public void setInterval(int interval) {
        this.interval = interval;
    }

    @XmlElement(name = "simvar")
    public void setSimVars(List<Simvar> simVars) {
        this.simVars = simVars;
    }

    public String getType() {
        return type;
    }

    public int getInterval() {
        return interval;
    }

    public List<Simvar> getSimVars() {
        return simVars;
    }
}
