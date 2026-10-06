package com.dracoxavierseverin.memorylinkgraphstoregpubridge.comm;

public interface MessageListener {
    void onMessage(String from, String topic, Object payload);
}
