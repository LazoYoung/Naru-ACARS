package com.flylazo.naru_acars.servlet.service;

import jakarta.annotation.Nullable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ACARS_Service {
    private final List<Runnable> listeners = new ArrayList<>();
    private boolean connected = false;
    private String server = null;

    public boolean isConnected() {
        return connected;
    }

    @Nullable
    public String getServer() {
        return server;
    }

    public void addUpdateListener(Runnable run) {
        listeners.add(run);
    }

    public void notifyUpdate() {
        listeners.forEach(Runnable::run);
    }
}
