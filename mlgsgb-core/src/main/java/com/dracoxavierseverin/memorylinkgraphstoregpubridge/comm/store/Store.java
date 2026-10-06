package com.dracoxavierseverin.memorylinkgraphstoregpubridge.comm.store;

import java.util.List;

public interface Store {

    String id();

    StoreScope scope();

    StoreLifetime lifetime();

    long capacityBytes();

    long usedBytes();

    int size();

    List<String> modules();

    List<String> tags(String moduleId);

    boolean put(String moduleId, String tag, Object content);

    Object get(String moduleId, String tag);

    boolean contains(String moduleId, String tag);

    boolean remove(String moduleId, String tag);

    void clear();

    void release();
}
