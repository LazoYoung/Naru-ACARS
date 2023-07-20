package com.flylazo.naru_acars.servlet.bridge;

public interface BridgeListener {
    void onConnected(SimBridge newBridge);
    void onDisconnected();
    void onProcess();
    void onFail(String message);
}
