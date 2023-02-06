package com.naver.idealproduction.song.repo;

import com.naver.idealproduction.song.entity.Overlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OverlayRepository {

    private final List<Overlay> list = new ArrayList<>();

    public OverlayRepository() {
        var headsUpDisplay = new Overlay("HUD");
        var platformDisplay = new Overlay("Platform display");
        var boardingPass = new Overlay("Boarding pass");
        list.add(headsUpDisplay);
        list.add(platformDisplay);
        list.add(boardingPass);
    }

    public Optional<Overlay> get(String name) {
        return list.stream()
                .filter(e -> e.getName().equals(name))
                .findAny();
    }

    public List<Overlay> getAll() {
        return new ArrayList<>(list);
    }

}
