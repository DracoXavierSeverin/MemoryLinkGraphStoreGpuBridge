package com.dracoxavierseverin.memorylinkgraphstoregpubridge.impl;

import com.dracoxavierseverin.memorylinkgraphstoregpubridge.GraphStore;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultGraphStore implements GraphStore {
    private final ConcurrentHashMap<String, Object> graphs = new ConcurrentHashMap<>();

    @Override
    public void storeGraph(String key, Object graphData) {
        if (key == null) return;
        graphs.put(key, graphData);
    }

    @Override
    public Object loadGraph(String key) {
        return graphs.get(key);
    }

    @Override
    public void releaseGraph(String key) {
        graphs.remove(key);
    }

    @Override
    public int graphCount() {
        return graphs.size();
    }
}
