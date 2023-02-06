package com.naver.idealproduction.song.entity;

import jakarta.annotation.Nonnull;

import java.util.Objects;

public class Overlay {
    private final String name;

    private final String path;

    public Overlay(@Nonnull String name, @Nonnull String path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Overlay overlay = (Overlay) o;
        return name.equals(overlay.name) && path.equals(overlay.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, path);
    }
}
