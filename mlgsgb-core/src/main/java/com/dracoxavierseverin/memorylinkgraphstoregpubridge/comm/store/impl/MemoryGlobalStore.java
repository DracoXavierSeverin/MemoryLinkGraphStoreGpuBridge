package com.dracoxavierseverin.memorylinkgraphstoregpubridge.comm.store.impl;

import com.dracoxavierseverin.memorylinkgraphstoregpubridge.comm.store.Store;
import com.dracoxavierseverin.memorylinkgraphstoregpubridge.comm.store.StoreBuilder;
import com.dracoxavierseverin.memorylinkgraphstoregpubridge.comm.store.StoreEntry;
import com.dracoxavierseverin.memorylinkgraphstoregpubridge.comm.store.StoreLifetime;
import com.dracoxavierseverin.memorylinkgraphstoregpubridge.comm.store.StoreScope;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class MemoryGlobalStore implements Store {

    private static final long DEFAULT_CAPACITY = 24L * 1024L * 1024L;

    private final String id;
    private final long capacityBytes;
    private final AtomicLong usedBytes = new AtomicLong(0);
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, StoreEntry>> data =
            new ConcurrentHashMap<>();

    public MemoryGlobalStore(StoreBuilder builder) {
        this.id = builder.getId() == null ? "global-temp" : builder.getId();
        long cap = builder.getCapacityBytes();
        this.capacityBytes = cap > 0 ? cap : DEFAULT_CAPACITY;
    }

    public MemoryGlobalStore() {
        this.id = "global-temp";
        this.capacityBytes = DEFAULT_CAPACITY;
    }

    private static final long ESTIMATED_ENTRY_BYTES = 256L;

    @Override
    public String id() { return id; }

    @Override
    public StoreScope scope() { return StoreScope.GLOBAL; }

    @Override
    public StoreLifetime lifetime() { return StoreLifetime.TEMP; }

    @Override
    public long capacityBytes() { return capacityBytes; }

    @Override
    public long usedBytes() { return usedBytes.get(); }

    @Override
    public int size() {
        int total = 0;
        for (ConcurrentHashMap<String, StoreEntry> m : data.values()) {
            total += m.size();
        }
        return total;
    }

    @Override
    public boolean put(String moduleId, String tag, Object content) {
        if (moduleId == null || tag == null) return false;

        long projected = usedBytes.get() + ESTIMATED_ENTRY_BYTES;
        if (projected > capacityBytes) return false;

        ConcurrentHashMap<String, StoreEntry> bucket =
                data.computeIfAbsent(moduleId, k -> new ConcurrentHashMap<>());

        StoreEntry previous = bucket.put(tag, new StoreEntry(moduleId, tag, content));

        if (previous == null) {
            usedBytes.addAndGet(ESTIMATED_ENTRY_BYTES);
        }
        return true;
    }

    @Override
    public Object get(String moduleId, String tag) {
        if (moduleId == null || tag == null) return null;
        ConcurrentHashMap<String, StoreEntry> bucket = data.get(moduleId);
        if (bucket == null) return null;
        StoreEntry e = bucket.get(tag);
        return e == null ? null : e.content;
    }

    @Override
    public boolean contains(String moduleId, String tag) {
        if (moduleId == null || tag == null) return false;
        ConcurrentHashMap<String, StoreEntry> bucket = data.get(moduleId);
        return bucket != null && bucket.containsKey(tag);
    }

    @Override
    public boolean remove(String moduleId, String tag) {
        if (moduleId == null || tag == null) return false;
        ConcurrentHashMap<String, StoreEntry> bucket = data.get(moduleId);
        if (bucket == null) return false;
        StoreEntry removed = bucket.remove(tag);
        if (removed != null) {
            usedBytes.addAndGet(-ESTIMATED_ENTRY_BYTES);
            if (bucket.isEmpty()) data.remove(moduleId);
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        data.clear();
        usedBytes.set(0);
    }

    @Override
    public void release() {
        clear();
    }

    @Override
    public java.util.List<String> modules() {
        return new java.util.ArrayList<>(data.keySet());
    }

    @Override
    public java.util.List<String> tags(String moduleId) {
        if (moduleId == null) return new java.util.ArrayList<>();
        ConcurrentHashMap<String, StoreEntry> bucket = data.get(moduleId);
        if (bucket == null) return new java.util.ArrayList<>();
        return new java.util.ArrayList<>(bucket.keySet());
    }
}
