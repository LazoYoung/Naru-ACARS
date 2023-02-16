package com.naver.idealproduction.song.domain.overlay;

import jakarta.xml.bind.annotation.XmlAttribute;

public class Icon {
    private String file;
    private int x;
    private int y;
    private int width;
    private int height;

    @XmlAttribute(name = "file")
    public void setFile(String file) {
        this.file = file;
    }

    @XmlAttribute(name = "x")
    public void setX(int x) {
        this.x = x;
    }

    @XmlAttribute(name = "y")
    public void setY(int y) {
        this.y = y;
    }

    @XmlAttribute(name = "width")
    public void setWidth(int width) {
        this.width = width;
    }

    @XmlAttribute(name = "height")
    public void setHeight(int height) {
        this.height = height;
    }

    public String getFile() {
        if (!file.startsWith("/")) {
            file = "/" + file;
        }
        return file;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
