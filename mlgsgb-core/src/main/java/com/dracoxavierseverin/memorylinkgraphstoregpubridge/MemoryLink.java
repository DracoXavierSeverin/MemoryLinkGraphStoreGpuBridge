package com.dracoxavierseverin.memorylinkgraphstoregpubridge;

public interface MemoryLink {
    void connect(String tag, Object target);
    void disconnect(String tag);
    Object get(String tag);
    boolean isConnected(String tag);
}
