package com.naver.idealproduction.song.repository;

import com.naver.idealproduction.song.service.OverlayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OverlayServiceTest {

    @Autowired
    private OverlayService repository;

    // todo Revise test logic
//    @Test
//    void getAll() {
//        var backList = new ArrayList<Overlay>();
//        var test1 = new Overlay("TEst1", "");
//        var test2 = new Overlay("test2", "");
//        var test3 = new Overlay("test3", "");
//        backList.add(test1);
//        backList.add(test2);
//        backList.add(test3);
//
//        for (var overlay : backList) {
//            repository.add(overlay);
//        }
//
//        var list = repository.getOverlays();
//
//        for (var e : backList) {
//            if (!list.contains(e)) {
//                fail(String.format("getAll() failed to include: %s", e.getId()));
//            }
//        }
//    }
//
//    @Test
//    void select() {
//        var test1 = new Overlay("TEst1", "");
//        var test2 = new Overlay("test2", "");
//        repository.add(test1);
//        repository.add(test2);
//
//        assertThat(repository.get()).isEmpty();
//
//        repository.select(test1.getId());
//        var selected = repository.get();
//        assertThat(selected).isNotEmpty().get().isEqualTo(test1);
//
//        repository.select(test2.getId());
//        selected = repository.get();
//        assertThat(selected).isNotEmpty().get().isEqualTo(test2);
//
//        repository.select(null);
//        assertThat(repository.get()).isEmpty();
//    }
}