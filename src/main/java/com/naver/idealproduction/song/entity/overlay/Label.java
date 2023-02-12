package com.naver.idealproduction.song.entity.overlay;

import jakarta.annotation.Nullable;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlType(name = "label")
public class Label {
    private int x;
    private int y;
    private int size;
    private String simVar;
    private Animate animate;

    @XmlAttribute(name = "x")
    public void setX(int x) {
        this.x = x;
    }

    @XmlAttribute(name = "y")
    public void setY(int y) {
        this.y = y;
    }

    @XmlAttribute(name = "size")
    public void setSize(int size) {
        this.size = size;
    }

    @XmlElement(name = "simvar")
    public void setSimVar(String simVar) {
        this.simVar = simVar;
    }

    @XmlElement(name = "animate")
    public void setAnimate(Animate animate) {
        this.animate = animate;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getSize() {
        return size;
    }

    @Nullable
    public String getSimVar() {
        return simVar;
    }

    @Nullable
    public Animate getAnimate() {
        return animate;
    }
}
