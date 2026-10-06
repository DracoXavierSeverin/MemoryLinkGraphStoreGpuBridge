package com.dracoxavierseverin.memorylinkgraphstoregpubridge;

public interface GraphStore {
    void storeGraph(String key, Object graphData);
    Object loadGraph(String key);
    void releaseGraph(String key);
    int graphCount();
}
