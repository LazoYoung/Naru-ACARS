package com.naver.idealproduction.song.repo;

import com.naver.idealproduction.song.entity.Overlay;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

@Repository
public class OverlayRepository {

    private final List<Overlay> list = new ArrayList<>();
    private final List<Runnable> listeners = new ArrayList<>();
    private int activeOverlay = -1;

    public Optional<Overlay> get(String name) {
        return list.stream()
                .filter(e -> e.getName().equals(name))
                .findAny();
    }

    public List<Overlay> getAll() {
        return new ArrayList<>(list);
    }

    public Optional<Overlay> getSelected() {
        if (activeOverlay < 0) {
            return Optional.empty();
        }
        return Optional.of(list.get(activeOverlay));
    }

    public void add(Overlay... overlay) {
        list.addAll(List.of(overlay));
        notifyListeners();
    }

    public void addUpdateListener(Runnable run) {
        listeners.add(run);
    }

    public void remove(String name) {
        ListIterator<Overlay> iter = list.listIterator();

        while (iter.hasNext()) {
            if (iter.next().getName().equals(name)) {
                iter.remove();
                notifyListeners();
                return;
            }
        }
    }

    public void select(@Nullable String name) {
        if (name == null) {
            activeOverlay = -1;
            return;
        }

        for (int i = 0; i < list.size(); ++i) {
            var matcher = list.get(i).getName();

            if (matcher.equals(name.toLowerCase())) {
                activeOverlay = i;
                return;
            }
        }
    }

    private void notifyListeners() {
        listeners.forEach(Runnable::run);
    }

}
