package com.naver.idealproduction.song.view;

import com.naver.idealproduction.song.SimData;
import org.junit.jupiter.api.Test;

class DashboardTest {

    @Test
    void onUpdate() {
        var online = false;
        SimData dataStub = new SimData() {
            @Override
            public boolean isConnected() {
                return online;
            }
        };

    }
}