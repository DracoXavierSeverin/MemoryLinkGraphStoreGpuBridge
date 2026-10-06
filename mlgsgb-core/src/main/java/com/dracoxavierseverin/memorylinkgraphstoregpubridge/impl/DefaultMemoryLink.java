package com.dracoxavierseverin.memorylinkgraphstoregpubridge.impl;

import com.dracoxavierseverin.memorylinkgraphstoregpubridge.MemoryLink;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultMemoryLink implements MemoryLink {
    private final ConcurrentHashMap<String, Object> links = new ConcurrentHashMap<>();

    @Override
    public void connect(String tag, Object target) {
        if (tag == null) return;
        links.put(tag, target);
    }

    @Override
    public void disconnect(String tag) {
        links.remove(tag);
    }

    @Override
    public Object get(String tag) {
        return links.get(tag);
    }

    @Override
    public boolean isConnected(String tag) {
        return links.containsKey(tag);
    }
}
