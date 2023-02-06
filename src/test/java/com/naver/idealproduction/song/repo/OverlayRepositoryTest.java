package com.naver.idealproduction.song.repo;

import com.naver.idealproduction.song.entity.Overlay;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
class OverlayRepositoryTest {

    @Autowired
    private OverlayRepository repository;

    @Test
    void getAll() {
        var backList = new ArrayList<Overlay>();
        var test1 = new Overlay("test1");
        var test2 = new Overlay("test2");
        var test3 = new Overlay("test3");
        backList.add(test1);
        backList.add(test2);
        backList.add(test3);

        for (var overlay : backList) {
            repository.add(overlay);
        }

        var list = repository.getAll();

        for (var e : backList) {
            if (!list.contains(e)) {
                fail(String.format("getAll() failed to include: %s", e.getName()));
            }
        }
    }

    @Test
    void addAndGet() {
        var test1 = new Overlay("test1");
        var test2 = new Overlay("test2");
        var test1Name = test1.getName();
        var test2Name = test2.getName();
        repository.add(test1);
        repository.add(test2);

        var test1Get = repository.get(test1Name);
        var test2Get = repository.get(test2Name);

        assertThat(test1Get).isNotEmpty().get().isEqualTo(test1);
        assertThat(test2Get).isNotEmpty().get().isEqualTo(test2);
    }

    @Test
    void select() {
        var test1 = new Overlay("test1");
        var test2 = new Overlay("test2");
        repository.add(test1);
        repository.add(test2);

        assertThat(repository.getSelected()).isEmpty();

        repository.select(test1.getName());
        var selected = repository.getSelected();
        assertThat(selected).isNotEmpty().get().isEqualTo(test1);

        repository.select(test2.getName());
        selected = repository.getSelected();
        assertThat(selected).isNotEmpty().get().isEqualTo(test2);

        repository.select(null);
        assertThat(repository.getSelected()).isEmpty();
    }
}