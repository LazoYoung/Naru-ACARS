package com.naver.idealproduction.song.entity.overlay;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

import java.util.List;

@XmlRootElement(name = "overlay")
public class Overlay {
    private String id;
    private String name;
    private String background;
    private List<Icon> icons;
    private List<Label> labels;

    public Overlay() {}

    public Overlay(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @XmlTransient
    public void setId(String id) {
        this.id = id;
    }

    @XmlAttribute(name = "name")
    public void setName(String name) {
        this.name = name;
    }

    @XmlAttribute(name = "background")
    public void setBackground(String background) {
        this.background = background;
    }

    @XmlElement(name = "icon")
    public void setIcons(List<Icon> icons) {
        this.icons = icons;
    }

    @XmlElement(name = "label")
    public void setLabels(List<Label> labels) {
        this.labels = labels;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBackground() {
        if (!background.startsWith("/overlay/")) {
            background = "/overlay/" + background;
        }
        return background;
    }

    public List<Icon> getIcons() {
        return icons;
    }

    public List<Label> getLabels() {
        return labels;
    }
}
