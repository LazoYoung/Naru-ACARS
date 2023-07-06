package com.naver.idealproduction.song.servlet.bridge;

public interface BridgeListener {
    void onConnected(SimBridge newBridge);
    void onDisconnected();
    void onProcess();
    void onFail(String message);
}
