package com.naver.idealproduction.song.view;

import com.naver.idealproduction.song.SimData;
import com.naver.idealproduction.song.SimMonitor;
import com.naver.idealproduction.song.SimUpdateListener;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

class DashboardTest {

    List<SimUpdateListener> listeners = new ArrayList<>();

    @Test
    void onUpdate() {
        var simMonitor = Mockito.mock(SimMonitor.class);
        doReturn(1000).when(simMonitor).getRefreshRate();
        doAnswer(invocation -> {
            listeners.add(invocation.getArgument(0));
            System.out.println("Registered a listener.");
            return null;
        }).when(simMonitor).addUpdateListener(any());

        var dashboard = mock(Dashboard.class);
        doAnswer(invocation -> {
            System.out.println("Event received.");
            return null;
        }).when(dashboard).onUpdate(any());

        simMonitor.addUpdateListener(dashboard);
        notifyListeners(mock(SimData.class));
        verify(dashboard).onUpdate(any());
    }

    private void notifyListeners(SimData data) {
        listeners.forEach(l -> l.onUpdate(data));
    }

}